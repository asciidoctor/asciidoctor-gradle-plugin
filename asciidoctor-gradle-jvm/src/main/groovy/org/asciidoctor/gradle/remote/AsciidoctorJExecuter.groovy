/*
 * Copyright 2013-2018 the original author or authors.
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

import javax.inject.Inject

/** Actual executor used for running an Asciidoctorj instance.
 *
 * @since 2.0.0
 * @author Schalk W. Cronje
 */
@CompileStatic
@Log4j
class AsciidoctorJExecuter extends ExecutorBase implements Runnable {

    private final Set<File> asciidoctorClasspath

    @Inject
    AsciidoctorJExecuter(
        final ExecutorConfigurationContainer execConfig,
        final Set<File> asciidoctorClasspath
    ) {
        super(execConfig)
        this.asciidoctorClasspath = asciidoctorClasspath
    }

    @Override
    void run() {
        Thread.currentThread().contextClassLoader = asciidoctorClassLoader
        logLevel = findHighestLogLevel(runConfigurations*.executorLogLevel)
        logClasspath(Thread.currentThread().contextClassLoader)
        if (runConfigurations.size() == 1) {
            runSingle()
        } else {
            runMultiple()
        }
    }

    @SuppressWarnings('CatchThrowable')
    private void runSingle() {

        ExecutorConfiguration runConfiguration = runConfigurations[0]

        Asciidoctor asciidoctor = Asciidoctor.Factory.create(
            runConfiguration.gemPath.empty ? null : runConfiguration.gemPath
        )

        runConfiguration.with {
            for (require in requires) {
                asciidoctor.requireLibrary(require)
            }

            if (asciidoctorExtensions?.size()) {
                registerExtensions(asciidoctor, asciidoctorExtensions)
            }
        }

        runConfiguration.outputDir.mkdirs()

        runConfiguration.sourceTree.each { File file ->
            try {
                if (runConfiguration.logDocuments) {
                    log.info("Converting ${file}")
                }
                asciidoctor.convertFile(file, normalisedOptionsFor(file, runConfiguration))
            } catch (Throwable t) {
                throw new AsciidoctorRemoteExecutionException("Error running Asciidoctor whilst attempting to process ${file} using backend ${runConfiguration.backendName}", t)
            }
        }
    }

    @SuppressWarnings('CatchThrowable')
    private void runMultiple() {

        String combinedGemPath = runConfigurations*.gemPath.join(File.pathSeparator)


        Asciidoctor asciidoctor = (combinedGemPath.empty || combinedGemPath == File.pathSeparator) ?
            Asciidoctor.Factory.create() :
            Asciidoctor.Factory.create(combinedGemPath)

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
            runConfiguration.outputDir.mkdirs()

            runConfiguration.sourceTree.each { File file ->
                try {
                    if (runConfiguration.logDocuments) {
                        log.info("Converting ${file}")
                    }
                    asciidoctor.convertFile(file, normalisedOptionsFor(file, runConfiguration))
                } catch (Throwable t) {
                    throw new AsciidoctorRemoteExecutionException("Error running Asciidoctor whilst attempting to process ${file} using backend ${runConfiguration.backendName}", t)
                }
            }
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

    ExecutorLogLevel findHighestLogLevel(Iterable<ExecutorLogLevel> levels) {
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
    void logClasspath(ClassLoader cl) {
        if(cl instanceof URLClassLoader) {
            Set<URL> urls = ((URLClassLoader) cl).URLs as Set

            if(cl.parent instanceof URLClassLoader) {
                urls.addAll(((URLClassLoader) cl.parent).URLs as Set)
            }

            log.info "AsciidoctorJ worker is using effective classpath of: ${urls.join(' ')}"
        }

        // TODO: Find a way of logging classpath in JDK9 & 10
    }


}