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
package org.asciidoctor.gradle.js.nodejs.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.SafeMode
import org.asciidoctor.gradle.base.Transform
import org.gradle.api.Project
import org.gradle.process.ExecSpec
import org.ysb33r.gradle.nodejs.utils.NodeJSExecutor

/** Executes an instance of Asciidoctor.Js
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSRunner {

    private static final String ATTR = '-a'
    private static final String REQUIRE = '-r'
    private static final String DOCTYPE = '-d'
    private static final String BACKEND = '-b'
    private static final String SAFEMODE = '-S'
    private static final String BASEDIR = '-B'
    private static final String DESTDIR = '-D'

    private final List<String> arguments
    private final Project project
    private final File nodejs
    private final File asciidoctorjs
    private final File destinationDir
    private final File nodeWorkingDir
    private final boolean logDocuments

    @SuppressWarnings('ParameterCount')
    AsciidoctorJSRunner(
        File nodejs,
        Project project,
        FileLocations asciidoctorjs,
        String backend,
        SafeMode safeMode,
        File baseDir,
        File destinationDir,
        Map<String, String> attributes,
        Set<String> requires,
        Optional<String> doctype,
        boolean logDocuments
    ) {
        this.project = project
        this.asciidoctorjs = asciidoctorjs.executable
        this.nodejs = nodejs
        this.destinationDir = destinationDir
        this.logDocuments = logDocuments
        this.nodeWorkingDir = asciidoctorjs.workingDir

        this.arguments = [
            BACKEND, backend,
            SAFEMODE, safeMode.toString().toLowerCase(Locale.US),
            BASEDIR, baseDir.absolutePath
        ]

        if (doctype.present) {
            arguments.addAll([DOCTYPE, doctype.get()])
        }

        arguments.addAll(attributes.collectMany { String key, String value ->
            value ? [ATTR, "${key}=${value}".toString()] : [ATTR, key]
        })

        arguments.addAll(requires.collectMany {
            [REQUIRE, it]
        })
    }

    void convert(File source, String relativeOutputPath) {
        convert([source] as Set, relativeOutputPath)
    }

    void convert(Set<File> sources, String relativeOutputPath) {
        Closure configurator = { ExecSpec spec ->
            spec.with {
                executable nodejs
                args(asciidoctorjs.absolutePath)
                args(arguments)
                args(
                    DESTDIR,
                    (relativeOutputPath.empty ?
                        destinationDir :
                        new File(destinationDir, relativeOutputPath)
                    ).absolutePath
                )
                args('--')
                args(Transform.toList(sources) {
                    it.absolutePath
                })
                environment = NodeJSExecutor.defaultEnvironment
                workingDir = nodeWorkingDir
            }
        }

        if (logDocuments) {
            project.logger.info("Converting ${sources*.name.join(', ')}")
        }

        project.exec((Closure) configurator)
    }

    @SuppressWarnings('ClassName')
    static class FileLocations {
        File executable
        File workingDir
    }

//    --embedded, -e          suppress enclosing document structure and output an embedded document
//                            [boolean] [default: false]
//    --no-header-footer, -s  suppress enclosing document structure and output an embedded document
//    Optional<String> sectionNumbers -n
//    Optional<String> failureLevel --failure-level
//        [choices: "info", "INFO", "warn", "WARN", "warning", "WARNING",
//        "error", "ERROR", "fatal", "FATAL"] [default: "FATAL"]
//    boolean verboseMode -v
//   boolean traceMode --trace
//    boolean withTimings -t
}
