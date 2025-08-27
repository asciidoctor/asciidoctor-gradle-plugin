package org.asciidoctor.gradle.model5.js.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorExecutionsSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.gradle.api.Project
import org.ysb33r.gradle.nodejs.NodeJSExecSpec
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.ExecTools
import org.ysb33r.grolifant5.api.core.OperatingSystem

import javax.inject.Inject

import static org.ysb33r.grolifant5.api.core.ExecTools.OutputType.CAPTURE
import static org.ysb33r.grolifant5.api.core.ExecTools.OutputType.FORWARD

/**
 *
 * @author Schalk W. Cronjé
 *
 * @since
 */
@CompileStatic
class DefaultLauncher implements AsciidoctorLauncher {

    @Inject
    DefaultLauncher(NodeJSExecSpec execSpec, Project tempProjectReference) {
        final ccso = ConfigCacheSafeOperations.from(tempProjectReference)

        this.execTools = ccso.execTools()
        this.execSpec = execSpec
    }

    @Override
    void run(AsciidoctorExecutionsSettings executionsSettings, AsciidoctorConversionSettings conversionSettings) {

        final sourcePaths = partitionFiles(conversionSettings. sourceFiles.get())

        final fixedArgs = [
                '-b', conversionSettings.backend.get().backend,
                '-S', executionsSettings.safeMode.get().toString().toLowerCase(Locale.US),
                '-B', conversionSettings.baseDir.get().absolutePath,
                '-D', conversionSettings.destinationDir.get().absolutePath
        ]
        final attrs = conversionSettings.attributes.get().collectMany { k, v ->
            if (v) {
                ['-a', "${k}=${v}".toString()]
            } else {
                ['-a', k]
            }
        } + ( conversionSettings.docType.present ? ['-d',conversionSettings. docType.get().lc()] : [])

        sourcePaths.each { partition ->
            final result = execTools.exec( CAPTURE, FORWARD) {spec ->
                execSpec.copyTo(spec)
                spec.args(fixedArgs)
                spec.args(attrs)
                spec.args(partition)
            }
            result.assertNormalExitValue()
        }
    }

    private List<List<String>> partitionFiles(Set<File> sourceFiles) {
        final sources = sourceFiles.collect { it.absolutePath }
        final allSum = (long)sources.sum { (long)it.size() }
        if(allSum <= CMD_LIMIT) {
            [ sources ]
        } else {
            List<List<String>> partitions = []
            int startIndex = 0
            int max = sources.size()
            while(true) {
                int index = findIndexWithinCmdLimit(startIndex, sources)
                partitions.add(sources[startIndex..<index])
                if(index == max) {
                    break
                }
            }
            partitions
        }
    }

    private int findIndexWithinCmdLimit(int startIndex,List<String> sources) {
        long accumulator = 0
        int index = startIndex
        int max = sources.size()
        while(index < max) {
            long size = sources[index].size()
            if(accumulator + size > CMD_LIMIT) {
                break
            } else {
                accumulator+= size
                index++
            }
        }
        index
    }
//    -o, --out-file          output file (default: based on path of input file) use '' to output to STDOUT  [string]
//    -e, --embedded          suppress enclosing document structure and output an embedded document  [boolean]
//    -s, --no-header-footer  suppress enclosing document structure and output an embedded document  [boolean]
//    -n, --section-numbers   auto-number section titles in the HTML backend disabled by default  [boolean] [default: false]
//    --failure-level     set minimum logging level that triggers non-zero exit code  [choices: "info", "INFO", "warn", "WARN", "warning", "WARNING", "error", "ERROR", "fatal", "FATAL"] [default: "FATAL"]
//    -q, --quiet             suppress warnings  [boolean] [default: false]
//    --trace             include backtrace information on errors  [boolean] [default: false]
//    -v, --verbose           enable verbose mode  [boolean] [default: false]
//    -t, --timings           enable timings mode  [boolean] [default: false]
//    -T, --template-dir      a directory containing custom converter templates that override the built-in converter (may be specified multiple times)  [array]
//    -E, --template-engine   template engine to use for the custom converter templates  [string]
//    -r, --require           require the specified library before executing the processor, using the standard Node require  [array]
    private final ExecTools execTools
    private final NodeJSExecSpec execSpec

    private final static long CMD_LIMIT = OperatingSystem.current().windows ? 7000L : (  (1L << 21) - 1000L)
}
