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
package org.asciidoctor.gradle.model5.reducer.internal.engines

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.asciidoctor.gradle.model5.core.AsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorExecutionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.jrubytools.internal.engines.BaseLauncher
import org.asciidoctor.gradle.model5.jrubytools.internal.engines.DefaultJRubyEngineOptions
import org.gradle.api.Project
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleJavaForkOptions

import javax.inject.Inject

import static java.util.Collections.EMPTY_MAP
import static org.asciidoctor.gradle.model5.reducer.internal.formatters.DefaultReducerAsciidocOutputFormatter.KEY_PRESERVE_CONDITIONALS

/**
 * Launcher for the {@code asciidoctor-reducer} engine.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
@Slf4j
class DefaultLauncher extends BaseLauncher implements AsciidoctorLauncher {

    private final DefaultEngineOptions engineOptions

    @Inject
    DefaultLauncher(
        GrolifantSimpleJavaForkOptions forkOptions,
        DefaultEngineOptions engineOptions,
        WorkerExecutor we,
        Project tempProjectReference
    ) {
        super(forkOptions, we, tempProjectReference)
        this.engineOptions = engineOptions
    }

    @Override
    void run(AsciidoctorExecutionSettings executionsSettings, AsciidoctorConversionSettings conversionSettings) {
        final baseOptions = allCmdLineOptions(executionsSettings, conversionSettings)

        final outdir = conversionSettings.destinationDir.get()
        final root = conversionSettings.sourceRootDir.get().asFile

        final files = conversionSettings.sourceFiles.get().collectEntries {
            final targetName = it.name.replaceFirst(~/(\.a\p{Alpha}+?)$/, '.reduced.adoc')
            final relPath = ccso.fsOperations().relativize(root, it.parentFile)
            final target = relPath ? outdir.dir(relPath).file(targetName).asFile : outdir.file(targetName).asFile
            [it, target]
        } as Map<File, File>

        final allExecutions = files.collect { k, v ->
            final List<String> opts = []
            opts.addAll(baseOptions)
            opts.addAll(['-o', v.absolutePath, k.absolutePath])
            opts
        } as List<List<String>>

        files.collect { k, v -> v.parentFile }.toSet().each {
            it.mkdirs()
        }

        runOnMultiScriptWorker(
            executionsSettings.toolchainName,
            executionsSettings.formatterName,
            allExecutions
        )
    }

    @Override
    protected DefaultJRubyEngineOptions getJrubyEngineOptions() {
        this.engineOptions
    }

    @Override
    protected String getErrorLogBaseName() {
        'reducer-errors'
    }

    private List<String> allCmdLineOptions(
        AsciidoctorExecutionSettings executionsSettings,
        AsciidoctorConversionSettings conversionSettings
    ) {
        final List<String> cmdlineOptions = [
            '-S',
            'asciidoctor-reducer',
            '--safe-mode', executionsSettings.safeMode.get().name().toLowerCase(Locale.US)
        ]

        executionsSettings.moduleRequires.get().each {
            cmdlineOptions.addAll(['-r', it])
        }

        if (engineOptions.trace.getOrElse(false)) {
            cmdlineOptions.add('--trace')
        }

        if (conversionSettings.customProperties.get().containsKey(KEY_PRESERVE_CONDITIONALS)) {
            cmdlineOptions.add('--preserve-conditionals')
        }

        conversionSettings.attributes.getOrElse(EMPTY_MAP).each { k, v ->
            cmdlineOptions.add('-a')
            if (v == null) {
                cmdlineOptions.add("${k}!".toString())
            } else if (v) {
                cmdlineOptions.add("${k}=${v}".toString())
            } else {
                cmdlineOptions.add(k)
            }
        }

        cmdlineOptions.asImmutable()
    }
}
