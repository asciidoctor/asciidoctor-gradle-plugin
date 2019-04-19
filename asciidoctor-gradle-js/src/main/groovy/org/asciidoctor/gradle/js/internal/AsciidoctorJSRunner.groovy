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
package org.asciidoctor.gradle.js.internal


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

    AsciidoctorJSRunner(
        File nodejs,
        Project project,
        File asciidoctorjs,
        String backend,
        SafeMode safeMode,
        File baseDir,
        File destinationDir,
        File workingDir,
        Map<String, String> attributes,
        Set<String> requires,
        Optional<String> doctype,
        boolean logDocuments
    ) {
        this.project = project
        this.asciidoctorjs = asciidoctorjs
        this.nodejs = nodejs
        this.destinationDir = destinationDir
        this.logDocuments = logDocuments
        this.nodeWorkingDir = workingDir

        this.arguments = [
            '-b', backend,
            '-S', safeMode.toString().toLowerCase(Locale.US),
            '-B', baseDir.absolutePath
        ]

        if (doctype.present) {
            arguments.addAll(['-d', doctype.get()])
        }

        arguments.addAll(attributes.collectMany { String key, String value ->
            !value ? ['-a', key] : ['-a', "${key}=${value}".toString()]
        })

        arguments.addAll(requires.collectMany {
            ['-r', it]
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
                args('-D', (relativeOutputPath.empty ? destinationDir : new File(destinationDir, relativeOutputPath)).absolutePath)
                args('--')
                args(Transform.toList(sources) {
                    it.absolutePath
                })
                setEnvironment(NodeJSExecutor.defaultEnvironment)
                workingDir = nodeWorkingDir
            }
        }

        if(logDocuments) {
            project.logger.info("Converting ${sources*.name.join(', ')}")
        }

        project.exec(configurator)
    }

    private final List<String> arguments
    private final Project project
    private final File nodejs
    private final File asciidoctorjs
    private final File destinationDir
    private final File nodeWorkingDir
    private final boolean logDocuments
    private static final String QUOTE = "'"

//    --embedded, -e          suppress enclosing document structure and output an embedded document [boolean] [default: false]
//    --no-header-footer, -s  suppress enclosing document structure and output an embedded document
//    Optional<String> sectionNumbers -n
//    Optional<String> failureLevel --failure-level
//        [choices: "info", "INFO", "warn", "WARN", "warning", "WARNING", "error", "ERROR", "fatal", "FATAL"] [default: "FATAL"]
//    boolean verboseMode -v
//   boolean traceMode --trace
//    boolean withTimings -t
}
