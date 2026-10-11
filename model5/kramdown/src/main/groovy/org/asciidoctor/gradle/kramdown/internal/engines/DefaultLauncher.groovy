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
package org.asciidoctor.gradle.kramdown.internal.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.kramdown.KramdownConversionSettings
import org.asciidoctor.gradle.kramdown.KramdownExecutionSettings
import org.asciidoctor.gradle.kramdown.KramdownLauncher
import org.asciidoctor.gradle.kramdown.publications.KramdownSourceSet
import org.asciidoctor.gradle.model5.jrubytools.internal.engines.BaseLauncher
import org.asciidoctor.gradle.model5.jrubytools.internal.engines.DefaultJRubyEngineOptions
import org.gradle.api.Project
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleJavaForkOptions

import javax.inject.Inject

import static java.util.Collections.EMPTY_MAP
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_AUTOID_DROP
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_AUTOID_GENERATE
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_AUTOID_PREFIX
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_AUTOID_SEPARATOR
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_AUTOLINK
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_LANGS
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_OFFSET
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_PASSTHRU
import static org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter.KEY_WRAP_MODE

/**
 * Default launcher for Kramdown
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultLauncher extends BaseLauncher implements KramdownLauncher {

    private final DefaultJRubyEngineOptions engineOptions

    @Inject
    DefaultLauncher(
        GrolifantSimpleJavaForkOptions forkOptions,
        DefaultJRubyEngineOptions engineOptions,
        WorkerExecutor we,
        Project tempProjectReference
    ) {
        super(forkOptions, we, tempProjectReference)
        this.engineOptions = engineOptions
    }

    @Override
    void run(KramdownExecutionSettings executionsSettings, KramdownConversionSettings conversionSettings) {
        final baseOptions = allCmdLineOptions(conversionSettings)

        final outdir = conversionSettings.destinationDir.get()
        final root = conversionSettings.sourceRootDir.get().asFile

        final files = conversionSettings.sourceFiles.get().collectEntries {
            final targetName = it.name.replaceFirst(~/(\.md)$/, '.adoc')
            final relPath = ccso.fsOperations().relativize(root, it.parentFile)
            final target = relPath ? outdir.dir(relPath).file(targetName).asFile : outdir.file(targetName).asFile
            [it, target]
        } as Map<File, File>

        final allExecutions = files.collect { k, v ->
            final List<String> opts = []
            opts.addAll(baseOptions)
            opts.addAll(['-o', v.absolutePath, k.absolutePath])
            opts
        }

        files.collect { k, v -> v.parentFile }.toSet().each {
            it.mkdirs()
        }

        runOnMultiScriptWorker(
            executionsSettings.toolchainName,
            executionsSettings.formatterName,
            (List<List<String>>)allExecutions
        )
    }

    @Override
    protected DefaultJRubyEngineOptions getJrubyEngineOptions() {
        this.engineOptions
    }

    @Override
    protected String getErrorLogBaseName() {
        'kramdown-errors'
    }

    private List<String> allCmdLineOptions(
        KramdownConversionSettings conversionSettings
    ) {
        final List<String> cmdlineOptions = [
            '-S',
            'kramdoc',
            "--format=${conversionSettings.sourceFormat.get().engineFormat}".toString()
        ]

        final cp = conversionSettings.customProperties.get()
        if (cp.containsKey(KEY_LANGS)) {
            cmdlineOptions.add("--diagram-languages=${cp[KEY_LANGS]}".toString())
        }

        if (cp.containsKey(KEY_WRAP_MODE)) {
            cmdlineOptions.add("--wrap=${cp[KEY_WRAP_MODE]}".toString())
        }

        if (cp.containsKey(KEY_OFFSET)) {
            cmdlineOptions.add("--heading-offset=${cp[KEY_OFFSET]}".toString())
        }

        cmdlineOptions.addAll([
            cp.containsKey(KEY_PASSTHRU) ? '--html-to-native' : '--no-html-to-native',
            cp.containsKey(KEY_AUTOLINK) ? '--auto-links' : '--no-auto-links'
        ])

        if (cp.containsKey(KEY_AUTOID_GENERATE)) {
            cmdlineOptions.add('--auto-ids')

            if (cp.containsKey(KEY_AUTOID_DROP)) {
                cmdlineOptions.add('--lazy-ids')
            }

            if (cp.containsKey(KEY_AUTOID_PREFIX)) {
                cmdlineOptions.add("--auto-id-prefix=${cp[KEY_AUTOID_PREFIX]}".toString())
            }

            if (cp.containsKey(KEY_AUTOID_SEPARATOR)) {
                cmdlineOptions.add("--auto-id-separator=${cp[KEY_AUTOID_SEPARATOR]}".toString())
            }
        }

        conversionSettings.attributes.getOrElse(EMPTY_MAP).each { k, v ->
            if (k == KramdownSourceSet.ATTR_IMAGES_DIR) {
                cmdlineOptions.add("--imagesdir=${k}".toString())
            } else {
                cmdlineOptions.add('-a')
                if (v == null) {
                    cmdlineOptions.add("${k}!".toString())
                } else if (v) {
                    cmdlineOptions.add("${k}=${v}".toString())
                } else {
                    cmdlineOptions.add(k)
                }
            }
        }

        cmdlineOptions.asImmutable()
    }
}
