/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.gradle.jvm

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorAttributeProvider
import org.asciidoctor.gradle.base.SafeMode
import org.asciidoctor.gradle.base.Transform
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.NonExtensible
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.ResolutionStrategy
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.util.GradleVersion
import org.ysb33r.grolifant.api.AbstractCombinedProjectTaskExtension
import org.ysb33r.grolifant.api.OperatingSystem

import java.util.regex.Pattern

import static org.ysb33r.grolifant.api.StringUtils.stringize

/** Extension for configuring AsciidoctorJ.
 *
 * It can be used as both a project and a task extension.
 *
 * @since 2.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
@SuppressWarnings('MethodCount')
@NonExtensible
class AsciidoctorJExtension extends AbstractCombinedProjectTaskExtension {

    // ------------------------------------------------------------------------
    // Be careful about modifying the keyword ordering in these six lines.
    // They are parsed by the build script to set up some compilation dependencies.
    // It is also a good idea that DEFAULT_ASCIIDOCTORJ_VERSION  matches one of
    // the values in testfixtures-jvm.
    // ------------------------------------------------------------------------
    final static String DEFAULT_ASCIIDOCTORJ_VERSION = '1.6.2'
    final static String DEFAULT_GROOVYDSL_VERSION = '1.6.0'
    final static String DEFAULT_PDF_VERSION = '1.5.0-alpha.16'
    final static String DEFAULT_EPUB_VERSION = '1.5.0-alpha.8.1'
    final static String DEFAULT_DIAGRAM_VERSION = '1.5.16'
    // ------------------------------------------------------------------------

    static final String ASCIIDOCTORJ_GROUP = 'org.asciidoctor'
    static final String ASCIIDOCTORJ_CORE_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj"
    static final String ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-groovy-dsl"
    static final String ASCIIDOCTORJ_PDF_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-pdf"
    static final String ASCIIDOCTORJ_EPUB_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-epub3"
    static final String ASCIIDOCTORJ_DIAGRAM_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-diagram"

    static final String JRUBY_COMPLETE_DEPENDENCY = 'org.jruby:jruby-complete'
    static final String JRUBY_DEPENDENCY = 'org.jruby:jruby'

    final static String NAME = 'asciidoctorj'

    static final OperatingSystem OS = OperatingSystem.current()
    public static final boolean GUAVA_REQUIRED_FOR_EXTERNALS = GradleVersion.current() >= GradleVersion.version('4.8')

    private Object version
    private Optional<Object> groovyDslVersion
    private Optional<Object> pdfVersion
    private Optional<Object> epubVersion
    private Optional<Object> diagramVersion
    private Optional<Object> jrubyVersion

    private Boolean injectGuavaJar

    private final Map<String, Object> options = [:]
    private final Map<String, Object> attributes = [:]
    private final List<Object> jrubyRequires = []
    private final List<Object> asciidoctorExtensions = []
    private final List<Object> gemPaths = []
    private final List<Action<ResolutionStrategy>> resolutionsStrategies = []
    private final List<Object> warningsAsErrors = []
    private final List<AsciidoctorAttributeProvider> attributeProviders = []

    private boolean onlyTaskOptions = false
    private boolean onlyTaskAttributes = false
    private boolean onlyTaskRequires = false
    private boolean onlyTaskExtensions = false
    private boolean onlyTaskGems = false
    private boolean onlyTaskWarnings = false

    private LogLevel logLevel

    private SafeMode safeMode

    /** Attach extension to a project.
     *
     * @param project
     */
    AsciidoctorJExtension(Project project) {
        super(project)

        this.attributes['gradle-project-name'] = project.name
        this.attributes['gradle-project-group'] = { project.group ?: '' }
        this.attributes['gradle-project-version'] = { project.version ?: '' }

        this.safeMode = SafeMode.SAFE
        this.version = DEFAULT_ASCIIDOCTORJ_VERSION
        this.groovyDslVersion = Optional.empty()
    }

    /** Attach extension to a task.
     *
     * @param task
     */
    AsciidoctorJExtension(Task task) {
        super(task, NAME)
    }

    /** Whether the Guava JAR that ships with the Gradle distribution should be injected into the
     * classpath for external AsciidoctorJ processes.
     *
     * If not set previously via {@link #setInjectInternalGuavaJar} then a default version depending of the version of
     * the Gradle distribution will be used.
     *
     * @return {@code true} if JAR should be injected.
     */
    boolean getInjectInternalGuavaJar() {
        if(task) {
            this.injectGuavaJar == null ? extFromProject.injectInternalGuavaJar : this.injectGuavaJar
        } else {
            this.injectGuavaJar == null ? GUAVA_REQUIRED_FOR_EXTERNALS : this.injectGuavaJar
        }
    }

    /** Whether the Guava JAR that ships with the Gradle distribution should be injected into the
     * classpath for external AsciidoctorJ processes.
     *
     * @param inject {@code true} if JAR should be injected.
     */
    void setInjectInternalGuavaJar(boolean inject) {
        this.injectGuavaJar = inject
    }

    /** Version of AsciidoctorJ that should be used.
     *
     */
    String getVersion() {
        if (task) {
            this.version ? stringize(this.version) : extFromProject.getVersion()
        } else {
            stringize(this.version)
        }
    }

    /** Set a new version to use.
     *
     * @param v New version to be used. Can be of anything that be be resolved by {@link stringize ( Object o )}
     */
    void setVersion(Object v) {
        this.version = v
    }

    /** Version of the Groovy Extension DSL that should be used.
     *
     * @return Version of extension DSL or {@code null} if extensions will not be used.
     */
    String getGroovyDslVersion() {
        if (task) {
            if (this.groovyDslVersion != null && this.groovyDslVersion.present) {
                stringize(this.groovyDslVersion.get())
            } else {
                extFromProject.getGroovyDslVersion()
            }
        } else {
            this.groovyDslVersion?.present ? stringize(this.groovyDslVersion.get()) : null
        }
    }

    /** Set a new Groovy DSL version to use
     *
     * @param v Groovy DSL version.
     */
    void setGroovyDslVersion(Object v) {
        this.groovyDslVersion = Optional.of(v)
    }

    /** The version of JRuby to use.
     *
     * If no version of JRuby is specified the one that is linked to AsciidoctorJ
     * will be used.
     *
     * @return Version of JRuby to use or {@code null} to use the JRUby version that is
     * linked to the specified vesrion of AsciidoctorJ.
     *
     */
    String getJrubyVersion() {
        if (task) {
            if (this.jrubyVersion != null && this.jrubyVersion.present) {
                stringize(this.jrubyVersion.get())
            } else {
                extFromProject.getJrubyVersion()
            }
        } else {
            this.jrubyVersion?.present ? stringize(this.jrubyVersion.get()) : null
        }
    }

    /** Set a version of JRuby to use.
     *
     * The version specified is not a guarantetd version, simply a minimum required version.
     * If the version of asciidoctorj is dependent on a version later than the one specified
     * here, then that would be used instead. In such cases iif the exact version needs to be
     * forced then a resolution strategy needs to be provided via {@link #resolutionStrategy}.
     *
     * @param v JRuby version
     */
    void setJrubyVersion(Object v) {
        this.jrubyVersion = Optional.of(v)
    }

    /** Version of the {@code asciidoctorj-diagram} that should be used.
     *
     * @return Version of {@code asciidoctorj-diagram} or {@code null} if not used.
     */
    String getDiagramVersion() {
        if (task) {
            if (this.diagramVersion != null && this.diagramVersion.present) {
                stringize(this.diagramVersion.get())
            } else {
                extFromProject.getDiagramVersion()
            }
        } else {
            this.diagramVersion?.present ? stringize(this.diagramVersion.get()) : null
        }
    }

    /** Set a new {@code asciidoctorj-diagram} version to use.
     *
     * Setting this will automatically add {@code} to list of {@code requires}.
     *
     * @param v {@code asciidoctorj-diagram} version.
     */
    void setDiagramVersion(Object v) {
        this.diagramVersion = Optional.of(v)

        if (v != null) {
            requires 'asciidoctor-diagram'
        }
    }

    /** Version of the Asciidoctor EPUB that should be used.
     *
     * @return Version of Asciidoctor EPUB or {@code null} if EPUB conversion is not used.
     */
    String getEpubVersion() {
        if (task) {
            if (this.epubVersion != null && this.epubVersion.present) {
                stringize(this.epubVersion.get())
            } else {
                extFromProject.getEpubVersion()
            }
        } else {
            this.epubVersion?.present ? stringize(this.epubVersion.get()) : null
        }
    }

    /** Set a new asciidoctor EPUB version to use
     *
     * @param v Asciidoctor EPUB version.
     */
    void setEpubVersion(Object v) {
        this.epubVersion = Optional.of(v)
    }

    /** Version of the Asciidoctor PDF that should be used.
     *
     * @return Version of Asciidoctor PDF or {@code null} if PDF conversion is not used.
     */
    String getPdfVersion() {
        if (task) {
            if (this.pdfVersion != null && this.pdfVersion.present) {
                stringize(this.pdfVersion.get())
            } else {
                extFromProject.getPdfVersion()
            }
        } else {
            this.pdfVersion?.present ? stringize(this.pdfVersion.get()) : null
        }
    }

    /** Set a new asciidoctor PDF version to use
     *
     * @param v Asciidoctor PDF version.
     */
    void setPdfVersion(Object v) {
        this.pdfVersion = Optional.of(v)
    }

    /** Returns all of the Asciidoctor options.
     *
     * @return Map with all scalars converted to strings. items in containers are
     *   also converted to strings but the contianer stucture is maintained.
     */
    Map<String, Object> getOptions() {
        stringizeMapRecursive(this.options, onlyTaskOptions) { AsciidoctorJExtension it ->
            it.options
        }
    }

    /** Apply a new set of Asciidoctor options, clearing any options previously set.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will override the global options.
     *
     * @param m Map with new options
     */
    void setOptions(Map m) {
        checkForAttributesInOptions(m)
        this.options.clear()
        this.options.putAll(m)

        if (task) {
            onlyTaskOptions = true
        }
    }

    /** Add additional Asciidoctor options
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will use this options in the task in addition to any global options.
     *
     * @param m Map with new options
     */
    @SuppressWarnings('ConfusingMethodName')
    void options(Map m) {
        checkForAttributesInOptions(m)
        this.options.putAll(m)
    }

    /** Returns all of the Asciidoctor options.
     *
     */
    Map<String, Object> getAttributes() {
        stringizeMapRecursive(this.attributes, onlyTaskOptions) { AsciidoctorJExtension it ->
            it.attributes
        }
    }

    /** Apply a new set of Asciidoctor attributes, clearing any attributes previously set.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will override the global attributes.
     *
     * @param m Map with new options
     */
    void setAttributes(Map m) {
        this.attributes.clear()
        this.attributes.putAll(m)

        if (task) {
            onlyTaskAttributes = true
        }
    }

    /** Add additional Asciidoctor attributes.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will use this attributes in the task in addition to any global attributes.
     *
     * @param m Map with new options
     */
    @SuppressWarnings('ConfusingMethodName')
    void attributes(Map m) {
        this.attributes.putAll(m)
    }

    /** Returns a list of additional attribute providers.
     *
     * @return List of providers. Can be empty. Never {@code null}.
     */
    List<AsciidoctorAttributeProvider> getAttributeProviders() {
        if(task) {
            this.attributeProviders.empty ? extFromProject.attributeProviders : this.attributeProviders
        } else {
            this.attributeProviders
        }
    }

    /** Adds an additional attribute provider.
     *
     * @param provider
     */
    void attributeProvider(AsciidoctorAttributeProvider provider) {
        this.attributeProviders.add(provider)
    }

    /** Adds a closure as an additional attribute provider.
     *
     * @param provider A closure must return a Map<String,Object>
     */
    void attributeProvider(Closure provider) {
        attributeProvider(provider as AsciidoctorAttributeProvider)
    }

    /** Returns the set of Ruby modules to be included.
     *
     * @since 1.5.0
     */
    Set<String> getRequires() {
        stringizeList(this.jrubyRequires, onlyTaskRequires) { AsciidoctorJExtension it ->
            it.requires.toList()
        }.toSet()
    }

    /** Applies a new set of Ruby modules to be included, clearing any previous set.
     *
     * If this is called on a task extension, the global project requires will be ignored.
     *
     * @param b One or more ruby modules to be included
     */
    void setRequires(Object... b) {
        this.jrubyRequires.clear()
        this.jrubyRequires.addAll(b)

        if (task) {
            onlyTaskRequires = true
        }
    }

    /** Appends new set of Ruby modules to be included.
     *
     * @param b One or more ruby modules to be included
     */
    @SuppressWarnings('ConfusingMethodName')
    void requires(Object... b) {
        this.jrubyRequires.addAll(b)
    }

    /** Returns the list of paths to be used for {@code GEM_HOME}
     *
     */
    FileCollection getGemPaths() {
        if (!task || onlyTaskGems) {
            project.files(this.gemPaths)
        } else {
            project.files(this.gemPaths) + extFromProject.gemPaths
        }
    }

    /** Sets a new list of GEM paths to be used.
     *
     * @param paths Paths resolvable by {@ocde project.files}
     */
    void setGemPaths(Iterable<Object> paths) {
        this.gemPaths.clear()
        this.gemPaths.addAll(paths)

        if (task) {
            this.onlyTaskGems = true
        }
    }

    /** Adds more paths for discovering GEMs.
     *
     * @param f Path objects that can be be converted with {@code project.file}.
     */
    @SuppressWarnings('ConfusingMethodName')
    void gemPaths(Object... f) {
        this.gemPaths.addAll(f)
    }

    /** Returns the list of paths to be used for GEM installations in a format that is suitable for assignment to {@code GEM_HOME}
     *
     * Calling this will cause gemPath to be resolved immediately.
     */
    String asGemPath() {
        getGemPaths().files*.toString().join(OS.pathSeparator)
    }

    /** Returns the ASciidoctor SafeMode under which a conversion will be run.
     *
     * @return Asciidoctor Safe Mode
     */
    SafeMode getSafeMode() {
        (task && this.safeMode || !task) ? this.safeMode : extFromProject.safeMode
    }

    /** Set Asciidoctor safe mode.
     *
     * @param mode An instance of Asciidoctor SafeMode.
     */
    void setSafeMode(SafeMode mode) {
        this.safeMode = mode
    }

    /** Set Asciidoctor safe mode.
     *
     * @param mode A valid integer representing a Safe Mode
     */
    void setSafeMode(int mode) {
        this.safeMode = SafeMode.safeMode(mode)
    }

    /** Set Asciidoctor safe mode.
     *
     * @param mode A valid string representing a Safe Mode
     */
    void setSafeMode(String mode) {
        this.safeMode = SafeMode.valueOf(mode.toUpperCase())
    }

    /** Returns a runConfiguration of the configured AsciidoctorJ dependencies.
     *
     * @return A non-attached runConfiguration.
     */
    @SuppressWarnings('DuplicateStringLiteral')
    Configuration getConfiguration() {
        final String gDslVer = getGroovyDslVersion()
        final String pdfVer = getPdfVersion()
        final String epubVer = getEpubVersion()
        final String diagramVer = getDiagramVersion()
        final String jrubyVer = getJrubyVersion() ?: minimumSafeJRubyVersion(getVersion())
        final String jrubyCompleteDep = "${JRUBY_COMPLETE_DEPENDENCY}:${jrubyVer}"

        List<Dependency> deps = [createDependency("${ASCIIDOCTORJ_CORE_DEPENDENCY}:${getVersion()}")]

        if (gDslVer != null) {
            deps.add(createDependency("${ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY}:${gDslVer}"))
        }

        if (pdfVer != null) {
            deps.add(createDependency("${ASCIIDOCTORJ_PDF_DEPENDENCY}:${pdfVer}"))
        }

        if (epubVer != null) {
            deps.add(createDependency("${ASCIIDOCTORJ_EPUB_DEPENDENCY}:${epubVer}"))
        }

        if (diagramVer != null) {
            deps.add(createDependency(
                "${ASCIIDOCTORJ_DIAGRAM_DEPENDENCY}:${diagramVer}", excludeTransitiveAsciidoctorJ()
            ))
        }

        deps.add(
            createDependency(jrubyCompleteDep)
        )

        Configuration configuration = project.configurations.detachedConfiguration(
            deps.toArray() as Dependency[]
        )

        configuration.resolutionStrategy.eachDependency { DependencyResolveDetails dsr ->
            if (dsr.target.name == 'jruby' && dsr.target.group == 'org.jruby') {
                dsr.useTarget "${JRUBY_COMPLETE_DEPENDENCY}:${dsr.target.version}"
            }
        }

        resolutionsStrategies.each {
            configuration.resolutionStrategy(it)
        }

        configuration
    }

    /** Return extensionRegistry.
     *
     * These extensionRegistry are not registered at this call. That action is left
     * to the specific task at its execution time.
     *
     * @return
     */
    List<Object> getExtensions() {
        if (!task || onlyTaskExtensions) {
            this.asciidoctorExtensions
        } else if (this.asciidoctorExtensions.empty) {
            extFromProject.extensions
        } else {
            extFromProject.extensions + this.asciidoctorExtensions
        }
    }

    /** Defines extensionRegistry. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     */
    void extensions(Object... exts) {
        addExtensions(exts as List)
    }

    /** Clears the existing extensionRegistry and replace with a new set.
     *
     * If this is declared on a task extension all extention from the global
     * project extension will be ignored.
     */
    void setExtensions(Iterable<Object> newExtensions) {
        asciidoctorExtensions.clear()
        addExtensions(newExtensions as List)
        onlyTaskExtensions = true
    }

    /** The level at which the AsciidoctorJ process should be logging.
     *
     * @return The currently configured log level. By default this is {@code project.logging.level}.
     */
    LogLevel getLogLevel() {
        if (task) {
            this.logLevel == null ? extFromProject.logLevel : this.logLevel
        } else {
            this.logLevel ?: project.logging.level
        }
    }

    /** Set the level at which the AsciidoctorJ process should be logging.
     *
     * @param logLevel LogLevel to use
     */
    void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel
    }

    /** Set the level at which the AsciidoctorJ process should be logging.
     *
     * @param logLevel LogLevel to use
     */
    void setLogLevel(String logLevel) {
        this.logLevel = LogLevel.valueOf(logLevel.toUpperCase())
    }

    /** Clears the current list of resolution strategies.
     *
     */
    void clearResolutionStrategies() {
        this.resolutionsStrategies.clear()
    }

    /** Adds a resolution strategy for resolving asciidoctorj related dependencies
     *
     * @param strategy Additional resolution strategy. Takes a {@link ResolutionStrategy} as parameter.
     */
    void resolutionStrategy(Action<ResolutionStrategy> strategy) {
        this.resolutionsStrategies.add(strategy)
    }

    /** Adds a resolution strategy for resolving asciidoctorj related dependencies
     *
     * @param strategy Additional resolution strategy. Takes a {@link ResolutionStrategy} as parameter.
     */
    void resolutionStrategy(@DelegatesTo(ResolutionStrategy) Closure strategy) {
        this.resolutionsStrategies.add(strategy as Action<ResolutionStrategy>)
    }

    /** Returns a patterns suitable for detecting missing include files.
     *
     * This can be passed to {@link #fatalWarnings(Object ...)}
     *
     * @return Missing include file pattern.
     */
    Pattern missingIncludes() {
        ~/include file not found/
    }

    /** Provide patterns for Asciidoctor messages that are treated as failures.
     *
     * @return Regex patterns that will be used to check Asciidoctor log messages.
     *
     */
    List<Pattern> getFatalWarnings() {
        if (!task || onlyTaskWarnings) {
            patternize(this.warningsAsErrors)
        } else if (this.warningsAsErrors.empty) {
            extFromProject.fatalWarnings
        } else {
            extFromProject.fatalWarnings + patternize(this.warningsAsErrors)
        }
    }

    /** Provide patterns for Asciidoctor messages that are treated as failures.
     *
     * Clears any existing message patterns. If this method is called on a task extension,
     * the patterns from the project extension will be ignored.
     *
     * @param patterns
     */
    void setFatalWarnings(Iterable<Object> patterns) {
        onlyTaskWarnings = true
        this.warningsAsErrors.clear()
        this.warningsAsErrors.addAll(patterns)
    }

    /** Adds additional message patterns for treating Asciidoctor log messages as errors.
     *
     * @param patterns Message patterns.
     */
    void fatalWarnings(Object... patterns) {
        this.warningsAsErrors.addAll(patterns)
    }

    @SuppressWarnings('FactoryMethodName')
    private Dependency createDependency(final String notation, final Closure configurator = null) {
        if (configurator) {
            project.dependencies.create(notation, configurator)

        } else {
            project.dependencies.create(notation)

        }
    }

    private Collection<String> stringizeList(Collection<Object> list, boolean fromTaskOnly, Closure<Collection<String>> other) {
        if (!task || fromTaskOnly) {
            stringize(list)
        } else if (list.isEmpty()) {
            other.call(extFromProject)
        } else {
            List<Object> newOptions = []
            newOptions.addAll(other.call(extFromProject))
            newOptions.addAll(list)
            stringize(newOptions)
        }
    }

    private Map<String, Object> stringizeMapRecursive(Map<String, Object> map, boolean fromTaskOnly, Closure<Map<String, Object>> other) {
        if (!task || fromTaskOnly) {
            stringizeScalarMapItems(map)
        } else if (map.isEmpty()) {
            other.call(extFromProject)
        } else {
            Map<String, Object> newOptions = [:]
            newOptions.putAll(other.call(extFromProject))
            newOptions.putAll(map)
            stringizeScalarMapItems(newOptions)
        }
    }

    private List<Object> stringizeScalarListItems(List<Object> list) {
        Transform.toList(list) { item ->
            switch (item) {
                case List:
                    return stringizeScalarListItems((List) item)
                case Map:
                    return stringizeScalarMapItems((Map) item)
                case boolean:
                case Boolean:
                    return (Boolean) item
                case File:
                    return ((File) item).absolutePath
                default:
                    return stringize(item)
            }
        }
    }

    private Map<String, Object> stringizeScalarMapItems(Map<String, Object> map) {
        map.collectEntries { String key, Object item ->
            switch (item) {
                case List:
                    return [key, stringizeScalarListItems((List) item)]
                case Map:
                    return [key, stringizeScalarMapItems((Map) item)]
                case boolean:
                case Boolean:
                    return [key, ((Boolean) item)]
                case File:
                    return [key, ((File) item).absolutePath]
                default:
                    return [key, stringize(item)]
            }
        } as Map<String, Object>
    }

    private AsciidoctorJExtension getExtFromProject() {
        task ? (AsciidoctorJExtension) projectExtension : this
    }

    private void checkForAttributesInOptions(Map m) {
        if (m.containsKey('attributes')) {
            throw new GradleException('Attributes found in options. Please use \'attributes\' method')
        }
    }

    private void setDefaultGroovyDslVersionIfRequired() {
        if (getGroovyDslVersion() == null) {
            setGroovyDslVersion DEFAULT_GROOVYDSL_VERSION
        }
    }

    /** Adds extensions to the existing container.
     *
     * Also sets the Groovy DSL version if required.
     *
     * @param newExtensions List of new extensiosn to add
     */
    private void addExtensions(List<Object> newExtensions) {
        setDefaultGroovyDslVersionIfRequired()
        asciidoctorExtensions.addAll(dehydrateExtensions(newExtensions))
    }

    /** Prepare extensions for serialisation.
     *
     * This takes care of dehydrating any closures.
     *
     * @param exts List of extensions
     * @return List of extensions suitable for serialization.
     *
     */
    private List<Object> dehydrateExtensions(final List<Object> exts) {
        Transform.toList(exts){
            switch (it) {
                case Closure:
                    ((Closure) it).dehydrate()
                    break
                case Project:
                    project.dependencies.project(path: ((Project) it).path)
                    break
                default:
                    it
            }
        }
    }

    @SuppressWarnings('UnusedPrivateMethodParameter')
    private String minimumSafeJRubyVersion(final String asciidoctorjVersion) {
        '9.1.0.0'
    }

    @SuppressWarnings('Instanceof')
    private List<Pattern> patternize(final List<Object> patterns) {
        Transform.toList(patterns){
            (Pattern)(it instanceof Pattern ? it : ~/${stringize(it)}/)
        }
    }

    @CompileDynamic
    @SuppressWarnings('DuplicateStringLiteral')
    Closure excludeTransitiveAsciidoctorJ() {
        return {
            exclude(group: ASCIIDOCTORJ_GROUP, module: 'asciidoctorj')
            exclude(group: ASCIIDOCTORJ_GROUP, module: 'asciidoctorj-api')
        }

    }
}
