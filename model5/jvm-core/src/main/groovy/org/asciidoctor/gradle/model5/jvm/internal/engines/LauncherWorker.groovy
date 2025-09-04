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

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.asciidoctor.log.LogHandler
import org.asciidoctor.log.LogRecord
import org.gradle.workers.WorkAction

import static org.asciidoctor.log.Severity.ERROR
import static org.asciidoctor.log.Severity.FATAL
import static org.asciidoctor.log.Severity.INFO
import static org.asciidoctor.log.Severity.WARN
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Running AsciidoctorJ in a worker.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
@SuppressWarnings('AbstractClassWithoutAbstractMethod')
abstract class LauncherWorker implements WorkAction<LauncherParameters> {
    @Override
    void execute() {
        final asciidoctor = Asciidoctor.Factory.create()
        final reqs = parameters.requires.get()
        final logger = new WorkerLogHandler(parameters.logFile.get().asFile)
        asciidoctor.registerLogHandler(logger)

        if (!reqs.empty) {
            asciidoctor.requireLibraries(reqs)
        }

        // TODO: Handle extensions

        final destDir = parameters.destinationDir.get().asFile
        destDir.mkdirs()

        try {
            if (parameters.adjustBaseDirPerFile.get()) {
                partitionSourceFiles().each { bd, files ->
                    asciidoctor.convertFiles(files, normalisedOptions(bd))
                }
            } else {
                asciidoctor.convertFiles(parameters.sourceFiles.get(), normalisedOptions(parameters.baseDir.get().asFile))
            }
        } finally {
            logger?.close()
        }

    }

    private Options normalisedOptions(File withBaseDir) {
        final optionsBuilder = Options.builder()
        final attributesBuilder = Attributes.builder()

        parameters.attributes.get().each { k, v ->
            if (v == null) {
                attributesBuilder.attribute(k, null)
            } else {
                attributesBuilder.attribute(k, v)
            }
        }

        final eo = parameters.engineOptions.get()

        optionsBuilder.tap {
            inPlace(false)
            mkDirs(true)
            backend(parameters.backend.get())
            safe(SafeMode.valueOf(parameters.safeMode.get().toUpperCase(Locale.US)))
            baseDir(withBaseDir)
            toDir(parameters.destinationDir.get().asFile)
            attributes(attributesBuilder.build())

            catalogAssets(eo.catalogAssets)
            eruby(eo.eruby)
            sourcemap(eo.sourceMap)
            standalone(!parameters.embedded.get())
        }

        optionsBuilder.build()
    }

    private Map<File, List<File>> partitionSourceFiles() {
        parameters.sourceFiles.get().groupBy { it.parentFile }
    }

    private static class WorkerLogHandler implements LogHandler, AutoCloseable {

        private final File logFile

        WorkerLogHandler(File logFile) {
            this.logFile = logFile
            logFile.parentFile.mkdirs()
            this.logFile.text = '[\n'
        }

        @Override
        void close() throws Exception {
            logFile.withWriterAppend { it.println ']' }
        }

        @Override
        void log(LogRecord logRecord) {
            if (logRecord.severity in [ERROR, WARN, FATAL]) {
                final data = [
                    severity: logRecord.severity.name(),
                    message : logRecord.message,
//                    source  : logRecord.sourceFileName ?: EMPTY,
//                    method  : logRecord.sourceMethodName ?: EMPTY
                ]

                final cursor = logRecord.cursor

                if (cursor) {
                    data.putAll([
                        path: cursor.path ?: EMPTY,
                        dir : cursor.dir ?: EMPTY,
                        file: cursor.file ?: EMPTY,
                        line: cursor.lineNumber >= 0 ? cursor.lineNumber.toString() : EMPTY
                    ])
                }

                logFile.withWriterAppend { it.println(JsonOutput.toJson(data)) }
            }
        }
    }
}
