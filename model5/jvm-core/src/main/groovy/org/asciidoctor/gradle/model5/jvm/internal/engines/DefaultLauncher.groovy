/*
 * Copyright 2013 - 2025 the original author or authors.
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
import org.asciidoctor.gradle.model5.core.AsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorExecutionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.ExecutionMode
import org.asciidoctor.gradle.model5.core.internal.engines.EngineUtils
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.FileSystemOperations

import javax.inject.Inject

/**
 * Launches conversion jobs on JVM workers.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultLauncher implements AsciidoctorLauncher {
    private final WorkerExecutor workerExecutor
    private final ConfigurableFileCollection classpath
    private final FileSystemOperations fsOperations
    private final Property<LauncherEngineOptions> launcherEngineOptions

    @Inject
    DefaultLauncher(Project tempProjectRef, WorkerExecutor we) {
        this.workerExecutor = we
        this.fsOperations = ConfigCacheSafeOperations.from(tempProjectRef).fsOperations()
        this.classpath = fsOperations.emptyFileCollection()
        this.launcherEngineOptions = tempProjectRef.objects.property(LauncherEngineOptions)
    }

    void classpath(FileCollection files) {
        this.classpath.from(files)
    }

    void setEngineOptions(Provider<LauncherEngineOptions> leo) {
        this.launcherEngineOptions.set(leo)
    }

    @Override
    void run(AsciidoctorExecutionSettings executionsSettings, AsciidoctorConversionSettings conversionSettings) {
        final wq = createWorkQueue(executionsSettings.preferredExecutionMode.getOrNull())
        final groups = EngineUtils.groupByParent(conversionSettings.sourceFiles.get())
        final root = conversionSettings.sourceRootDir.get().asFile
        final destDir = conversionSettings.destinationDir
        groups.each { parent, allFiles ->
            final relPath = fsOperations.relativize(root, parent)
            wq.submit(LauncherWorker) { lp ->
                lp.tap {
                    sourceFiles.set(allFiles)
                    requires.set(executionsSettings.moduleRequires)
                    baseDir.set(conversionSettings.baseDir)
                    adjustBaseDirPerFile.set(conversionSettings.adjustBaseDirPerFile)
                    destinationDir.set(relPath.empty ? destDir : destDir.map { it.dir(relPath) })
                    backend.set(conversionSettings.backend.map { b -> b.backend })
                    safeMode.set(executionsSettings.safeMode.map { it.name() })
                    attributes.set(conversionSettings.attributes)
                    engineOptions.set(launcherEngineOptions)
                }
            }
        }
        wq.await()
    }

    private WorkQueue createWorkQueue(ExecutionMode mode) {
        final cp = classpath

        if(mode == null || mode == ExecutionMode.IN_PROCESS) {
            workerExecutor.classLoaderIsolation { spec ->
                spec.classpath.from(cp)
            }
        } else {
            workerExecutor.processIsolation { spec ->
//                spec.forkOptions {
//                }
                spec.classpath.from(cp)
            }
        }
    }
}
