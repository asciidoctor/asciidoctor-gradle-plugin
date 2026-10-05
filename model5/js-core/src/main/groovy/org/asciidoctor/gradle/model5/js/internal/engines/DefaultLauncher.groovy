/*
 * Copyright 2013 - 2026 the original author or authors.
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
import org.ysb33r.gradle.jse.pnpm.toolchains.JsePnpmExecSpec
import org.ysb33r.gradle.jsecosystem.packages.PackageDescriptor
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.ExecTools
import org.ysb33r.grolifant5.api.core.OperatingSystem

import javax.inject.Inject
import java.util.regex.Pattern

import static java.util.Collections.EMPTY_LIST
import static java.util.Collections.EMPTY_SET
import static org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor.LOG_EVENTS_FILE_PREFIX
import static org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor.parseLogs
import static org.ysb33r.grolifant5.api.core.ExecTools.OutputType.CAPTURE

/**
 * Launcher for the {@code asciidoctor.js} engine.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
@Slf4j
class DefaultLauncher implements AsciidoctorLauncher {

    private final static long CMD_LIMIT = OperatingSystem.current().windows ? 7000L : ((1L << 21) - 1000L)
    private final static Pattern LOG_LINE_MATCHER = ~(
        /^asciidoctor: (?<severity>ERROR|INFO|WARN(?:ING)?|FATAL): / +
            /(?:(?<path>.+?): line (?<line>\d+): )?(?<message>.+)$/
    )

    private final ExecTools execTools
    private final Provider<JsePnpmExecSpec> execSpec
    private final ConfigCacheSafeOperations ccso
    private final ListProperty<PackageDescriptor> packages
    private final DirectoryProperty logDir

    @Inject
    DefaultLauncher(
        Provider<JsePnpmExecSpec> execSpec,
        Project tempProjectReference
    ) {
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.execTools = ccso.execTools()
        this.execSpec = execSpec
        this.packages = tempProjectReference.objects.listProperty(PackageDescriptor)
        this.logDir = tempProjectReference.objects.directoryProperty().value(
            tempProjectReference.layout.buildDirectory.dir(LogProcessor.LOG_SUBPATH)
        )
    }

    void setPackages(Provider<List<PackageDescriptor>> pkgs) {
        this.packages.set(pkgs)
    }

    @Override
    String getEcosystemSignature() {
        packages.get()*.packageCoordinates.join('\n')
    }

    @Override
    void run(AsciidoctorExecutionSettings executionsSettings, AsciidoctorConversionSettings conversionSettings) {
        final warnings = conversionSettings.fatalWarnings.getOrElse(EMPTY_SET)
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

        final embedded = conversionSettings.embedded.orElse(false).map {
            it ? ['-e', '-s'] : EMPTY_LIST
        }.get()

        final templateDirs = conversionSettings.templates.map {
            it.templateDirs.collectMany { ['-T', it.asFile.absolutePath] }
        }.getOrElse(EMPTY_LIST)

        final attrs = conversionSettings.attributes.get().collectMany { k, v ->
            ['-a', v ? "${k}=${v}".toString() : k]
        } + (conversionSettings.docType.present ? ['-d', conversionSettings.docType.get().lc()] : EMPTY_LIST)

        int index = 1
        groups.each { parent, files ->
            final relPath = ccso.fsOperations().relativize(root, parent)
            final sourcePaths = partitionFiles(files)
            final destArgs = ['-D', relPath.empty ? destRoot.asFile : destRoot.dir(relPath).asFile]

            sourcePaths.each { partition ->
                final result = execTools.exec(CAPTURE, CAPTURE) { spec ->
                    execSpec.get().copyTo(spec)
                    spec.tap {
                        args(embedded)
                        args(fixedArgs)
                        args(destArgs)
                        args(attrs)
                        args(templateDirs)
                        args(partition)
                        ignoreExitValue = true
                    }
                }
                if (result.result.get()) {
                    log.error(result.standardError.asText.get())
                    result.assertNormalExitValue()
                }

                processLogToJson(index + 1, jobLogDir.get(), result.standardError.asText.get())
                ++index
            }
        }

        parseLogs(jobLogDir.get(), warnings, index)
    }

    private void processLogToJson(int index, Directory dir, String stderr) {
        final logLines = stderr.readLines().collect { LOG_LINE_MATCHER.matcher(it) }.findAll { it.matches() }

        final logFile = dir.file("${LOG_EVENTS_FILE_PREFIX}.${index}").asFile
        logFile.parentFile.mkdirs()
        final records = logLines.collect { m ->
            final data = [
                severity: m.group('severity'),
                message : m.group('message')
            ]

            final path = m.group('path')
            if (path) {
                data.putAll([
                    path: path,
                    line: m.group('line')
                ])
            }

            JsonOutput.toJson(data)
        }

        logFile.withWriter { w ->
            w.println(LogProcessor.OPEN_RECORDS)
            if (records) {
                w.println(records.join(',\n'))
            }
            w.println(LogProcessor.CLOSE_RECORDS)
        }
    }

    private List<? extends List<String>> partitionFiles(List<File> sourceFiles) {
        final sources = sourceFiles*.absolutePath
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
}
