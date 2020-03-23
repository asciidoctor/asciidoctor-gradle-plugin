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
package org.asciidoctor.gradle.remote

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.Asciidoctor
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.internal.ExecutorConfigurationContainer
import org.asciidoctor.gradle.internal.ExecutorLogLevel
import org.asciidoctor.groovydsl.AsciidoctorExtensions
import org.asciidoctor.log.LogHandler

import static org.asciidoctor.jruby.AsciidoctorJRuby.Factory.create

/** Runs Asciidoctor as an externally invoked Java process.
 *
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorJavaExec extends ExecutorBase {

    static void main(String[] args) {
        if (args.size() != 1) {
            throw new AsciidoctorRemoteExecutionException('No serialised location specified')
        }

        ExecutorConfigurationContainer ecc
        new File(args[0]).withInputStream { input ->
            new ObjectInputStream(input).withCloseable { ois ->
                ecc = (ExecutorConfigurationContainer) ois.readObject()
            }
        }

        new AsciidoctorJavaExec(ecc).run()
    }

    AsciidoctorJavaExec(ExecutorConfigurationContainer ecc) {
        super(ecc)
    }

    void run() {
        failureLevel = findHighestFailureLevel(runConfigurations*.failureLevel.toList())
        Thread.currentThread().contextClassLoader = this.class.classLoader
        Asciidoctor asciidoctor = asciidoctorInstance
        addRequires(asciidoctor)

        runConfigurations.each { runConfiguration ->
            if (runConfiguration.asciidoctorExtensions?.size()) {
                registerExtensions(asciidoctor, runConfiguration.asciidoctorExtensions)
            }
        }

        runConfigurations.each { runConfiguration ->
            LogHandler lh = getLogHandler(runConfiguration.executorLogLevel)
            asciidoctor.registerLogHandler(lh)
            resetMessagePatternsTo(runConfiguration.fatalMessagePatterns)
            runConfiguration.outputDir.mkdirs()
            convertFiles(asciidoctor, runConfiguration)
            asciidoctor.unregisterLogHandler(lh)
            failOnFailureLevelReachedOrExceeded()
            failOnWarnings()
        }
    }

    @SuppressWarnings(['Println', 'CatchThrowable'])
    private void convertFiles(Asciidoctor asciidoctor, ExecutorConfiguration runConfiguration) {
        runConfiguration.sourceTree.each { File file ->
            try {
                if (runConfiguration.logDocuments) {
                    println("Converting ${file}")
                }
                asciidoctor.convertFile(file, normalisedOptionsFor(file, runConfiguration))
            } catch (Throwable exception) {
                throw new AsciidoctorRemoteExecutionException(
                    "Error running Asciidoctor whilst attempting to process ${file} " +
                        "using backend ${runConfiguration.backendName}",
                    exception
                )
            }
        }
    }

    private Asciidoctor getAsciidoctorInstance() {
        String combinedGemPath = runConfigurations*.gemPath.findAll { it }.join(File.pathSeparator)
        boolean noGemPath = combinedGemPath.empty || combinedGemPath == File.pathSeparator
        noGemPath ? create() : create(combinedGemPath)
    }

    private void addRequires(Asciidoctor asciidoctor) {
        runConfigurations.each { runConfiguration ->
            for (require in runConfiguration.requires) {
                asciidoctor.requireLibrary(require)
            }
        }
    }

    /** Writes the message to stdout.
     *
     * @param logLevel The level of the message (ignored).
     * @param msg Message to be logged.
     */
    @SuppressWarnings('Println')
    @Override
    protected void logMessage(ExecutorLogLevel logLevel, String msg) {
        println msg
    }

    @CompileDynamic
    private void registerExtensions(Asciidoctor asciidoctor, List<Object> exts) {
        AsciidoctorExtensions extensionRegistry = new AsciidoctorExtensions()

        for (Object ext in rehydrateExtensions(extensionRegistry, exts)) {
            extensionRegistry.addExtension(ext)
        }
        extensionRegistry.registerExtensionsWith((Asciidoctor) asciidoctor)
    }
}
