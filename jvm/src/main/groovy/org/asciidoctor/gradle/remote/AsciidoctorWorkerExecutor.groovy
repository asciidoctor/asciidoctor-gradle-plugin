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
package org.asciidoctor.gradle.remote

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.asciidoctor.Asciidoctor
import org.asciidoctor.gradle.internal.AsciidoctorWorkerParameters
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.groovydsl.AsciidoctorExtensions
import org.asciidoctor.log.LogHandler
import org.ysb33r.grolifant.api.remote.worker.WorkerAppExecutor

import static org.asciidoctor.jruby.AsciidoctorJRuby.Factory.create

/**
 * Runs Asciidoctor inside a worker.
 *
 * @author Schalk W. Cronjé
 *
 * @since 4.0
 */
@CompileStatic
@Log4j
class AsciidoctorWorkerExecutor implements WorkerAppExecutor<AsciidoctorWorkerParameters>, Serializable {

    @Delegate
    private final AsciidoctorJSetup setup

    @Delegate
    private final AsciidoctorJLogProcessor logProcessor

    AsciidoctorWorkerExecutor() {
        setup = new AsciidoctorJSetup()
        // TODO: Try to set these values up via startup
        logProcessor = new AsciidoctorJLogProcessor(
                0, // DEBUG
                4 // FATAL
        )
    }

    @Override
    void executeWith(AsciidoctorWorkerParameters params) {
        // TODO: Current implementation ignores the params.runParallelInWorker setting.
        params.asciidoctorConfigurations.values().each { exeConfigs ->
            exeConfigs.each { exeConfig ->
                runSingle(exeConfig)
            }
        }
    }

    @SuppressWarnings('CatchThrowable')
    private void runSingle(ExecutorConfiguration runConfiguration) {
        Asciidoctor asciidoctor = create()

        runConfiguration.with {
            asciidoctor.requireLibraries(runConfiguration.requires)
            if (asciidoctorExtensions?.size()) {
                registerExtensions(asciidoctor, asciidoctorExtensions)
            }
            LogHandler lh = getLogHandler(executorLogLevel)
            asciidoctor.registerLogHandler(lh)
            resetMessagePatternsTo(fatalMessagePatterns)
        }

        runConfiguration.outputDir.mkdirs()

        runConfiguration.sourceTree.each { File file ->
            try {
                if (runConfiguration.logDocuments) {
                    log.info("Converting ${file}")
                }
                asciidoctor.convertFile(file, normalisedOptionsFor(file, runConfiguration))
            } catch (Throwable exception) {
                throw new AsciidoctorRemoteExecutionException(
                        "ERROR: Running Asciidoctor whilst attempting to process ${file} " +
                                "using backend ${runConfiguration.backendName}",
                        exception
                )
            }
        }

        failOnFailureLevelReachedOrExceeded()
        failOnWarnings()
    }

    @CompileDynamic
    private void registerExtensions(Object asciidoctor, List<Object> exts) {
        AsciidoctorExtensions extensionRegistry = new AsciidoctorExtensions()

        for (Object ext in rehydrateExtensions(extensionRegistry, exts)) {
            extensionRegistry.addExtension(ext)
        }
        extensionRegistry.registerExtensionsWith((Asciidoctor) asciidoctor)
    }

//    private void addRequires(Asciidoctor asciidoctor) {
//        runConfigurations.each { runConfiguration ->
//            asciidoctor.requireLibraries(runConfiguration.requires)
//        }
//    }
}
