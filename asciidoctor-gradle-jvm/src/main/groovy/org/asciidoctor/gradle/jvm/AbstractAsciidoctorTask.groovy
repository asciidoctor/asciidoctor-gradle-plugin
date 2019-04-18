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
import org.asciidoctor.gradle.base.AbstractAsciidoctorBaseTask
import org.asciidoctor.gradle.base.AsciidoctorAttributeProvider
import org.asciidoctor.gradle.base.AsciidoctorExecutionException
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.base.internal.Workspace
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.internal.ExecutorConfigurationContainer
import org.asciidoctor.gradle.internal.ExecutorUtils
import org.asciidoctor.gradle.internal.JavaExecUtils
import org.asciidoctor.gradle.remote.AsciidoctorJExecuter
import org.asciidoctor.gradle.remote.AsciidoctorJavaExec
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Provider
@java.lang.SuppressWarnings('NoWildcardImports')
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet
import org.gradle.process.JavaExecSpec
import org.gradle.process.JavaForkOptions
import org.gradle.util.GradleVersion
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.FileUtils
import org.ysb33r.grolifant.api.StringUtils

import java.nio.file.Path

import static org.asciidoctor.gradle.base.AsciidoctorUtils.executeDelegatingClosure
import static org.asciidoctor.gradle.base.AsciidoctorUtils.getClassLocation
import static org.asciidoctor.gradle.base.AsciidoctorUtils.UNDERSCORE_LED_FILES
import static org.asciidoctor.gradle.base.AsciidoctorUtils.executeDelegatingClosure
import static org.asciidoctor.gradle.base.AsciidoctorUtils.getClassLocation
import static org.asciidoctor.gradle.base.AsciidoctorUtils.getSourceFileTree
import static org.gradle.api.tasks.PathSensitivity.RELATIVE
import static org.gradle.workers.IsolationMode.CLASSLOADER
import static org.gradle.workers.IsolationMode.PROCESS
import static org.ysb33r.grolifant.api.FileUtils.filesFromCopySpec

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

    final static ProcessMode IN_PROCESS = ProcessMode.IN_PROCESS
    final static ProcessMode OUT_OF_PROCESS = ProcessMode.OUT_OF_PROCESS
    final static ProcessMode JAVA_EXEC = ProcessMode.JAVA_EXEC

    @Internal
    protected final static GradleVersion LAST_GRADLE_WITH_CLASSPATH_LEAKAGE = GradleVersion.version(('5.99'))

    private final AsciidoctorJExtension asciidoctorj
    private final WorkerExecutor worker
    private final List<Object> asciidocConfigurations = []
    private
    final org.ysb33r.grolifant.api.JavaForkOptions javaForkOptions = new org.ysb33r.grolifant.api.JavaForkOptions()

    /** Run Asciidoctor conversions in or out of process
     *
     * Valid options are {@link #IN_PROCESS}, {@link #OUT_OF_PROCESS} and {@link #JAVA_EXEC}.
     * The default mode is {@link #JAVA_EXEC}.
     */
    @Internal
    ProcessMode inProcess = JAVA_EXEC

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
     * @param configurator Closure that configures a {@link org.ysb33r.grolifant.api.JavaForkOptions} instance.
     */
    void forkOptions(@DelegatesTo(org.ysb33r.grolifant.api.JavaForkOptions) Closure configurator) {
        executeDelegatingClosure(this.javaForkOptions, configurator)
    }

    /** Set fork options for {@link #JAVA_EXEC} and {@link #OUT_OF_PROCESS} modes.
     *
     * These options are ignored if {@link #inProcess} {@code ==} {@link #IN_PROCESS}.
     *
     * @param configurator Action that configures a {@link org.ysb33r.grolifant.api.JavaForkOptions} instance.
     */
    void forkOptions(Action<org.ysb33r.grolifant.api.JavaForkOptions> configurator) {
        configurator.execute(this.javaForkOptions)
    }

    /** Returns all of the Asciidoctor options.
     *
     * This is equivalent of using {@code asciidoctorj.getOptions}
     *
     */
    @Input
    Map getOptions() {
        asciidoctorj.options
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
    Map getAttributes() {
        asciidoctorj.attributes
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
        FileCollection fc = asciidoctorj.configuration
        FileCollection precompiledExtensions = findDependenciesInExtensions()
        this.asciidocConfigurations.each {
            if (it instanceof Configuration) {
                fc = fc + (FileCollection) it
            } else {
                fc = fc + (FileCollection) (project.configurations.getByName(StringUtils.stringize(it)))
            }
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

    @SuppressWarnings('UnnecessaryGetter')
    @TaskAction
    void processAsciidocSources() {
        validateConditions()

        Workspace workspace = prepareWorkspace()
        Set<File> sourceFiles = workspace.sourceTree.files

        Map<String, ExecutorConfiguration> executorConfigurations

        if (finalProcessMode != JAVA_EXEC) {
            executorConfigurations = runWithWorkers(workspace.workingSourceDir, sourceFiles)
        } else {
            executorConfigurations = runWithJavaExec(workspace.workingSourceDir, sourceFiles)
        }

        copyResourcesByBackend(executorConfigurations.values())
    }

    @Override
    @Internal
    protected String getEngineName() {
        'AsciidoctorJ'
    }

    /** Initialises the core an Asciidoctor task
     *
     * @param we {@link WorkerExecutor}. This is usually injected into the
     *   constructor of the subclass.
     */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    protected AbstractAsciidoctorTask(WorkerExecutor we) {
        this.worker = we
        this.asciidoctorj = extensions.create(AsciidoctorJExtension.NAME, AsciidoctorJExtension, this)

        addInputProperty 'required-ruby-modules', { asciidoctorj.requires }
        addInputProperty 'gemPath', { asciidoctorj.asGemPath() }
        addInputProperty 'trackBaseDir', { AbstractAsciidoctorTask t -> t.getBaseDir().absolutePath }.curry(this)

        inputs.files { asciidoctorj.gemPaths }
        inputs.files { filesFromCopySpec(resourceCopySpec) }
    }

    /** Returns all of the executor configurations for this task
     *
     * @return Executor configurations
     */
    protected Map<String, ExecutorConfiguration> getExecutorConfigurations(
        final File workingSourceDir,
        final Set<File> sourceFiles
    ) {
        configuredOutputOptions.backends.collectEntries { String activeBackend ->
            [
                "backend=${activeBackend}".toString(),
                getExecutorConfigurationFor(activeBackend, workingSourceDir, sourceFiles)
            ]
        }
    }

    /** Provides configuration information for the worker.
     *
     * @param backendName Name of backend that will be run.
     * @param workingSourceDir Source directory that will used for work. This can be
     *   the original source directory or an intermediate.
     * @param sourceFiles THe actual top-level source files that will be used as entry points
     *   for generating documentation.
     * @return Executor configuration
     */
    @SuppressWarnings('Instanceof')
    protected ExecutorConfiguration getExecutorConfigurationFor(
        final String backendName,
        final File workingSourceDir,
        final Set<File> sourceFiles
    ) {

        java.util.Optional<List<String>> copyResources = getCopyResourcesForBackends()
        new ExecutorConfiguration(
            sourceDir: workingSourceDir,
            sourceTree: sourceFiles,
            outputDir: getOutputDirFor(backendName),
            baseDir: getBaseDir(),
            projectDir: project.projectDir,
            rootDir: project.rootProject.projectDir,
            options: evaluateProviders(options),
            attributes: preparePreserialisedAttributes(workingSourceDir),
            backendName: backendName,
            logDocuments: logDocuments,
            gemPath: gemPath,
            fatalMessagePatterns: asciidoctorj.fatalWarnings,
            asciidoctorExtensions: (asciidoctorJExtensions.findAll { !(it instanceof Dependency) }),
            requires: requires,
            copyResources: copyResources.present && (copyResources.get().empty || backendName in copyResources.get()),
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

    /** Configure Java fork options prior to execution
     *
     * The default method will copy anything configured via {@link #forkOptions(Closure c)} or
     * {@link #forkOptions(Action c)} to the rpovided {@link JavaForkOptions}.
     *
     * @param pfo Fork options to be configured.
     */
    @SuppressWarnings('UnusedMethodParameter')
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
    protected Set<String> getRequires() {
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

    private String getGemPath() {
        asciidoctorj.asGemPath()
    }

    private Map<String, ExecutorConfiguration> runWithWorkers(
        final File workingSourceDir, final Set<File> sourceFiles) {
        FileCollection asciidoctorClasspath = configurations
        logger.info "Running AsciidoctorJ with workers. Classpath = ${asciidoctorClasspath.files}"

        Map<String, ExecutorConfiguration> executorConfigurations = getExecutorConfigurations(
            workingSourceDir,
            sourceFiles
        )

        if (parallelMode) {
            executorConfigurations.each { String configName, ExecutorConfiguration executorConfiguration ->
                worker.submit(AsciidoctorJExecuter) { WorkerConfiguration config ->
                    configureWorker(
                        "Asciidoctor (task=${name}) conversion for ${configName}",
                        config,
                        asciidoctorClasspath,
                        new ExecutorConfigurationContainer(executorConfiguration)
                    )
                }
            }
        } else {
            worker.submit(AsciidoctorJExecuter) { WorkerConfiguration config ->
                configureWorker(
                    "Asciidoctor (task=${name}) conversions for ${executorConfigurations.keySet().join(', ')}",
                    config,
                    asciidoctorClasspath,
                    new ExecutorConfigurationContainer(executorConfigurations.values())
                )
            }
        }
        executorConfigurations
    }

    private void configureWorker(
        final String displayName,
        final WorkerConfiguration config,
        final FileCollection asciidoctorClasspath,
        final ExecutorConfigurationContainer ecContainer
    ) {
        config.isolationMode = inProcess == IN_PROCESS ? CLASSLOADER : PROCESS
        config.classpath = asciidoctorClasspath
        config.displayName = displayName
        config.params(
            ecContainer
        )
        configureForkOptions(config.forkOptions)
    }

    private Map<String, ExecutorConfiguration> runWithJavaExec(
        final File workingSourceDir,
        final Set<File> sourceFiles
    ) {
        FileCollection javaExecClasspath = JavaExecUtils.getJavaExecClasspath(
            project,
            configurations,
            asciidoctorj.injectInternalGuavaJar
        )
        Map<String, ExecutorConfiguration> executorConfigurations = getExecutorConfigurations(
            workingSourceDir,
            sourceFiles
        )
        File execConfigurationData = JavaExecUtils.writeExecConfigurationData(this, executorConfigurations.values())

        logger.debug("Serialised AsciidoctorJ configuration to ${execConfigurationData}")
        logger.info "Running AsciidoctorJ instance with classpath ${javaExecClasspath.files}"

        project.javaexec { JavaExecSpec jes ->
            configureForkOptions(jes)
            logger.debug "Running AsciidoctorJ instance with environment: ${jes.environment}"
            jes.with {
                main = AsciidoctorJavaExec.canonicalName
                classpath = javaExecClasspath
                args execConfigurationData.absolutePath
            }
        }

        executorConfigurations
    }

    private void copyResourcesByBackend(Iterable<ExecutorConfiguration> executorConfigurations) {
        CopySpec rcs = resourceCopySpec
        for (ExecutorConfiguration ec : executorConfigurations) {
            if (ec.copyResources) {
                copyResourcesByBackend(ec.backendName,ec.sourceDir,ec.outputDir)
            }
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

    private Map<String, Object> evaluateProviders(final Map<String, Object> initialMap) {
        initialMap.collectEntries { String k, Object v ->
            if (v instanceof Provider) {
                [k, v.get()]
            } else {
                [k, v]
            }
        } as Map<String, Object>
    }

    private Map<String, Object> preparePreserialisedAttributes(final File workingSourceDir) {
        Map<String, Object> attrs = [:]
        attrs.putAll(attributes)
        attributeProviders.each {
            attrs.putAll(it.attributes)
        }
        Set<String> userDefinedAttrKeys = attrs.keySet()

        Map<String, Object> defaultAttrs = getTaskSpecificDefaultAttributes(workingSourceDir).findAll { k, v ->
            !userDefinedAttrKeys.contains(k)
        }.collectEntries { k, v ->
            ["${k}@".toString(), v instanceof Serializable ? v : StringUtils.stringize(v)]
        } as Map<String, Object>

        attrs.putAll(defaultAttrs)
        evaluateProviders(attrs)
    }

    private List<Closure> findExtensionClosures() {
        asciidoctorj.docExtensions.findAll {
            it instanceof Closure
        } as List<Closure>
    }
}
