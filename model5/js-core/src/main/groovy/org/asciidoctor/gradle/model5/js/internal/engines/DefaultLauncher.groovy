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
package org.asciidoctor.gradle.model5.js.internal.engines

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.asciidoctor.gradle.model5.core.AsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorExecutionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.internal.engines.EngineUtils
import org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.ysb33r.gradle.nodejs.NodeJSConfigCacheSafeOperations
import org.ysb33r.gradle.nodejs.NodeJSExecSpec
import org.ysb33r.gradle.nodejs.NpmConfigCacheSafeOperations
import org.ysb33r.gradle.nodejs.NpmPackageDescriptor
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.ExecTools
import org.ysb33r.grolifant5.api.core.OperatingSystem
import org.ysb33r.grolifant5.api.core.StringTools

import javax.inject.Inject
import java.util.regex.Pattern

import static org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor.LOG_EVENTS_FILE_PREFIX
import static org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor.parseLogs
import static org.ysb33r.grolifant5.api.core.ExecTools.OutputType.CAPTURE
import static org.ysb33r.grolifant5.api.core.StringTools.COLON
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 *
 * @author Schalk W. Cronjé
 *
 * @since
 */
@CompileStatic
@Slf4j
class DefaultLauncher implements AsciidoctorLauncher {

    private final ExecTools execTools
    private final StringTools stringTools
    private final NodeJSExecSpec execSpec
    private final ConfigCacheSafeOperations ccso
    private final ListProperty<NpmPackageDescriptor> packages
    private final NodeJSConfigCacheSafeOperations node
    private final NpmConfigCacheSafeOperations npm
    private final DirectoryProperty logDir

    private final static long CMD_LIMIT = OperatingSystem.current().windows ? 7000L : ((1L << 21) - 1000L)
    private final static Pattern LOG_LINE_MATCHER = ~/^asciidoctor: (ERROR|INFO|WARN|FATAL): .+$/

    @Inject
    DefaultLauncher(
        NodeJSExecSpec execSpec,
        NodeJSConfigCacheSafeOperations node,
        NpmConfigCacheSafeOperations npm,
        Project tempProjectReference
    ) {
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.execTools = ccso.execTools()
        this.stringTools = ccso.stringTools()
        this.execSpec = execSpec
        this.packages = tempProjectReference.objects.listProperty(NpmPackageDescriptor)
        this.node = node
        this.npm = npm
        this.logDir = tempProjectReference.objects.directoryProperty().value(
            tempProjectReference.layout.buildDirectory.dir(LogProcessor.LOG_SUBPATH)
        )
    }

    void setPackages(Provider<List<NpmPackageDescriptor>> pkgs) {
        this.packages.set(pkgs)
    }

    @Override
    void run(AsciidoctorExecutionSettings executionsSettings, AsciidoctorConversionSettings conversionSettings) {
        final groups = EngineUtils.groupByParent(conversionSettings.sourceFiles.get())
        final root = conversionSettings.sourceRootDir.get().asFile
        final destRoot = conversionSettings.destinationDir.get()
        final aliasName = conversionSettings.backend.map { b -> b.name }

        final jobLogDir = logDir.zip(executionsSettings.toolchainName) { ldir, tc ->
            ldir.dir(tc)
        }.zip(aliasName) { ldir, alias ->
            ldir.dir(alias)
        }
        final fixedArgs = [
            '-v',
            '-b', conversionSettings.backend.get().backend,
            '-S', executionsSettings.safeMode.get().toString().toLowerCase(Locale.US),
            '-B', conversionSettings.baseDir.get().asFile.absolutePath,
        ] + executionsSettings.moduleRequires.get().collectMany { ['-r', it] }

        final attrs = conversionSettings.attributes.get().collectMany { k, v ->
            if (v) {
                ['-a', "${k}=${v}".toString()]
            } else {
                ['-a', k]
            }
        } + (conversionSettings.docType.present ? ['-d', conversionSettings.docType.get().lc()] : [])

        int index = 1
        groups.each { parent, files ->
            final relPath = ccso.fsOperations().relativize(root, parent)
            final sourcePaths = partitionFiles(files)
            final destArgs = ['-D', relPath.empty ? destRoot.asFile : destRoot.dir(relPath).asFile]

            sourcePaths.each { partition ->
                final result = execTools.exec(CAPTURE, CAPTURE) { spec ->
                    execSpec.copyTo(spec)
                    spec.args(fixedArgs)
                    spec.args(destArgs)
                    spec.args(attrs)
                    spec.args(partition)
                    spec.ignoreExitValue = true
                }
                if (result.result.get()) {
                    log.error(result.standardError.asText.get())
                    result.assertNormalExitValue()
                }

                processLogToJson(index + 1, jobLogDir.get(), result.standardError.asText.get())
                ++index
            }
        }

        parseLogs(stringTools, jobLogDir.get(), conversionSettings.fatalWarnings.get(), index)
    }

    private void processLogToJson(int index, Directory dir, String stderr) {
        final logLines = stderr.readLines().findAll {
            it.find(LOG_LINE_MATCHER)
        }

        if (!logLines.empty) {
            final logFile = dir.file("${LOG_EVENTS_FILE_PREFIX}.${index}").asFile
            logFile.parentFile.mkdirs()
            logFile.withWriter { w ->
                w.println(LogProcessor.OPEN_RECORDS)
                logLines.each { line ->
                    final parts = line.split(COLON)
                    if (parts.size() >= 5) {
                        final data = [
                            severity: parts[1].trim(),
                            message : parts[4].trim(),
                            path    : parts[3].trim(),
                            file    : parts.size() >= 6 ? parts[5].trim() : EMPTY,
                            line    : parts[2].replaceFirst(~/\s?line\s/, EMPTY)
                        ]
                        w.println(JsonOutput.toJson(data))
                    }
                }
                w.println(LogProcessor.CLOSE_RECORDS)
            }
        }
    }

    private List<List<String>> partitionFiles(List<File> sourceFiles) {
        final sources = sourceFiles.collect { it.absolutePath }
        final allSum = (long) sources.sum { (long) it.size() }
        if (allSum <= CMD_LIMIT) {
            [sources]
        } else {
            List<List<String>> partitions = []
            int startIndex = 0
            int max = sources.size()
            while (true) {
                int index = findIndexWithinCmdLimit(startIndex, sources)
                partitions.add(sources[startIndex..<index])
                if (index == max) {
                    break
                }
            }
            partitions
        }
    }

    private int findIndexWithinCmdLimit(int startIndex, List<String> sources) {
        long accumulator = 0
        int index = startIndex
        int max = sources.size()
        while (index < max) {
            long size = sources[index].size()
            if (accumulator + size > CMD_LIMIT) {
                break
            } else {
                accumulator += size
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
}
