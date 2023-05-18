/*
 * Copyright 2013-2023 the original author or authors.
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
import groovy.transform.PackageScope
import org.asciidoctor.gradle.base.AbstractAsciidoctorBaseTask
import org.asciidoctor.gradle.base.AsciidoctorAttributeProvider
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.base.internal.Workspace
import org.asciidoctor.gradle.base.log.Severity
import org.asciidoctor.gradle.base.process.ProcessMode
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.internal.ExecutorConfigurationContainer
import org.asciidoctor.gradle.internal.ExecutorUtils
import org.asciidoctor.gradle.internal.JavaExecUtils
import org.asciidoctor.gradle.remote.AsciidoctorJExecuter
import org.asciidoctor.gradle.remote.AsciidoctorJavaExec
import org.asciidoctor.gradle.remote.AsciidoctorRemoteExecutionException
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec
import org.gradle.process.JavaForkOptions
import org.gradle.util.GradleVersion
import org.gradle.workers.ClassLoaderWorkerSpec
import org.gradle.workers.ProcessWorkerSpec
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor

import static org.asciidoctor.gradle.base.AsciidoctorUtils.executeDelegatingClosure
import static org.asciidoctor.gradle.base.AsciidoctorUtils.getClassLocation
import static org.asciidoctor.gradle.base.internal.AsciidoctorAttributes.resolveAsCacheable
import static org.asciidoctor.gradle.base.internal.AsciidoctorAttributes.resolveAsSerializable
import static org.asciidoctor.gradle.base.internal.ConfigurationUtils.asConfiguration
import static org.asciidoctor.gradle.base.internal.ConfigurationUtils.asConfigurations
import static org.gradle.api.tasks.PathSensitivity.RELATIVE
import static org.ysb33r.grolifant.api.core.LegacyLevel.PRE_6_4

/** Base class for all AsciidoctorJ tasks.
 *
 * @author Schalk W. Cronjé
 * @author Manuel Prinz
 *
 * @since 2.0.0
 */
@SuppressWarnings('MethodCount')
@CompileStatic
class AbstractAsciidoctorTask extends AbstractAsciidoctorBaseTask {

    public final static ProcessMode IN_PROCESS = ProcessMode.IN_PROCESS
    public final static ProcessMode OUT_OF_PROCESS = ProcessMode.OUT_OF_PROCESS
    public final static ProcessMode JAVA_EXEC = ProcessMode.JAVA_EXEC

    public final static Severity FATAL = Severity.FATAL
    public final static Severity ERROR = Severity.ERROR
    public final static Severity WARN = Severity.WARN
    public final static Severity INFO = Severity.INFO

    protected final static GradleVersion LAST_GRADLE_WITH_CLASSPATH_LEAKAGE = GradleVersion.version(('6.99'))

    protected final AsciidoctorJExtension asciidoctorj
    private ProcessMode inProcess = JAVA_EXEC
    private Severity failureLevel = Severity.FATAL
    private final WorkerExecutor worker
    private final List<Object> asciidocConfigurations = []

    @PackageScope
    final org.ysb33r.grolifant.api.v4.JavaForkOptions javaForkOptions =
            new org.ysb33r.grolifant.api.v4.JavaForkOptions()

    /** Set how AsciidoctorJ should be run.
     *
     * @param mode {@link #IN_PROCESS}, {@link #OUT_OF_PROCESS} or {@link #JAVA_EXEC}.
     */
    void setInProcess(ProcessMode mode) {
        this.inProcess = mode
    }

    /** Set how AsciidoctorJ should be run.
     *
     * @param mode Case-insensitive string from of {@link #IN_PROCESS}, {@link #OUT_OF_PROCESS} or {@link #JAVA_EXEC}.
     *
     * @since 3.0
     */
    void setInProcess(String mode) {
        this.inProcess = ProcessMode.valueOf(mode.toUpperCase(Locale.US))
    }

    /** Run Asciidoctor conversions in or out of process
     *
     * Valid options are {@link #IN_PROCESS}, {@link #OUT_OF_PROCESS} and {@link #JAVA_EXEC}.
     * The default mode is {@link #JAVA_EXEC}.
     */
    @Internal
    ProcessMode getInProcess() {
        this.inProcess
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
    @Internal
    boolean parallelMode = true

    /** Set fork options for {@link #JAVA_EXEC} and {@link #OUT_OF_PROCESS} modes.
     *
     * These options are ignored if {@link #inProcess} {@code ==} {@link #IN_PROCESS}.
     *
     * @param configurator Closure that configures a {@link org.ysb33r.grolifant.api.v4.JavaForkOptions} instance.
     */
    void forkOptions(@DelegatesTo(org.ysb33r.grolifant.api.v4.JavaForkOptions) Closure configurator) {
        executeDelegatingClosure(this.javaForkOptions, configurator)
    }

    /** Set fork options for {@link #JAVA_EXEC} and {@link #OUT_OF_PROCESS} modes.
     *
     * These options are ignored if {@link #inProcess} {@code ==} {@link #IN_PROCESS}.
     *
     * @param configurator Action that configures a {@link org.ysb33r.grolifant.api.v4.JavaForkOptions} instance.
     */
    void forkOptions(Action<org.ysb33r.grolifant.api.v4.JavaForkOptions> configurator) {
        configurator.execute(this.javaForkOptions)
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
        FileCollection precompiledExtensions = findDependenciesInExtensions()
        FileCollection fc = this.asciidocConfigurations.inject(asciidoctorj.configuration) {
            FileCollection seed, Object it ->
                seed + asConfiguration(project, it)
        }
        precompiledExtensions ? fc + precompiledExtensions : fc
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
        ([asciidoctorj.configuration] + asConfigurations(project, asciidocConfigurations)).toSet()
    }

    /**
     * Additional locations to consider when GEMs are loaded by AsciidoctorJ or JRuby.
     *
     * @return The additional GEM locations.
     *
     * @since 4.0
     */
    @Internal
    String getGemPath() {
        asciidoctorj.asGemPath()
    }

    @SuppressWarnings('UnnecessaryGetter')
    @TaskAction
    void processAsciidocSources() {
        validateConditions()

        languagesAsOptionals.each { Optional<String> lang ->
            Workspace workspace = lang.present ? prepareWorkspace(lang.get()) : prepareWorkspace()
            Set<File> sourceFiles = workspace.sourceTree.files

            if (finalProcessMode != JAVA_EXEC) {
                runWithWorkers(
                        workspace.workingSourceDir,
                        sourceFiles,
                        lang
                )
            } else {
                runWithJavaExec(
                        workspace.workingSourceDir,
                        sourceFiles,
                        lang
                )
            }
        }
    }

    /** Initialises the core an Asciidoctor task
     *
     * @param we {@link WorkerExecutor}. This is usually injected into the
     *   constructor of the subclass.
     */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    protected AbstractAsciidoctorTask(WorkerExecutor we) {
        super()
        this.worker = we
        this.asciidoctorj = extensions.create(AsciidoctorJExtension.NAME, AsciidoctorJExtension, this)

        addInputProperty 'required-ruby-modules', { AsciidoctorJExtension aj -> aj.requires }
                .curry(this.asciidoctorj)

        addInputProperty 'asciidoctor-version', { AsciidoctorJExtension aj -> aj.version }
                .curry(this.asciidoctorj)

        addOptionalInputProperty 'jruby-version', { AsciidoctorJExtension aj -> aj.jrubyVersion }
                .curry(this.asciidoctorj)

        inputs.files { asciidoctorj.gemPaths }
                .withPathSensitivity(RELATIVE)
    }

    /** Name of implementation engine.
     *
     * @return Always{@code AsciidoctorJ}
     */
    @Override
    @Internal
    protected String getEngineName() {
        'AsciidoctorJ'
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
        configuredOutputOptions.backends.collectEntries { String activeBackend ->
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
    @SuppressWarnings('UnnecessaryGetter')
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
                outputDir: lang.present ? getOutputDirFor(backendName, lang.get()) : getOutputDirFor(backendName),
                baseDir: lang.present ? getBaseDir(lang.get()) : getBaseDir(),
                projectDir: project.projectDir,
                rootDir: project.rootProject.projectDir,
                options: resolveAsSerializable(evaluateProviders(options), projectOperations.stringTools),
                failureLevel: failureLevel.level,
                attributes: resolveAsSerializable(
                        preparePreserialisedAttributes(workingSourceDir, lang),
                        projectOperations.stringTools
                ),
                backendName: backendName,
                logDocuments: logDocuments,
                gemPath: gemPath,
                fatalMessagePatterns: asciidoctorj.fatalWarnings,
                asciidoctorExtensions: (asciidoctorJExtensions.findAll { !(it instanceof Dependency) }),
                requires: requires,
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

    /** Configure Java fork options prior to execution.
     *
     * The default method will copy anything configured via {@link #forkOptions(Closure c)} or
     * {@link #forkOptions(Action c)} to the provided {@link JavaForkOptions}.
     *
     * @param pfo Fork options to be configured.
     */
    protected void configureForkOptions(JavaForkOptions pfo) {
        this.javaForkOptions.copyTo(pfo)
    }

    /** Allow a task to enhance additional '{@code requires}'
     *
     * The default implementation will add a special script to deal with verbose mode.
     *
     * @return The final set of '{@code requires}'
     */
    @Internal
    protected List<String> getRequires() {
        asciidoctorj.requires
    }

    /** Selects a final process mode.
     *
     * Some incompatibilities can cause certain process mode to fail given a combination of factors.
     *
     * Task implementations can override this method to select a safe process mode, than the one provided by the
     * build script author. The default implementation will simply return whatever what was configured, except in the
     * case for Gradle 4.3 or older in which case it will always return {@link #JAVA_EXEC}.
     *
     * @return Process mode to use for execution.
     */
    @Internal
    protected ProcessMode getFinalProcessMode() {
        if (inProcess != JAVA_EXEC && GradleVersion.current() < GradleVersion.version(('4.3'))) {
            logger.warn('Gradle API classpath leakage will cause issues with Gradle < 4.3. ' +
                    'Switching to JAVA_EXEC instead.')
            JAVA_EXEC
        } else {
            this.inProcess
        }
    }

    private Map<String, ExecutorConfiguration> runWithWorkers(
            final File workingSourceDir,
            final Set<File> sourceFiles,
            Optional<String> lang
    ) {
        FileCollection asciidoctorClasspath = configurations
        logger.info "Running AsciidoctorJ with workers. Classpath = ${asciidoctorClasspath.files}"

        Map<String, ExecutorConfiguration> executorConfigurations = getExecutorConfigurations(
                workingSourceDir,
                sourceFiles,
                lang
        )

        if (parallelMode) {
            WorkQueue queue = getWorkQueue(asciidoctorClasspath)
            executorConfigurations.each { String configName, ExecutorConfiguration executorConfiguration ->
                copyResourcesByBackend(executorConfiguration, lang)
                queue.submit(AsciidoctorJExecuterWorker) { params ->
                    params.extensionConfigurationContainer =
                        new ExecutorConfigurationContainer(executorConfiguration)
                }
            }
        } else {
            copyResourcesByBackend(executorConfigurations.values(), lang)
            getWorkQueue(asciidoctorClasspath).submit(AsciidoctorJExecuterWorker) { params ->
                params.extensionConfigurationContainer =
                    new ExecutorConfigurationContainer(executorConfigurations.values())
            }
        }
        executorConfigurations
    }

    private WorkQueue getWorkQueue(FileCollection asciidoctorClasspath) {
        IN_PROCESS ?
            worker.classLoaderIsolation(configureClassloaderIsolatedWorker(asciidoctorClasspath)) :
            worker.processIsolation(configureProcessIsolatedWorker(asciidoctorClasspath))
    }

    private Closure configureClassloaderIsolatedWorker(FileCollection asciidoctorClasspath) {
        return { ClassLoaderWorkerSpec spec ->
            spec.classpath.from(asciidoctorClasspath)
        }
    }

    private Closure configureProcessIsolatedWorker(FileCollection asciidoctorClasspath) {
        return { ProcessWorkerSpec spec ->
            spec.classpath.from(asciidoctorClasspath)
            configureForkOptions(spec.forkOptions)
        }
    }

    private Map<String, ExecutorConfiguration> runWithJavaExec(
            final File workingSourceDir,
            final Set<File> sourceFiles,
            Optional<String> lang
    ) {
        FileCollection javaExecClasspath = JavaExecUtils.getJavaExecClasspath(
                project,
                configurations,
                asciidoctorj.injectInternalGuavaJar
        )
        Map<String, ExecutorConfiguration> executorConfigurations = getExecutorConfigurations(
                workingSourceDir,
                sourceFiles,
                lang
        )
        File execConfigurationData = JavaExecUtils.writeExecConfigurationData(this, executorConfigurations.values())
        copyResourcesByBackend(executorConfigurations.values(), lang)

        logger.debug("Serialised AsciidoctorJ configuration to ${execConfigurationData}")
        logger.info "Running AsciidoctorJ instance with classpath ${javaExecClasspath.files}"

        try {
            project.javaexec { JavaExecSpec jes ->
                configureForkOptions(jes)
                logger.debug "Running AsciidoctorJ instance with environment: ${jes.environment}"
                jes.with {
                    setExecClass(jes, AsciidoctorJavaExec.canonicalName)
                    classpath = javaExecClasspath
                    args execConfigurationData.absolutePath
                }
            }
        } catch (GradleException e) {
            throw new AsciidoctorRemoteExecutionException(
                    'Remote Asciidoctor process failed to complete successfully',
                    e
            )
        }

        executorConfigurations
    }

    /** Attempt to use the mainClass property if we're running a recent enough Gradle version,
     * else revert to the old property.
     *
     * The main property will be removed from JavaExecSpec in Gradle 8.0 and replaced by
     * the mainClass property that was added in 6.4.
     */
    @CompileDynamic
    private void setExecClass(JavaExecSpec jes, String execClass) {
        if (PRE_6_4) {
            jes.main = execClass
        } else {
            jes.mainClass = execClass
        }
    }

    private void copyResourcesByBackend(
            Iterable<ExecutorConfiguration> executorConfigurations,
            Optional<String> lang
    ) {
        for (ExecutorConfiguration ec : executorConfigurations) {
            copyResourcesByBackend(ec, lang)
        }
    }

    private void copyResourcesByBackend(
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
            // Jumping through hoops to make docExtensions based upon closures to work.
            closurePaths.add(getClassLocation(org.gradle.internal.scripts.ScriptOrigin))
            closurePaths.addAll(ifNoGroovyAddLocal(deps))
        }

        if (deps.empty && closurePaths.empty) {
            null
        } else if (closurePaths.empty) {
            jrubyLessConfiguration(deps)
        } else if (deps.empty) {
            project.files(closurePaths)
        } else {
            jrubyLessConfiguration(deps) + project.files(closurePaths)
        }
    }

    private List<File> ifNoGroovyAddLocal(final List<Dependency> deps) {
        if (deps.find {
            it.name == 'groovy-all' || it.name == 'groovy'
        }) {
            []
        } else {
            [JavaExecUtils.localGroovy]
        }
    }

    @CompileDynamic
    private Configuration jrubyLessConfiguration(List<Dependency> deps) {
        Configuration cfg = project.configurations.detachedConfiguration(deps.toArray() as Dependency[])
        cfg.resolutionStrategy.eachDependency { DependencyResolveDetails dsr ->
            dsr.with {
                if (target.name == 'jruby' && target.group == 'org.jruby') {
                    useTarget "org.jruby:jruby:${target.version}"
                }
            }
        }
        cfg
    }

    private Map<String, Object> preparePreserialisedAttributes(final File workingSourceDir, Optional<String> lang) {
        prepareAttributes(
                workingSourceDir,
                attributes,
                lang.present ? asciidoctorj.getAttributesForLang(lang.get()) : [:],
                attributeProviders,
                lang
        )
    }

    private List<Closure> findExtensionClosures() {
        asciidoctorj.docExtensions.findAll {
            it instanceof Closure
        } as List<Closure>
    }

    @SuppressWarnings('AbstractClassWithoutAbstractMethod')
    abstract static class AsciidoctorJExecuterWorker implements WorkAction<Params> {
        static interface Params extends WorkParameters {
            ExecutorConfigurationContainer getExtensionConfigurationContainer()
            void setExtensionConfigurationContainer(ExecutorConfigurationContainer container)
        }

        @Override
        void execute() {
            new AsciidoctorJExecuter(parameters.extensionConfigurationContainer).run()
        }
    }
}
