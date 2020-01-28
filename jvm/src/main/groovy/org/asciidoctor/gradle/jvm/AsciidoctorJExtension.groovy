/*
 * Copyright 2013-2020 the original author or authors.
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
import org.asciidoctor.gradle.base.AbstractImplementationEngineExtension
import org.asciidoctor.gradle.base.ModuleNotFoundException
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.internal.JavaExecUtils
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
import org.ysb33r.grolifant.api.OperatingSystem

import java.util.regex.Pattern

import static org.ysb33r.grolifant.api.ClosureUtils.configureItem
import static org.ysb33r.grolifant.api.StringUtils.stringize

/** Extension for configuring AsciidoctorJ.
 *
 * It can be used as both a project and a task extension.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.0
 */
@CompileStatic
@SuppressWarnings('MethodCount')
@NonExtensible
class AsciidoctorJExtension extends AbstractImplementationEngineExtension {

    public final static String NAME = 'asciidoctorj'

    private static final String ASCIIDOCTORJ_GROUP = 'org.asciidoctor'
    private static final String ASCIIDOCTORJ_CORE_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj"
    private static final String ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-groovy-dsl"
    private static final String ASCIIDOCTORJ_PDF_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-pdf"
    private static final String ASCIIDOCTORJ_EPUB_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-epub3"
    private static final String ASCIIDOCTORJ_DIAGRAM_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-diagram"
    private static final String ASCIIDOCTORJ_LEANPUB_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctor-leanpub-markdown"
    private static final String JRUBY_COMPLETE_DEPENDENCY = JavaExecUtils.JRUBY_COMPLETE_DEPENDENCY
    private static final String ASCIIDOCTOR_DEPENDENCY_PROPERTY_NAME = 'asciidoctorj'
    private static final OperatingSystem OS = OperatingSystem.current()
    private static final boolean GUAVA_REQUIRED_FOR_EXTERNALS = GradleVersion.current() >= GradleVersion.version('4.8')

    private Object version
    private Optional<Object> jrubyVersion

    private Boolean injectGuavaJar

    private final Map<String, Object> options = [:]
    private final List<Object> jrubyRequires = []
    private final List<Object> asciidoctorExtensions = []
    private final List<Object> gemPaths = []
    private final List<Action<ResolutionStrategy>> resolutionsStrategies = []
    private final List<Action<Configuration>> configurationCallbacks = []
    private final List<Object> warningsAsErrors = []
    private final AsciidoctorJModules modules

    private boolean onlyTaskOptions = false
    private boolean onlyTaskRequires = false
    private boolean onlyTaskExtensions = false
    private boolean onlyTaskGems = false
    private boolean onlyTaskWarnings = false
    private boolean onlyTaskResolutionStrategies = false
    private boolean onlyTaskConfigurationCallbacks = false

    private LogLevel logLevel

    /** Attach extension to a project.
     *
     * @param project
     */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    AsciidoctorJExtension(Project project) {
        super(project, 'asciidoctorj-extension')
        this.version = defaultVersionMap[ASCIIDOCTOR_DEPENDENCY_PROPERTY_NAME]
        this.modules = new AsciidoctorJModules(this, defaultVersionMap)

        if (this.version == null) {
            throw new ModuleNotFoundException('Default version for AsciidoctorJ must be defined. ' +
                'Please report a bug at https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues'
            )
        }
    }

    /** Attach extension to a task.
     *
     * @param task
     */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    AsciidoctorJExtension(Task task) {
        super(task, NAME)
        this.modules = new AsciidoctorJModules(this, defaultVersionMap)
    }

    /* -------------------------
       tag::extension-property[]
       docExtensions:: Groovy DSL and project-based extensions.
         Use `docExtensions` to add one or more extensions. Use `setDocExtensions` to replace the current set of
         extensions with a new set. Extensions can be any kind of object that is serialisable, although in most cases
         they will be strings or files. If extensions are detached dependencies, they will not be serialised, but
         rather will be placed on the classpath in order that {asciidoctorj-name} can pick them up automatically.
         See <asciidoctorj-extensions>> for more details.
       end::extension-property[]
       ------------------------- */

    /** Return extensions to be registered.
     *
     * These extensionRegistry are not registered at this call. That action is left
     * to the specific task at its execution time.
     *
     * @return All extensions that should be registered on a conversion.
     *
     * @since 2.2.0
     */
    List<Object> getDocExtensions() {
        if (!task || onlyTaskExtensions) {
            this.asciidoctorExtensions
        } else if (this.asciidoctorExtensions.empty) {
            extFromProject.docExtensions
        } else {
            extFromProject.docExtensions + this.asciidoctorExtensions
        }
    }

    /** Defines extensions to be registered. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     *
     * @since 2.2.0
     */
    void docExtensions(Object... exts) {
        addExtensions(exts as List)
    }

    /** Clears the existing list of extensions and replace with a new set.
     *
     * If this is declared on a task extension all extention from the global
     * project extension will be ignored.
     *
     * @since 2.2.0
     */
    void setDocExtensions(Iterable<Object> newExtensions) {
        asciidoctorExtensions.clear()
        addExtensions(newExtensions as List)
        onlyTaskExtensions = true
    }

    /* -------------------------
       tag::extension-property[]
       fatalWarnings:: Patterns for {asciidoctorj-name} log messages that should be treated as fatal errors.
         The list is empty be default. Use `setFatalWarnings` to clear any existing patterns or to decouple a task's
         configuration from the global configuration. Use `fatalWarnings` to add more patterns.
         Pass `missingIncludes()` to add the common use-case of missing include files.
       end::extension-property[]
       ------------------------- */

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

    /** Returns a patterns suitable for detecting missing include files.
     *
     * This can be passed to {@link #fatalWarnings(Object ...)}
     *
     * @return Missing include file pattern.
     */
    Pattern missingIncludes() {
        ~/include file not found/
    }

    /* -------------------------
       tag::extension-property[]
       gemPaths:: One or more gem installation directories (separated by the system path separator).
         Use `gemPaths` to append. Use `setGemPaths` or `gemPaths=['path1','path2']` to overwrite.
         Use `asGemPath` to obtain a path string, separated by platform-specific separator.
         Type: `FileCollection`, but any collection of objects convertible with `project.files` can be passed
         Default: empty
       end::extension-property[]
       ------------------------- */

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
    void gemPaths(Object... f) {
        this.gemPaths.addAll(f)
    }

    /** Returns the list of paths to be used for GEM installations in a format that is
     * suitable for assignment to {@code GEM_HOME}
     *
     * Calling this will cause gemPath to be resolved immediately.
     */
    String asGemPath() {
        getGemPaths().files*.toString().join(OS.pathSeparator)
    }

    /* -------------------------
       tag::extension-property[]
       jrubyVersion:: Minimum version of JRuby to be used.
         The exact version that will be used could be higher due to {asciidoctorj-name} having a transitive dependency
         that is newer.
       end::extension-property[]
       ------------------------- */

    /** The version of JRuby to use.
     *
     * If no version of JRuby is specified the one that is linked to AsciidoctorJ
     * will be used.
     *
     * @return Version of JRuby to use or {@code null} to use the JRuby version that is
     * linked to the specified version of AsciidoctorJ.
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

    /* -------------------------
       tag::extension-property[]
       logLevel:: The log level at which AsciidoctorJ will log.
         This is specified as a Gradle logging level. The plugin will translate it to the appropriate
         {asciidoctorj-name} logging level. Default is whatever `project.logger.level` is at the time of execution.
       end::extension-property[]
       ------------------------- */

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

    /* -------------------------
       tag::extension-property[]
        modules:: Configuration for version of specific components and converters that can be used.
          See <<asciidoctorj-modules>> for which moduels are supported.
       end::extension-property[]
       ------------------------- */

    /** Additional AsciidoctorJ modules to be configured.
     *
     * @return Module definition object.
     *
     * @since 2.2.0
     */
    AsciidoctorJModules getModules() {
        this.modules
    }

    /** Configure modules via a closure.
     *
     * @param cfg Configurating closure
     *
     * @since 2.2.0
     */
    void modules(@DelegatesTo(AsciidoctorJModules) Closure cfg) {
        configureItem(this.modules, cfg)
    }

    /** Configure modules via an {@code Action}.
     *
     * @param cfg Configurating {@code Action}
     *
     * @since 2.2.0
     */
    void modules(Action<AsciidoctorJModules> cfg) {
        cfg.execute(this.modules)
    }

    /* -------------------------
       tag::extension-property[]
       options:: {asciidoctorj-name} options.
         Use `options` to append and `setOptions` to replace any current options with a new set.
         Options are evaluated as late as possible. See <<options>> for more details.
       end::extension-property[]
       ------------------------- */

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
    void options(Map m) {
        checkForAttributesInOptions(m)
        this.options.putAll(m)
    }

    /* -------------------------
       tag::extension-property[]
       requires:: The set of Ruby modules to be included.
         Use `requires` to append. Use `setRequires` or `requires=['name']` to overwrite.
         Default: empty.
       end::extension-property[]
       ------------------------- */

    /** Returns the set of Ruby modules to be included.
     *
     * @since 1.5.0
     */
    List<String> getRequires() {
        stringizeList(this.jrubyRequires, onlyTaskRequires) { AsciidoctorJExtension it ->
            it.requires
        }.toList()
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
    void requires(Object... b) {
        this.jrubyRequires.addAll(b)
    }

    /* -------------------------
       tag::extension-property[]
        resolutionStrategy:: Strategies for resolving Asciidoctorj-related dependencies.
        {asciidoctorj-name} dependencies are held in a detached configuration.
        If for some special reason, you need to modify the way the dependency set is resolved, you can modify the
        behaviour by adding one or more strategies.
       end::extension-property[]
       ------------------------- */

    /** List of resolution strategies that will be applied to the Asciidoctorj group of dependencies.
     *
     * If called on a task, the project extensions's resolution strategies are returned ahead of the task-specific
     * resolution strategies.
     *
     * If called on a task where {@link #clearResolutionStrategies} was called previously, only the task-specifc
     * resolution strategies are returned.
     *
     * @return List of actions. Can be empty, but never {@code null}.
     *
     * @since 3.1.0
     */
    Iterable<Action<ResolutionStrategy>> getResolutionStrategies() {
        if (task) {
            if (onlyTaskResolutionStrategies) {
                this.resolutionsStrategies
            } else {
                extFromProject.resolutionsStrategies + this.resolutionsStrategies
            }
        } else {
            this.resolutionsStrategies
        }
    }

    /** Clears the current list of resolution strategies.
     *
     * If called on a task extension, all subsequent strategies added to the project extension will be ignored
     * in task context.
     */
    void clearResolutionStrategies() {
        if (task) {
            onlyTaskResolutionStrategies = true
        }
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

    /* -------------------------
       tag::extension-property[]
        onConfiguration:: Additional actions to be performed when the detached configuration for the
        {asciidoctorj-name} is created.
       end::extension-property[]
       ------------------------- */

    /** List of callbacks that will be applied whent he asciidoctorj-related detached configuration is created.
     *
     * If called on a task, the project extensions's callbacks are returned ahead of the task-specific
     * callbacks.
     *
     * If called on a task where {@link #clearConfigurationCallbacks} was called previously, only the task-specifc
     * callbacks are returned.
     *
     * @return List of callbacks. Can be empty, but never {@code null}.
     *
     * @since 3.1.0
     */
    Iterable<Action<Configuration>> getConfigurationCallbacks() {
        if (task) {
            if (onlyTaskConfigurationCallbacks) {
                this.configurationCallbacks
            } else {
                extFromProject.configurationCallbacks + this.configurationCallbacks
            }
        } else {
            this.configurationCallbacks
        }
    }

    /** Clears the current list of resolution strategies.
     *
     * If called on a task extension, all subsequent callbacks added to the project extension will be ignored
     * in task context.
     *
     * @since 3.1.0
     */
    void clearConfigurationCallbacks() {
        if (task) {
            onlyTaskConfigurationCallbacks = true
        }

        this.configurationCallbacks.clear()
    }

    /** Adds a callback for when the asciidoctorj-related detached configuration is created.
     *
     * @param callback The detached configuration is passed to this {@code Action}.
     *
     * @since 3.1.0
     */
    void onConfiguration(Action<Configuration> callback) {
        this.configurationCallbacks.add(callback)
    }

    /** Adds a callback for when the asciidoctorj-related detached configuration is created.
     *
     * @param callback The detached configuration is passed to this closure}.
     *
     * @since 3.1.0
     */
    void onConfiguration(@DelegatesTo(Configuration) Closure callback) {
        this.configurationCallbacks.add(callback as Action<Configuration>)
    }

    /* -------------------------
       tag::extension-property[]
       version:: {asciidoctorj-name} version. If not specified a sane default version will be used.
       end::extension-property[]
       ------------------------- */

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

    /** Whether the Guava JAR that ships with the Gradle distribution should be injected into the
     * classpath for external AsciidoctorJ processes.
     *
     * If not set previously via {@link #setInjectInternalGuavaJar} then a default version depending of the version of
     * the Gradle distribution will be used.
     *
     * @return {@code true} if JAR should be injected.
     */
    boolean getInjectInternalGuavaJar() {
        if (task) {
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

    /** Returns a runConfiguration of the configured AsciidoctorJ dependencies.
     *
     * @return A non-attached runConfiguration.
     */
    Configuration getConfiguration() {
        final String gDslVer = finalGroovyDslVersion
        final String pdfVer = finalPdfVersion
        final String epubVer = finalEpubVersion
        final String leanpubVer = finalLeanpubVersion
        final String diagramVer = finalDiagramVersion
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

        if (leanpubVer != null) {
            deps.add(createDependency("${ASCIIDOCTORJ_LEANPUB_DEPENDENCY}:${leanpubVer}"))
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

        resolutionStrategies.each {
            configuration.resolutionStrategy(it)
        }

        configurationCallbacks.each {
            it.execute(configuration)
        }

        configuration
    }

    private Dependency createDependency(final String notation, final Closure configurator = null) {
        if (configurator) {
            project.dependencies.create(notation, configurator)
        } else {
            project.dependencies.create(notation)
        }
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
        if (modules.groovyDsl.version == null) {
            modules.groovyDsl.use()
        }
    }

    /** Adds extensions to the existing container.
     *
     * Also sets the Groovy DSL version if required.
     *
     * @param newExtensions List of new extensions to add
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
        Transform.toList(exts) {
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
        Transform.toList(patterns) {
            (Pattern) (it instanceof Pattern ? it : ~/${stringize(it)}/)
        }
    }

    @CompileDynamic
    @SuppressWarnings('DuplicateStringLiteral')
    private Closure excludeTransitiveAsciidoctorJ() {
        return {
            exclude(group: ASCIIDOCTORJ_GROUP, module: 'asciidoctorj')
            exclude(group: ASCIIDOCTORJ_GROUP, module: 'asciidoctorj-api')
        }
    }

    private String getFinalGroovyDslVersion() {
        if (task) {
            this.modules.groovyDsl.version ?: extFromProject.modules.groovyDsl.version
        } else {
            extFromProject.modules.groovyDsl.version
        }
    }

    private String getFinalPdfVersion() {
        if (task) {
            this.modules.pdf.version ?: extFromProject.modules.pdf.version
        } else {
            extFromProject.modules.pdf.version
        }
    }

    private String getFinalEpubVersion() {
        if (task) {
            this.modules.epub.version ?: extFromProject.modules.epub.version
        } else {
            extFromProject.modules.epub.version
        }
    }

    private String getFinalLeanpubVersion() {
        if (task) {
            this.modules.leanpub.version ?: extFromProject.modules.leanpub.version
        } else {
            extFromProject.modules.leanpub.version
        }
    }

    private String getFinalDiagramVersion() {
        if (task) {
            this.modules.diagram.version ?: extFromProject.modules.diagram.version
        } else {
            extFromProject.modules.diagram.version
        }
    }
}
