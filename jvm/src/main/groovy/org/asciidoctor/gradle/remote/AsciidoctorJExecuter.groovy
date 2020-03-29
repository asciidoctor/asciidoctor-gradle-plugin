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
import groovy.util.logging.Log4j
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import org.asciidoctor.Asciidoctor
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.internal.ExecutorConfigurationContainer
import org.asciidoctor.gradle.internal.ExecutorLogLevel
import org.asciidoctor.groovydsl.AsciidoctorExtensions
import org.asciidoctor.log.LogHandler

import javax.inject.Inject

import static org.asciidoctor.jruby.AsciidoctorJRuby.Factory.create

/** Actual executor used for running an Asciidoctorj instance.
 *
 * @since 2.0.0* @author Schalk W. Cronje
 */
@CompileStatic
@Log4j
class AsciidoctorJExecuter extends ExecutorBase implements Runnable {

    @Inject
    AsciidoctorJExecuter(
        final ExecutorConfigurationContainer execConfig
    ) {
        super(execConfig)
    }

    @Override
    void run() {
        Thread.currentThread().contextClassLoader = asciidoctorClassLoader
        logLevel = findHighestLogLevel(runConfigurations*.executorLogLevel)
        failureLevel = findHighestFailureLevel(runConfigurations*.failureLevel.toList())
        logClasspath(Thread.currentThread().contextClassLoader)
        if (runConfigurations.size() == 1) {
            runSingle()
        } else {
            runMultiple()
        }
    }

    /** Forwards the message to Log4J.
     *
     * @param logLevel The level of the message
     * @param msg Message to be logged
     */
    @Override
    protected void logMessage(ExecutorLogLevel level, String msg) {
        switch (level) {
            case ExecutorLogLevel.DEBUG:
                log.debug(msg)
                break
            case ExecutorLogLevel.INFO:
                log.info(msg)
                break
            case ExecutorLogLevel.WARN:
                log.warn(msg)
                break
            case ExecutorLogLevel.ERROR:
                log.error(msg)
                break
            case ExecutorLogLevel.QUIET:
                log.fatal(msg)
                break
        }
    }

    @SuppressWarnings('CatchThrowable')
    private void runSingle() {
        ExecutorConfiguration runConfiguration = runConfigurations[0]

        Asciidoctor asciidoctor = create(
            runConfiguration.gemPath.empty ? null : runConfiguration.gemPath
        )

        runConfiguration.with {
            for (require in requires) {
                asciidoctor.requireLibrary(require)
            }

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

    @SuppressWarnings(['CatchThrowable'])
    private void runMultiple() {
        String combinedGemPath = runConfigurations*.gemPath.join(File.pathSeparator)

        Asciidoctor asciidoctor = (combinedGemPath.empty || combinedGemPath == File.pathSeparator) ?
            create() :
            create(combinedGemPath)

        runConfigurations.each { runConfiguration ->
            for (require in runConfiguration.requires) {
                asciidoctor.requireLibrary(require)
            }

            if (runConfiguration.asciidoctorExtensions?.size()) {
                registerExtensions(asciidoctor, runConfiguration.asciidoctorExtensions)
            }
        }

        runConfigurations.each { runConfiguration ->
            logLevel = runConfiguration.executorLogLevel
            resetMessagePatternsTo(runConfiguration.fatalMessagePatterns)
            runConfiguration.outputDir.mkdirs()

            runConfiguration.sourceTree.each { File file ->
                try {
                    if (runConfiguration.logDocuments) {
                        log.info("Converting ${file}")
                    }
                    asciidoctor.convertFile(file, normalisedOptionsFor(file, runConfiguration))
                } catch (Throwable exception) {
                    throw new AsciidoctorRemoteExecutionException('Error running Asciidoctor whilst attempting to ' +
                        "process ${file} using backend ${runConfiguration.backendName}",
                        exception
                    )
                }
            }

            failOnFailureLevelReachedOrExceeded()
            failOnWarnings()
        }
    }

    @CompileDynamic
    private void registerExtensions(Object asciidoctor, List<Object> exts) {
        AsciidoctorExtensions extensionRegistry = new AsciidoctorExtensions()

        for (Object ext in rehydrateExtensions(extensionRegistry, exts)) {
            extensionRegistry.addExtension(ext)
        }
        extensionRegistry.registerExtensionsWith((Asciidoctor) asciidoctor)
    }

    private ClassLoader getAsciidoctorClassLoader() {
        this.class.classLoader
    }

    private ExecutorLogLevel findHighestLogLevel(Iterable<ExecutorLogLevel> levels) {
        int lvl = levels*.level.min() as int
        ExecutorLogLevel.values().find {
            it.level == lvl
        }
    }

    private void setLogLevel(ExecutorLogLevel lvl) {
        switch (lvl) {
            case ExecutorLogLevel.DEBUG:
                LogManager.getLogger(log.name).level = Level.DEBUG
                break
            case ExecutorLogLevel.INFO:
                LogManager.getLogger(log.name).level = Level.INFO
                break
            case ExecutorLogLevel.WARN:
                LogManager.getLogger(log.name).level = Level.WARN
                break
            case ExecutorLogLevel.ERROR:
                LogManager.getLogger(log.name).level = Level.ERROR
                break
            case ExecutorLogLevel.QUIET:
                LogManager.getLogger(log.name).level = Level.FATAL
                break
        }
    }

    @SuppressWarnings('Instanceof')
    private void logClasspath(ClassLoader cl) {
        if (cl instanceof URLClassLoader) {
            Set<URL> urls = ((URLClassLoader) cl).URLs as Set

            if (cl.parent instanceof URLClassLoader) {
                urls.addAll(((URLClassLoader) cl.parent).URLs as Set)
            }

            log.info "AsciidoctorJ worker is using effective classpath of: ${urls.join(' ')}"
        }

        // TODO: Find a way of logging classpath in JDK9 & 10
    }
}