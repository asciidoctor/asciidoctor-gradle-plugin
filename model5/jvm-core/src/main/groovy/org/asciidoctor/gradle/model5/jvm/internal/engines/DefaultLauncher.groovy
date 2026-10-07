/*
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.model5.jvm.internal.engines

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.asciidoctor.gradle.model5.core.AsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorExecutionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor
import org.asciidoctor.gradle.model5.jvm.engines.ExecutionContext
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.FileSystemOperations

import javax.inject.Inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

import static java.util.Collections.EMPTY_SET
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Launches conversion jobs on JVM workers.
 *
 * @author Schalk W. Cronjé
 * @author Artemy Osipov
 *
 * @since 5.0
 */
@CompileStatic
@Slf4j
class DefaultLauncher implements AsciidoctorLauncher {
    public final static String SCRIPTS_GROOVY = 'groovy'
    private final static String LOG_EVENTS_FILE_PREFIX = LogProcessor.LOG_EVENTS_FILE_PREFIX
    private final WorkerExecutor workerExecutor
    private final ConfigurableFileCollection classpath
    private final FileSystemOperations fsOperations
    private final Property<LauncherEngineOptions> launcherEngineOptions
    private final String projectPath
    private final ConcurrentMap<String, Provider<ExecutionContext>> executionsContexts
    private final DirectoryProperty logDir

    @Inject
    DefaultLauncher(Project tempProjectRef, WorkerExecutor we) {
        this.workerExecutor = we

        final ccso = ConfigCacheSafeOperations.from(tempProjectRef)
        this.projectPath = ccso.projectTools().fullProjectPath
        this.fsOperations = ccso.fsOperations()
        this.classpath = fsOperations.emptyFileCollection()
        this.launcherEngineOptions = tempProjectRef.objects.property(LauncherEngineOptions)
        this.executionsContexts = new ConcurrentHashMap<>()
        this.logDir = tempProjectRef.objects.directoryProperty().value(
            tempProjectRef.layout.buildDirectory.dir(LogProcessor.LOG_SUBPATH)
        )
    }

    void classpath(FileCollection files) {
        this.classpath.from(files)
    }

    void setEngineOptions(Provider<LauncherEngineOptions> leo) {
        this.launcherEngineOptions.set(leo)
    }

    void registerExecutionContext(
        String toolchainName,
        String formatterName,
        Provider<ExecutionContext> executionContext
    ) {
        executionsContexts.put(
            executionContextKey(toolchainName, formatterName),
            executionContext
        )
    }

    @Override
    String getEcosystemSignature() {
        launcherEngineOptions.map { it.toString() }.getOrElse(EMPTY)
    }

    @Override
    @SuppressWarnings('NestedBlockDepth')
    void run(AsciidoctorExecutionSettings executionsSettings, AsciidoctorConversionSettings conversionSettings) {
        final warnings = conversionSettings.fatalWarnings.getOrElse(EMPTY_SET)
        final wq = createWorkQueue(executionsSettings)
        final destDir = conversionSettings.destinationDir
        final aliasName = conversionSettings.backend.map { b -> b.name }
        final backendName = conversionSettings.backend.map { b -> b.backend }
        final jobLogDir = logDir.zip(executionsSettings.toolchainName) { ldir, tc ->
            ldir.dir(tc)
        }.zip(aliasName) { ldir, alias ->
            ldir.dir(alias)
        }

        jobLogDir.get().asFile.deleteDir()

        int logFileIndex = 1

        wq.submit(LauncherWorker) { lp ->
            lp.tap {
                sourceRootDir.set(conversionSettings.sourceRootDir)
                sourceFiles.set(conversionSettings.sourceFiles.get())
                requires.set(executionsSettings.moduleRequires)
                baseDir.set(conversionSettings.baseDir)
                adjustBaseDirPerFile.set(conversionSettings.adjustBaseDirPerFile)
                destinationDir.set(destDir)
                backend.set(backendName)
                safeMode.set(executionsSettings.safeMode.map { it.name() })
                attributes.set(conversionSettings.attributes)
                engineOptions.set(launcherEngineOptions)
                logFile.set(jobLogDir.map { it.file("${LOG_EVENTS_FILE_PREFIX}.${logFileIndex}") })
                embedded.set(conversionSettings.embedded.orElse(false))

                if (conversionSettings.templates.present) {
                    final t = conversionSettings.templates.get()
                    templateEngine.set(t.templateEngines.first())
                    templateDirs.set(t.templateDirs)
                }

                if (conversionSettings.scriptCollections.present) {
                    final sc = conversionSettings.scriptCollections.get()
                    if (sc.containsKey(SCRIPTS_GROOVY)) {
                        final scFiles = sc[SCRIPTS_GROOVY].scriptFiles.getOrNull()
                        final scScripts = sc[SCRIPTS_GROOVY].scripts.getOrNull()

                        if (scFiles) {
                            groovyExtensionScriptFiles.set(scFiles)
                        }

                        if (scScripts) {
                            groovyExtensionScripts.set(scScripts)
                        }
                    }
                }
            }
        }
        wq.await()

        LogProcessor.parseLogs(jobLogDir.get(), warnings, logFileIndex)
    }

    private Optional<ExecutionContext> getExecutionContext(String toolchainName, String formatterName) {
        final ec = executionsContexts[executionContextKey(toolchainName, formatterName)]
        if (ec == null || !ec.present) {
            Optional.empty()
        } else {
            Optional.of(ec.get())
        }
    }

    private WorkQueue createWorkQueue(AsciidoctorExecutionSettings executionsSettings) {
        final toolchain = executionsSettings.toolchainName.get()
        final formatter = executionsSettings.formatterName.get()
        final ec = getExecutionContext(toolchain, formatter)
        final cp = classpath + executionsSettings.additionalClasspath
        log.info("Classpath files for ${toolchain}-${formatter}: ${cp.files*.name}")
        if (ec.present) {
            log.info("Engine context found for ${toolchain}-${formatter}. Running workers out of process.")
            final context = ec.get()
            workerExecutor.processIsolation { spec ->
                spec.forkOptions {
                    context.copyTo(it)
                }
                spec.classpath.from(cp)
            }
        } else {
            log.info("No engine context found for ${toolchain}-${formatter}. Running workers in process.")
            workerExecutor.classLoaderIsolation { spec ->
                spec.classpath.from(cp)
            }
        }
    }

    private String executionContextKey(String toolchainName, String formatterName) {
        "${toolchainName}-${formatterName}"
    }
}
