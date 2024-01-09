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

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorAttributeProvider
import org.asciidoctor.gradle.base.AsciidoctorTaskBaseDirConfiguration
import org.asciidoctor.gradle.base.AsciidoctorTaskFileOperations
import org.asciidoctor.gradle.base.AsciidoctorTaskMethods
import org.asciidoctor.gradle.base.AsciidoctorTaskOutputOptions
import org.asciidoctor.gradle.base.AsciidoctorTaskWorkspacePreparation
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.base.internal.DefaultAsciidoctorBaseDirConfiguration
import org.asciidoctor.gradle.base.internal.DefaultAsciidoctorFileOperations
import org.asciidoctor.gradle.base.internal.DefaultAsciidoctorOutputOptions
import org.asciidoctor.gradle.base.internal.DefaultAsciidoctorWorkspacePreparation
import org.asciidoctor.gradle.base.internal.Workspace
import org.asciidoctor.gradle.base.log.Severity
import org.asciidoctor.gradle.base.process.ProcessMode
import org.asciidoctor.gradle.internal.AsciidoctorExecutorFactory
import org.asciidoctor.gradle.internal.AsciidoctorWorkerParameterFactory
import org.asciidoctor.gradle.internal.AsciidoctorWorkerParameters
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.internal.ExecutorUtils
import org.asciidoctor.gradle.internal.JavaExecUtils
import org.asciidoctor.gradle.remote.AsciidoctorJavaExec
import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.process.JavaForkOptions
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.core.LegacyLevel
import org.ysb33r.grolifant.api.core.jvm.ExecutionMode
import org.ysb33r.grolifant.api.core.jvm.JavaForkOptionsWithEnvProvider
import org.ysb33r.grolifant.api.core.jvm.worker.WorkerAppParameterFactory
import org.ysb33r.grolifant.api.core.runnable.AbstractJvmModelExecTask
import org.ysb33r.grolifant.api.remote.worker.WorkerAppExecutorFactory

import java.util.function.Function

import static org.asciidoctor.gradle.base.AsciidoctorUtils.getClassLocation
import static org.asciidoctor.gradle.base.internal.AsciidoctorAttributes.evaluateProviders
import static org.asciidoctor.gradle.base.internal.AsciidoctorAttributes.prepareAttributes
import static org.asciidoctor.gradle.base.internal.AsciidoctorAttributes.resolveAsCacheable
import static org.asciidoctor.gradle.base.internal.AsciidoctorAttributes.resolveAsSerializable
import static org.asciidoctor.gradle.internal.JavaExecUtils.getExecConfigurationDataFile
import static org.asciidoctor.gradle.internal.JavaExecUtils.getInternalGradleLibraryLocation
import static org.gradle.api.tasks.PathSensitivity.RELATIVE

/**
 * Base class for all AsciidoctorJ tasks.
 *
 * @author Schalk W. Cronjé
 * @author Manuel Prinz
 *
 * @since 2.0.0
 */
@CompileStatic
@SuppressWarnings('MethodCount')
class AbstractAsciidoctorTask extends AbstractJvmModelExecTask<AsciidoctorJvmExecSpec, AsciidoctorWorkerParameters>
        implements AsciidoctorTaskMethods {

    public final static ExecutionMode CLASSPATH = ExecutionMode.CLASSPATH
    public final static ExecutionMode IN_PROCESS = ExecutionMode.CLASSPATH
    public final static ExecutionMode OUT_OF_PROCESS = ExecutionMode.OUT_OF_PROCESS
    public final static ExecutionMode JAVA_EXEC = ExecutionMode.JAVA_EXEC

    public final static Severity FATAL = Severity.FATAL
    public final static Severity ERROR = Severity.ERROR
    public final static Severity WARN = Severity.WARN
    public final static Severity INFO = Severity.INFO

    protected final AsciidoctorJExtension asciidoctorj
    private ExecutionMode inProcess
    private Severity failureLevel = Severity.FATAL
    private final List<Object> asciidocConfigurations = []
    private final File rootDir
    private final File projectDir
    private final File execConfigurationDataFile
    private final Function<List<Dependency>, Configuration> detachedConfigurationCreator
    private final Property<FileCollection> jvmClasspath
    private final List<Provider<File>> gemJarProviders = []

    @Delegate
    private final DefaultAsciidoctorFileOperations asciidoctorTaskFileOperations

    @Delegate
    private final AsciidoctorTaskWorkspacePreparation workspacePreparation

    @Delegate
    private final DefaultAsciidoctorOutputOptions asciidoctorOutputOptions

    @Delegate
    private final AsciidoctorTaskBaseDirConfiguration baseDirConfiguration

    /** Set how AsciidoctorJ should be run.
     *
     * @param mode {@link #IN_PROCESS}, {@link #OUT_OF_PROCESS} or {@link #JAVA_EXEC}.
     *
     * @deprecated Use {@link #setExecutionMode} instead.
     */
    @Deprecated
    void setInProcess(ProcessMode mode) {
        logger.warn "Use 'setExecutionMode' instead of 'setInProcess(ProcessMode)'"
        executionMode = mode.executionMode
    }

    /** Set how AsciidoctorJ should be run.
     *
     * @param mode Case-insensitive string from of {@link #IN_PROCESS}, {@link #OUT_OF_PROCESS} or {@link #JAVA_EXEC}.
     *
     * @since 3.0
     *
     * @deprecated Use {@link #setExecutionMode} instead.
     */
    @Deprecated
    void setInProcess(String mode) {
        logger.warn "Use 'setExecutionMode' instead of 'setInProcess(String)'"
        executionMode = ProcessMode.valueOf(mode.toUpperCase(Locale.US)).executionMode
    }

    /** Set the minimum logging level that will fail the task.
     *
     * @param severity {@link #FATAL}, {@link #ERROR} or {@link #WARN} or {@link #INFO}.
     */
    void setFailureLevel(Severity severity) {
        this.failureLevel = severity
    }

    /** Set the minimum logging level that will fail the task.
     *
     * @param severity Case-insensitive string from of {@link #FATAL}, {@link #ERROR} or {@link #WARN} or {@link #INFO}.
     */
    void setFailureLevel(String severity) {
        this.failureLevel = Severity.valueOf(severity.toUpperCase(Locale.US))
    }

    /** Get the minimum logging level that will fail the task.
     *
     * Valid options are {@link #FATAL}, {@link #ERROR} or {@link #WARN} or {@link #INFO}.
     * The default mode is {@link #FATAL}.
     */
    @Internal
    Severity getFailureLevel() {
        this.failureLevel
    }

    /** Set the mode for running conversions sequential or in parallel.
     * For instance a task that has multiple backends can have the
     * conversion in parallel.
     *
     * When running sequential, the worker classloader, Asciidoctor instances
     * and Asciidoctor docExtensions will be shared across all of the conversions.
     * When running parallel each conversion will be in a separate classloader,
     * with a new Asciidoctor instance being initialised for every conversion.
     *
     * Sequential work might execute slightly faster, but if you have backend-specific
     * docExtensions you might want to consider parallel mode (or use another Asciidoctor
     * task instance).
     *
     * Default is parallel.
     *
     * When {@link #inProcess} {@code ==} {@link #JAVA_EXEC} this option is ignored.
     */
    // TODO: parallelMode is currently ignored in 4.0
    @Internal
    boolean parallelMode = true

    /** Set fork options for {@link #JAVA_EXEC} and {@link #OUT_OF_PROCESS} modes.
     *
     * These options are ignored if {@link #inProcess} {@code ==} {@link #IN_PROCESS}.
     *
     * @param configurator Closure that configures a {@link JavaForkOptions} instance.
     *
     * @deprecated Use {@link #jvm} instead.
     */
    @Deprecated
    void forkOptions(@DelegatesTo(JavaForkOptionsWithEnvProvider) Closure configurator) {
        jvm(configurator)
    }

    /** Set fork options for {@link #JAVA_EXEC} and {@link #OUT_OF_PROCESS} modes.
     *
     * These options are ignored if {@link #inProcess} {@code ==} {@link #IN_PROCESS}.
     *
     * @param configurator Action that configures a {@link JavaForkOptions} instance.
     *
     * @deprecated Use {@link #jvm} instead.
     */
    @Deprecated
    void forkOptions(Action<JavaForkOptionsWithEnvProvider> configurator) {
        jvm(configurator)
    }

    /** Returns all of the Asciidoctor options.
     *
     * This is equivalent of using {@code asciidoctorj.getOptions}
     *
     */
    @Input
    Map<String, Object> getOptions() {
        resolveAsCacheable(asciidoctorj.options, projectOperations)
    }

    /** Apply a new set of Asciidoctor options, clearing any options previously set.
     *
     * If set here all global Asciidoctor options are ignored within this task.
     *
     * This is equivalent of using {@code asciidoctorj.setOptions}.
     *
     * @param m Map with new options
     */
    void setOptions(Map m) {
        asciidoctorj.options = m
    }

    /** Add additional asciidoctor options
     *
     * If set here these options will be used in addition to any global Asciidoctor options.
     *
     * This is equivalent of using {@code asciidoctorj.options}.
     *
     * @param m Map with new options
     */
    void options(Map m) {
        asciidoctorj.options(m)
    }

    /** Returns all of the Asciidoctor options.
     *
     * This is equivalent of using {@code asciidoctorj.getAttributes}
     *
     */
    @Input
    Map<String, Object> getAttributes() {
        resolveAsCacheable(asciidoctorj.attributes, projectOperations)
    }

    /** Apply a new set of Asciidoctor options, clearing any options previously set.
     *
     * If set here all global Asciidoctor options are ignored within this task.
     *
     * This is equivalent of using {@code asciidoctorj.setAttributes}.
     *
     * @param m Map with new options
     */
    void setAttributes(Map m) {
        asciidoctorj.attributes = m
    }

    /** Add additional asciidoctor options
     *
     * If set here these options will be used in addition to any global Asciidoctor options.
     *
     * This is equivalent of using {@code asciidoctorj.attributes}.
     *
     * @param m Map with new options
     */
    void attributes(Map m) {
        asciidoctorj.attributes(m)
    }

    /** Additional providers of attributes.
     *
     * NOTE: Attributes added via providers do no change the up-to-date status of the task.
     *   Providers are therefore useful to add attributes such as build time.
     *
     * @return List of attribute providers.
     */
    @Internal
    List<AsciidoctorAttributeProvider> getAttributeProviders() {
        asciidoctorj.attributeProviders
    }

    /** Returns all of the specified configurations as a collections of files.
     *
     * If any docExtensions are dependencies then they will be included here too.
     *
     * @return FileCollection
     */
    @Classpath
    @SuppressWarnings('Instanceof')
    FileCollection getConfigurations() {
        final precompiledExtensions = findDependenciesInExtensions()
        FileCollection fc = this.asciidocConfigurations.inject(asciidoctorj.configuration) {
            FileCollection seed, Object it ->
                seed + projectOperations.configurations.asConfiguration(it)
        }
        final gjp = projectOperations.fsOperations.files([gemJarProviders, fc])
        precompiledExtensions ? gjp + precompiledExtensions : gjp
    }

    /** Override any existing configurations except the ones available via the {@code asciidoctorj} task extension.
     *
     * @param configs
     */
    void setConfigurations(Iterable<Object> configs) {
        this.asciidocConfigurations.clear()
        configurations(configs)
    }

    /** Add additional configurations.
     *
     * @param configs Instances of {@link Configuration} or anything convertible to a string than can be used
     *   as a name of a runConfiguration.
     */
    void configurations(Iterable<Object> configs) {
        this.asciidocConfigurations.addAll(configs)
    }

    /** Add additional configurations.
     *
     * @param configs Instances of {@link Configuration} or anything convertible to a string than can be used
     *   as a name of a runConfiguration.
     */
    void configurations(Object... configs) {
        this.asciidocConfigurations.addAll(configs)
    }

    /** Configurations for which dependencies should be reported.
     *
     * @return Set of configurations. Can be empty, but never {@code null}.
     *
     * @since 2.3.0
     */
    @Override
    Set<Configuration> getReportableConfigurations() {
        ([asciidoctorj.configuration] + projectOperations.configurations.asConfigurations(asciidocConfigurations))
                .toSet()
    }

    /**
     * Adds a Jar of GEMs to the classpath.
     *
     * @param gemJar Provider to a {@link Jar} task which contain GEMs
     *
     * @since 4.0
     */
    void withGemJar(TaskProvider<Jar> gemJar) {
        dependsOn(gemJar)
        this.gemJarProviders.add(gemJar.map { it.archiveFile.get().asFile })
    }

    /**
     * Adds a Jar of GEMs to the classpath.
     *
     * @param gemJar name of a {@link Jar} task which contain GEMs.
     *
     * @since 4.0
     */
    void withGemJar(String taskName) {
        dependsOn(taskName)
        final gemJar = project.tasks.named(taskName, Jar)
        this.gemJarProviders.add(gemJar.map { it.archiveFile.get().asFile })
    }

    /**
     * Adds a directory to the classpath. THe directory will contain unpacked GEMs.
     *
     * @param gemPath A provider to a directory containing unpakcing GEMs.
     *
     * @param builtBy THe name of the task that prepares this directory.
     *
     * @since 4.0
     */
    void withGemPath(Provider<File> gemPath, String builtBy) {
        dependsOn(builtBy)
        this.gemJarProviders.add(gemPath)
    }

    /**
     * Sets the execution mode.
     * <p>
     * This allows for JVM tasks to be executed either on a worker queue inside the JVM using an isolated classpath,
     * ouside the JVM in a separate process, OR using a classic {@code javaexec}.
     *
     * <p>
     * If nothing is set, the default is {@link ExecutionMode#JAVA_EXEC}.
     *
     * @param em Execution mode.
     * @throw UnsupportedConfigurationException is mode is illegal within context.
     */
    @Override
    void setExecutionMode(ExecutionMode em) {
        super.setExecutionMode(em)
        inProcess = em

        if (em == JAVA_EXEC) {
            runnerSpec {
                setArgs([getExecConfigurationDataFile(this).absolutePath])
            }
        }
    }

    void setExecutionMode(String s) {
        executionMode = ExecutionMode.valueOf(s.toUpperCase(Locale.US))
    }

    @Override
    void exec() {
        checkForInvalidSourceDocuments()
        checkForIncompatiblePathRoots(baseDirStrategy)

        if (executionMode == JAVA_EXEC) {
            entrypoint {
                classpath(JavaExecUtils.getJavaExecClasspath(
                        projectOperations,
                        configurations
                ))
            }

            final mapping = prepareWorkspaceAndLoadExecutorConfigurations()

            JavaExecUtils.writeExecConfigurationData(
                    execConfigurationDataFile,
                    mapping.values().flatten() as List<ExecutorConfiguration>
            )
        } else {
            entrypoint {
                classpath(configurations)
            }
        }
        super.exec()
    }

    /** Initialises the core an Asciidoctor task
     *
     * @param we {@link WorkerExecutor}. This is usually injected into the
     *   constructor of the subclass.
     */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    protected AbstractAsciidoctorTask(WorkerExecutor we) {
        super(we)

        this.asciidoctorTaskFileOperations = new DefaultAsciidoctorFileOperations(this, 'AsciidoctorJ')
        this.workspacePreparation = new DefaultAsciidoctorWorkspacePreparation(
                projectOperations,
                this.asciidoctorTaskFileOperations,
                this.asciidoctorTaskFileOperations
        )
        this.asciidoctorOutputOptions = new DefaultAsciidoctorOutputOptions(
                projectOperations,
                name,
                asciidoctorTaskFileOperations
        )
        this.baseDirConfiguration = new DefaultAsciidoctorBaseDirConfiguration(project, this)
        this.asciidoctorj = extensions.create(AsciidoctorJExtension.NAME, AsciidoctorJExtension, this)
        this.projectDir = project.projectDir
        this.rootDir = project.rootDir
        this.jvmClasspath = project.objects.property(FileCollection)
        this.execConfigurationDataFile = getExecConfigurationDataFile(this)
        this.detachedConfigurationCreator = { ConfigurationContainer c, List<Dependency> deps ->
            final cfg = c.detachedConfiguration(deps.toArray() as Dependency[])
            cfg.canBeConsumed = false
            cfg.canBeResolved = true
            cfg
        }.curry(project.configurations) as Function<List<Dependency>, Configuration>

        inputs.files(this.asciidoctorj.configuration)
        inputs.files { gemJarProviders }.withPathSensitivity(RELATIVE)
        inputs.property 'backends', { -> backends() }
        inputs.property 'asciidoctorj-version', { -> asciidoctorj.version }
        inputs.property 'jruby-version', { -> asciidoctorj.jrubyVersion ?: '' }
        execSpec = new AsciidoctorJvmExecSpec(projectOperations)
        entrypoint {
            mainClass = AsciidoctorJavaExec.canonicalName
        }

        executionMode = IN_PROCESS
    }

    /**
     * The Ascidoctor execution mode on the JVM.
     *
     * @return THe configured execution mode.
     *
     * @since 4.0
     */
    @Internal
    protected ExecutionMode getExecutionMode() {
        this.inProcess
    }

    /**
     * Create a worker app executor factory.
     *
     * @return Instance of an execution factory.
     *
     * @since 4.0
     */
    @Override
    protected WorkerAppExecutorFactory<AsciidoctorWorkerParameters> createExecutorFactory() {
        new AsciidoctorExecutorFactory()
    }

    /**
     * Create a worker app parameter factory.
     *
     * @return Instance of a parameter factory.
     *
     * @since 4.0
     */
    @Override
    protected WorkerAppParameterFactory<AsciidoctorWorkerParameters> createParameterFactory() {
        new AsciidoctorWorkerParameterFactory({ ->
            owner.prepareWorkspaceAndLoadExecutorConfigurations()
        })
    }

    /**
     /** Returns all of the executor configurations for this task
     *
     *
     * @param workingSourceDir Working source directory
     * @param sourceFiles Source files
     * @param lang Language for which to create the exucutor configuration.
     *   Can be empty.
     * @return Executor configurations
     */
    protected Map<String, ExecutorConfiguration> getExecutorConfigurations(
            final File workingSourceDir,
            final Set<File> sourceFiles,
            Optional<String> lang
    ) {
        backends().collectEntries { String activeBackend ->
            [
                    "backend=${activeBackend}".toString(),
                    getExecutorConfigurationFor(activeBackend, workingSourceDir, sourceFiles, lang)
            ]
        }
    }

    /** Provides configuration information for the worker.
     *
     * @param backendName Name of backend that will be run.
     * @param workingSourceDir Source directory that will used for work. This can be
     *   the original source directory or an intermediate.
     * @param sourceFiles The actual top-level source files that will be used as entry points
     *   for generating documentation.
     * @return Executor configuration
     */
    @SuppressWarnings(['UnnecessaryGetter', 'LineLength'])
    protected ExecutorConfiguration getExecutorConfigurationFor(
            final String backendName,
            final File workingSourceDir,
            final Set<File> sourceFiles,
            Optional<String> lang
    ) {
        Optional<List<String>> copyResources = getCopyResourcesForBackends()
        new ExecutorConfiguration(
                sourceDir: workingSourceDir,
                sourceTree: sourceFiles,
                outputDir: lang.present ? getOutputDirForBackend(backendName, lang.get()) : getOutputDirForBackend(backendName),
                baseDir: lang.present ? getBaseDir(lang.get()) : getBaseDir(),
                projectDir: this.projectDir,
                rootDir: this.rootDir,
                options: resolveAsSerializable(evaluateProviders(options), projectOperations.stringTools),
                failureLevel: failureLevel.level,
                attributes: resolveAsSerializable(
                        preparePreserialisedAttributes(workingSourceDir, lang),
                        projectOperations.stringTools
                ),
                backendName: backendName,
                logDocuments: logDocuments,
                fatalMessagePatterns: asciidoctorj.fatalWarnings,
                asciidoctorExtensions: serializableAsciidoctorJExtensions,
                requires: asciidoctorj.requires,
                copyResources: copyResources.present &&
                        (copyResources.get().empty || backendName in copyResources.get()),
                executorLogLevel: ExecutorUtils.getExecutorLogLevel(asciidoctorj.logLevel),
                safeModeLevel: asciidoctorj.safeMode.level
        )
    }

    /** Returns all of the associated extensionRegistry.
     *
     * @return AsciidoctorJ extensionRegistry
     */
    @Internal
    protected List<Object> getAsciidoctorJExtensions() {
        asciidoctorj.docExtensions
    }

    @Nested
    protected AsciidoctorTaskFileOperations getAsciidoctorTaskFileOperations() {
        this.asciidoctorTaskFileOperations
    }

    @Nested
    protected AsciidoctorTaskWorkspacePreparation getWorkspacePreparation() {
        this.workspacePreparation
    }

    @Nested
    protected AsciidoctorTaskOutputOptions getAsciidoctorOutputOptions() {
        this.asciidoctorOutputOptions
    }

    @Nested
    protected AsciidoctorTaskBaseDirConfiguration getBaseDirConfiguration() {
        this.baseDirConfiguration
    }

    private static List<File> ifNoGroovyAddLocal(final List<Dependency> deps) {
        if (deps.find {
            it.name == 'groovy-all' || it.name == 'groovy'
        }) {
            []
        } else {
            [JavaExecUtils.localGroovy]
        }
    }

    private List<Object> getSerializableAsciidoctorJExtensions() {
        asciidoctorJExtensions.findAll { !(it instanceof Dependency) }.collect {
            getSerializableAsciidoctorJExtension(it)
        }
    }

    private Object getSerializableAsciidoctorJExtension(Object ext) {
        switch (ext) {
            case CharSequence:
                return projectOperations.stringTools.stringize(ext)
            case Provider:
                return getSerializableAsciidoctorJExtension(((Provider) ext).get())
            default:
                return ext
        }
    }

    private Map<String, Workspace> prepareWorkspacesByLanguage() {
        languagesAsOptionals.collectEntries { Optional<String> lang ->
            Workspace workspace = prepareWorkspace(lang)
            [lang.orElse(''), workspace]
        }
    }

    private Map<String, List<ExecutorConfiguration>> prepareWorkspaceAndLoadExecutorConfigurations() {
        final sourcesByLang = prepareWorkspacesByLanguage()
        final mapping = sourcesByLang.collectEntries { lang, workspace ->
            final byLang = Optional.ofNullable(lang)
            List<ExecutorConfiguration> loadedConfigurations = getExecutorConfigurations(
                    workspace.workingSourceDir,
                    workspace.sourceTree.files,
                    byLang
            ).values().toList()
            copyResourcesByExecutorConfiguration(loadedConfigurations, byLang)
            [lang, loadedConfigurations]
        } as Map<String, List<ExecutorConfiguration>>
        mapping
    }

    private List<Optional<String>> getLanguagesAsOptionals() {
        if (this.languages.empty) {
            [Optional.empty() as Optional<String>]
        } else {
            Transform.toList(this.languages) { String it ->
                Optional.of(it)
            }
        }
    }

    private void copyResourcesByExecutorConfiguration(
            Iterable<ExecutorConfiguration> executorConfigurations,
            Optional<String> lang
    ) {
        for (ExecutorConfiguration ec : executorConfigurations) {
            copyResourcesByExecutorConfiguration(ec, lang)
        }
    }

    private void copyResourcesByExecutorConfiguration(
            ExecutorConfiguration ec,
            Optional<String> lang
    ) {
        if (ec.copyResources) {
            copyResourcesByBackend(ec.backendName, ec.sourceDir, ec.outputDir, lang)
        }
    }

    @SuppressWarnings('Instanceof')
    private FileCollection findDependenciesInExtensions() {
        List<Dependency> deps = asciidoctorj.docExtensions.findAll {
            it instanceof Dependency
        } as List<Dependency>

        Set<File> closurePaths = Transform.toSet(findExtensionClosures()) {
            getClassLocation(it.class)
        }

        if (!closurePaths.empty) {
            handleGradleClosureInstrumentation(closurePaths)
            closurePaths.addAll(ifNoGroovyAddLocal(deps))
        }

        if (deps.empty && closurePaths.empty) {
            projectOperations.fsOperations.emptyFileCollection()
        } else if (closurePaths.empty) {
            jrubyLessConfiguration(deps)
        } else if (deps.empty) {
            projectOperations.fsOperations.files(closurePaths)
        } else {
            jrubyLessConfiguration(deps) + projectOperations.fsOperations.files(closurePaths)
        }
    }

    private void handleGradleClosureInstrumentation(Set<File> closurePaths) {
        // Jumping through hoops to make docExtensions based upon closures to work.
        closurePaths.add(getClassLocation(org.gradle.internal.scripts.ScriptOrigin))
        if (LegacyLevel.PRE_8_4 && !LegacyLevel.PRE_7_6) {
            closurePaths.add(getClassLocation(org.gradle.api.GradleException))
        }
        if (LegacyLevel.PRE_8_4 && !LegacyLevel.PRE_8_3) {
            closurePaths.add(getInternalGradleLibraryLocation(
                    projectOperations,
                    ~/gradle-internal-instrumentation-api-([\d.]+).jar/
            ))
            closurePaths.add(getInternalGradleLibraryLocation(
                    projectOperations,
                    ~/gradle-instrumentation-declarations-([\d.]+).jar/
            ))
        }
        if (!LegacyLevel.PRE_8_1 && LegacyLevel.PRE_8_4) {
            closurePaths.add(getClassLocation(kotlin.io.FilesKt))
            closurePaths.add(getClassLocation(org.gradle.internal.lazy.Lazy))
            closurePaths.add(getInternalGradleLibraryLocation(projectOperations, ~/fastutil-([\d.]+)-min.jar/))
        }
    }

    // TODO: Try to do this without a detached configuration
    private FileCollection jrubyLessConfiguration(List<Dependency> deps) {
        Configuration cfg = detachedConfigurationCreator.apply(deps)
        asciidoctorj.loadJRubyResolutionStrategy(cfg)
        cfg
    }

    private Map<String, Object> preparePreserialisedAttributes(final File workingSourceDir, Optional<String> lang) {
        prepareAttributes(
                projectOperations.stringTools,
                attributes,
                (lang.present ? asciidoctorj.getAttributesForLang(lang.get()) : [:]),
                getTaskSpecificDefaultAttributes(workingSourceDir) as Map<String, ?>,
                attributeProviders,
                lang
        )
    }

    private List<Closure> findExtensionClosures() {
        asciidoctorj.docExtensions.findAll {
            it instanceof Closure
        } as List<Closure>
    }

//    @SuppressWarnings('AbstractClassWithoutAbstractMethod')
//    abstract static class AsciidoctorJExecuterWorker implements WorkAction<Params> {
//        static interface Params extends WorkParameters {
//            ExecutorConfigurationContainer getExtensionConfigurationContainer()
//
//            void setExtensionConfigurationContainer(ExecutorConfigurationContainer container)
//        }
//
//        @Override
//        void execute() {
//            new AsciidoctorJExecuter(parameters.extensionConfigurationContainer).run()
//        }
//    }
}
