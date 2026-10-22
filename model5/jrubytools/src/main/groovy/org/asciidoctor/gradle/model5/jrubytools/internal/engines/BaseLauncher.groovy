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
package org.asciidoctor.gradle.model5.jrubytools.internal.engines

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.asciidoctor.gradle.model5.core.HasEcosystemSignature
import org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkerExecutor
import org.ysb33r.gradle.jruby.api.core.JRubyEnvironmentInheritance
import org.ysb33r.gradle.jruby.api.workers.MultiScriptWorkerInitiator
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleJavaForkOptions

import static org.ysb33r.gradle.jruby.api.core.JRubyEnvironmentInheritance.ESSENTIAL_ONLY

/**
 * Base classes for building JRuby-tool-based launchers
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
@Slf4j
abstract class BaseLauncher implements HasEcosystemSignature {

    protected final ConfigCacheSafeOperations ccso
    protected final ConfigurableFileCollection classpath
    private final DirectoryProperty logDir
    private final Property<JRubyEnvironmentInheritance> inheritEnv
    private final DirectoryProperty gemDir
    private final GrolifantSimpleJavaForkOptions forkOptions
    private final Property<Integer> forkEvery
    private final WorkerExecutor worker

    void setForkEvery(Provider<Integer> value) {
        this.forkEvery.set(value)
    }

    /**
     * Add to the runtime classpath (Not the GEM classpath).
     *
     * @param files Collection of files to add.
     */
    void classpath(FileCollection files) {
        this.classpath.from(files)
    }

    /**
     * Sets the directory where the GEMs are located.
     * @param dir Directory
     */
    void setGemDir(Provider<Directory> dir) {
        this.gemDir.set(dir)
    }

    @Override
    String getEcosystemSignature() {
        classpath.files*.name.join('\n')
    }

    protected BaseLauncher(
        GrolifantSimpleJavaForkOptions forkOptions,
        WorkerExecutor we,
        Project tempProjectReference
    ) {
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.worker = we
        this.forkOptions = forkOptions
        this.classpath = ccso.fsOperations().emptyFileCollection()
        this.inheritEnv = tempProjectReference.objects.property(JRubyEnvironmentInheritance).convention(ESSENTIAL_ONLY)
        this.gemDir = tempProjectReference.objects.directoryProperty()
        this.forkEvery = tempProjectReference.objects.property(Integer).convention(0)
        this.logDir = tempProjectReference.objects.directoryProperty().value(
            tempProjectReference.layout.buildDirectory.dir(LogProcessor.LOG_SUBPATH)
        )
    }

    abstract protected DefaultJRubyEngineOptions getJrubyEngineOptions()

    abstract protected String getErrorLogBaseName()

    protected void runOnMultiScriptWorker(
        Provider<String> toolchainName,
        Provider<String> formatterName,
        final List<List<String>> scriptCommandLines
    ) {
        final errorsFile = ccso.providerTools().zip3(
            logDir,
            toolchainName,
            formatterName
        ) { dir, tcName, fmtName ->
            dir.file("${tcName}/${fmtName}/${errorLogBaseName}.json")
        }

        final forkOnDocCount = forkEvery.get()
        final workerInitiator = new MultiScriptWorkerInitiator(this.worker, this.ccso).tap {
            resultsFile = errorsFile
            failOnNonZeroExitCode = true
            carryOnIfPossible = true
            it.forkEvery = forkOnDocCount
        }

        scriptCommandLines.each {
            workerInitiator.scripts(it as Collection<Object>)
        }

        workerInitiator.resultsFile = errorsFile
        final wq = workerInitiator.runOnWorker(
            classpath,
            gemDir,
            forkOptions,
            inheritEnv.get(),
            jrubyEngineOptions.openModules.get(),
            jrubyEngineOptions.systemPath.get()
        )
        wq.await()
    }
}
