package org.asciidoctor.gradle.js.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.SafeMode
import org.asciidoctor.gradle.base.Transform
import org.gradle.api.Project
import org.ysb33r.gradle.nodejs.NodeJSExecSpec

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
        Map<String, String> attributes,
        Set<String> requires,
        Optional<String> doctype
    ) {
        this.project = project
        this.asciidoctorjs = asciidoctorjs
        this.nodejs = nodejs
        this.destinationDir = destinationDir
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

    @CompileDynamic
    void convert(Set<File> sources, String relativeOutputPath) {
        Closure configurator = { NodeJSExecSpec spec ->
            spec.with {
                executable nodejs
                script(asciidoctorjs.absolutePath)
                scriptArgs(arguments)
                scriptArgs('-D', (relativeOutputPath.empty ? destinationDir : new File(destinationDir, relativeOutputPath)).absolutePath)
                scriptArgs('--')
                scriptArgs(Transform.toList(sources) {
                    it.absolutePath
                })
            }
        }

        project.nodeexec configurator
    }

    private final List<String> arguments
    private final Project project
    private final File nodejs
    private final File asciidoctorjs
    private final File destinationDir
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
