/*
 * Copyright 2013-2024 the original author or authors.
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
import org.asciidoctor.gradle.internal.DefaultAsciidoctorJModules
import org.asciidoctor.gradle.internal.JavaExecUtils
@java.lang.SuppressWarnings('NoWildcardImports')
import org.gradle.api.*
@java.lang.SuppressWarnings('NoWildcardImports')
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant.api.core.LegacyLevel

import java.util.concurrent.Callable
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function
import java.util.regex.Pattern

import static groovy.lang.Closure.DELEGATE_FIRST
import static org.ysb33r.grolifant.api.core.ClosureUtils.configureItem

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
    private static final String CONFIGURATION_NAME = "__\$\$${NAME}\$\$__"

    @SuppressWarnings(['SpaceAfterOpeningBrace', 'SpaceBeforeClosingBrace'])
    private static final Closure EMPTY_CONFIGURATOR = {}

    private static final BiConsumer<DependencyResolveDetails, Callable<String>> DRD_VERSION_RESOLVER = {
        DependencyResolveDetails drd, Callable<String> versionResolver ->
            drd.useVersion(versionResolver.call())
    } as BiConsumer<DependencyResolveDetails, Callable<String>>

    private final LogLevel defaultLogLevel
    private final Map<String, Object> options = [:]
    private final List<Object> jrubyRequires = []
    private final List<Object> asciidoctorExtensions = []
    private final List<Object> warningsAsErrors = []
    private final DefaultAsciidoctorJModules modules
    private final Configuration publicConfiguration
    private final Configuration privateConfiguration
    private final BiFunction<String, Closure, Dependency> dependencyCreator
    private final Function<Project, Dependency> projectDependency
    private Object version
    private Optional<Object> jrubyVersion
    private boolean onlyTaskOptions = false
    private boolean onlyTaskExtensions = false
    private boolean onlyTaskWarnings = false
    private LogLevel logLevel
    private boolean onlyTaskRequires = false

    /** Attach extension to a project.
     *
     * @param project
     */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    AsciidoctorJExtension(Project project) {
        super(project, 'asciidoctorj-extension')

        String privateName = "${CONFIGURATION_NAME}_d"
        String publicName = "${CONFIGURATION_NAME}_r"
        projectOperations.configurations.createLocalRoleFocusedConfiguration(privateName, publicName)
        this.privateConfiguration = project.configurations.getByName(privateName)
        this.publicConfiguration = project.configurations.getByName(publicName)
        loadStandardPublicConfigurationResolutionStrategy()

        this.dependencyCreator = createDependencyLoader(project.dependencies, this.privateConfiguration)
        this.projectDependency = createProjectDependencyLoader(project.dependencies)
        this.version = defaultVersionMap[ASCIIDOCTOR_DEPENDENCY_PROPERTY_NAME]
        this.modules = new DefaultAsciidoctorJModules(projectOperations, this, defaultVersionMap)
        this.defaultLogLevel = project.logging.level
        if (this.version == null) {
            throw new ModuleNotFoundException('Default version for AsciidoctorJ must be defined. ' +
                    'Please report a bug at https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues'
            )
        }

        this.modules.onUpdate { owner.updateConfiguration() }
        updateConfiguration()
    }

    /** Attach extension to a task.
     *
     * @param task
     */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    AsciidoctorJExtension(Task task) {
        super(task, NAME)
        String privateName = "__\$\$${NAME}_${task.name}\$\$__d"
        String publicName = "__\$\$${NAME}_${task.name}\$\$__r"
        projectOperations.configurations.createLocalRoleFocusedConfiguration(privateName, publicName)
        this.privateConfiguration = task.project.configurations.getByName(privateName)
        this.publicConfiguration = task.project.configurations.getByName(publicName)
        loadStandardPublicConfigurationResolutionStrategy()

        Configuration projectConfiguration = task.project.configurations.findByName("${CONFIGURATION_NAME}_d")
        if (projectConfiguration) {
            this.publicConfiguration.extendsFrom(projectConfiguration)
        }

        this.dependencyCreator = createDependencyLoader(task.project.dependencies, this.privateConfiguration)
        this.projectDependency = createProjectDependencyLoader(task.project.dependencies)
        this.modules = new DefaultAsciidoctorJModules(projectOperations, this, defaultVersionMap)
        this.modules.onUpdate { owner.updateConfiguration() }
        updateConfiguration()
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

    /**
     * Defines extensions to be registered. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     * <p>
     *    If you use this method, then Gradle JARs will be leaked on to the classpath. This might not be what you
     *    want.
     * <p>
     * @param Closures of Asciidoctor Groovy DSL extensions.
     *
     * @since 4.0.0
     */
    void docExtensions(Closure<?>... exts) {
        if (LegacyLevel.PRE_8_4) {
            addExtensions(exts as List<Object>)
        } else {
            throw new GradleException(
                    'Closures are not supported on Gradle 8.4+ due to Gradle instrumentation issues. ' +
                            'Place the content in a string or load from it from a file instead.'
            )
        }
    }

    /**
     * Defines extensions to be registered. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     *
     * @param Files of Groovy code of Asciidoctor Groovy DSL extensions.
     *
     * @since 4.0.0
     */
    void docExtensions(File... exts) {
        addExtensions(exts as List<Object>)
    }

    /**
     * Defines extensions to be registered. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     *
     * @param Strings of Groovy code of Asciidoctor Groovy DSL extensions.
     *
     * @since 4.0.0
     */
    void docExtensions(String... exts) {
        addExtensions(exts as List<Object>)
    }

    /**
     * Defines extensions to be registered. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     *
     * @param Provider to strings or files of Groovy code of Asciidoctor Groovy DSL extensions
     *
     * @since 4.0.0
     */
    void docExtensions(Provider<?>... exts) {
        addExtensions(exts as List<Object>)
    }

    /**
     * Defines extensions to be registered. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     *
     * <p>
     *     THis method is specifically useful for project dependencies.
     * </p>
     * @param Dependencies containing Asciidoctor extensions
     *
     * @since 4.0.0
     */
    void docExtensions(Dependency... exts) {
        addExtensions(exts as List<Object>)
    }

    /**
     * Defines extensions to be registered. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     *
     * <p>
     *     THis method is specifically useful for project dependencies.
     * </p>
     * @param Dependencies containing Asciidoctor extensions
     *
     * @since 4.0.0
     */
    void docExtensions(Project... exts) {
        addExtensions(exts as List<Object>)
    }

    /**
     * Defines extensions to be registered. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     *
     * @param External dependency definitions using standard Gradle dependency notation.
     *
     * @since 4.0.0
     */
    void docExtensionsFromExternal(String... exts) {
        addExtensions(Transform.toList(exts as List) {
            dependencyCreator.apply(it.toString(), EMPTY_CONFIGURATOR)
        } as List<Object>)
    }

    /**
     * Clears the existing list of extensions and replace with a new set.
     *
     * If this is declared on a task extension all extension from the global
     * project extension will be ignored.
     *
     * @since 2.2.0
     */
    void setDocExtensions(Iterable<Object> newExtensions) {
        if (!LegacyLevel.PRE_8_4 && newExtensions.find { it instanceof Closure }) {
            throw new GradleException(
                    'Closures are no longer supported on Gradle 8.4+ due to Gradle instrumentation issues. ' +
                            'Place content in a string or load from it from a file instead.'
            )
        }
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
                projectOperations.stringTools.stringize(this.jrubyVersion.get())
            } else {
                extFromProject.getJrubyVersion()
            }
        } else {
            this.jrubyVersion?.present ? projectOperations.stringTools.stringize(this.jrubyVersion.get()) : null
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
            this.logLevel ?: this.defaultLogLevel
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
          See <<asciidoctorj-modules>> for which modules are supported.
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
        stringizeList(
                this.jrubyRequires,
                onlyTaskRequires,
                x -> ((AsciidoctorJExtension) x).requires
        ).toList()
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

    /**
     * Adds rules to the resolution strategy for resolving asciidoctorj related dependencies
     *
     * @param strategy Additional resolution strategy. Takes a {@link ResolutionStrategy} as parameter.
     */
    void resolutionStrategy(Action<ResolutionStrategy> strategy) {
        this.publicConfiguration.resolutionStrategy(strategy)
    }

    /**
     * Adds rules to the resolution strategy for resolving asciidoctorj related dependencies
     *
     * @param strategy Additional resolution strategy. Takes a {@link ResolutionStrategy} as parameter.
     */
    void resolutionStrategy(@DelegatesTo(ResolutionStrategy) Closure strategy) {
        Closure cfg = (Closure) strategy.clone()
        cfg.resolveStrategy = DELEGATE_FIRST
        this.publicConfiguration.resolutionStrategy(cfg)
    }

    /* -------------------------
       tag::extension-property[]
       version:: {asciidoctorj-name} version. If not specified a sane default version will be used.
       end::extension-property[]
       ------------------------- */

    /**
     * Version of AsciidoctorJ that should be used.
     *
     * @return Asciidoctor version
     */
    String getVersion() {
        if (task) {
            this.version ? projectOperations.stringTools.stringize(this.version) : extFromProject.getVersion()
        } else {
            projectOperations.stringTools.stringize(this.version)
        }
    }

    /** Set a new version to use.
     *
     * @param v New version to be used. Can be of anything that be be resolved by
     * {@link org.ysb33r.grolifant.api.core.StringTools#stringize ( Object o )}
     */
    void setVersion(Object v) {
        this.version = v
    }

    /**
     * Returns a runConfiguration of the configured AsciidoctorJ dependencies.
     *
     * @return Resolvable configuration.
     */
    Configuration getConfiguration() {
        this.publicConfiguration
    }

    /**
     * Loads a JRUBy resolution rule onto the given configuration.
     *
     * @param cfg Configuration.
     *
     * @since 4.0
     */
    void loadJRubyResolutionStrategy(Configuration cfg) {
        cfg.resolutionStrategy.eachDependency { DependencyResolveDetails dsr ->
            if (dsr.target.name == 'jruby' && dsr.target.group == 'org.jruby') {
                dsr.useTarget "${JRUBY_COMPLETE_DEPENDENCY}:${dsr.target.version}"
            }
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

    /**
     * Prepare extensions for serialisation.
     *
     * This takes care of dehydrating any closures.
     *
     * @param exts List of extensions
     * @return List of extensions suitable for serialization.
     */
    private List<Object> dehydrateExtensions(final List<Object> exts) {
        Transform.toList(exts) {
            switch (it) {
                case Closure:
                    ((Closure) it).dehydrate()
                    break
                case Project:
                    projectDependency.apply(((Project) it))
                    break
                default:
                    it
            }
        }
    }

    @SuppressWarnings(['UnusedPrivateMethodParameter', 'UnusedPrivateMethod'])
    private String minimumSafeJRubyVersion(final String asciidoctorjVersion) {
        '9.1.0.0'
    }

    @SuppressWarnings('Instanceof')
    private List<Pattern> patternize(final List<Object> patterns) {
        Transform.toList(patterns) {
            (Pattern) (it instanceof Pattern ? it : ~/${projectOperations.stringTools.stringize(it)}/)
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

    private void updateConfiguration() {
        final String gDslVer = finalGroovyDslVersion
        final String pdfVer = finalPdfVersion
        final String epubVer = finalEpubVersion
        final String leanpubVer = finalLeanpubVersion
        final String diagramVer = finalDiagramVersion

        loadDependencyRuleOnce(ASCIIDOCTORJ_CORE_DEPENDENCY) { -> owner.version }
        loadDependencyRuleOnce(JRUBY_COMPLETE_DEPENDENCY) { ->
            owner.getJrubyVersion() ?: owner.minimumSafeJRubyVersion(owner.getVersion())
        }

        if (gDslVer != null) {
            loadDependencyRuleOnce(ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY) { -> owner.finalGroovyDslVersion }
        }

        if (pdfVer != null) {
            loadDependencyRuleOnce(ASCIIDOCTORJ_PDF_DEPENDENCY) { -> owner.finalPdfVersion }
        }

        if (epubVer != null) {
            loadDependencyRuleOnce(ASCIIDOCTORJ_EPUB_DEPENDENCY) { -> owner.finalEpubVersion }
        }

        if (leanpubVer != null) {
            loadDependencyRuleOnce(ASCIIDOCTORJ_LEANPUB_DEPENDENCY) { -> owner.finalLeanpubVersion }
        }

        if (diagramVer != null) {
            loadDependencyRuleOnce(
                    ASCIIDOCTORJ_DIAGRAM_DEPENDENCY,
                    { -> owner.finalDiagramVersion },
                    excludeTransitiveAsciidoctorJ()
            )
        }
    }

    @SuppressWarnings('DuplicateNumberLiteral')
    private void loadDependencyRuleOnce(
            final String coords,
            Callable<String> versionResolver,
            @DelegatesTo(ExternalModuleDependency) Closure configurator = null
    ) {
        final parts = coords.split(':', 2)

        if (parts.size() != 2) {
            throw new ModuleNotFoundException("'${coords}' is not a valid group:name format")
        }

        if (!this.privateConfiguration.dependencies.find {
            it.group == parts[0] && it.name == parts[1]
        }) {
            final initialVersion = versionResolver.call()
            dependencyCreator.apply("${coords}:${initialVersion}".toString(), configurator)
            this.publicConfiguration.resolutionStrategy { ResolutionStrategy rs ->
                rs.eachDependency { drd ->
                    if (drd.requested.group == parts[0] && drd.requested.name == parts[1]) {
                        DRD_VERSION_RESOLVER.accept(drd, versionResolver)
                    }
                }
            }
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

    private void loadStandardPublicConfigurationResolutionStrategy() {
        loadJRubyResolutionStrategy(publicConfiguration)
    }

    private BiFunction<String, Closure, Dependency> createDependencyLoader(DependencyHandler deps, Configuration cfg) {
        { String cfgName, String coords, Closure configurator ->
            if (configurator != null) {
                deps.add(cfgName, coords, configurator)
            } else {
                deps.add(cfgName, coords)
            }
        }.curry(cfg.name) as BiFunction<String, Closure, Dependency>
    }

    private Function<Project, Dependency> createProjectDependencyLoader(DependencyHandler deps) {
        { Project p ->
            deps.project(path: p.path)
        } as Function<Project, Dependency>
    }
}
