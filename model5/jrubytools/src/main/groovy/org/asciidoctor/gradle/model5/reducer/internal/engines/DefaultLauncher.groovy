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
import org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkerExecutor
import org.ysb33r.gradle.jruby.api.core.JRubyEnvironmentInheritance
import org.ysb33r.gradle.jruby.api.workers.MultiScriptWorkerInitiator
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.StringTools
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleJavaForkOptions

import javax.inject.Inject

import static java.util.Collections.EMPTY_MAP
import static org.asciidoctor.gradle.model5.reducer.internal.formatters.DefaultReducerAsciidocOutputFormatter.KEY_PRESERVE_CONDITIONALS
import static org.ysb33r.gradle.jruby.api.core.JRubyEnvironmentInheritance.ESSENTIAL_ONLY

/**
 * Launcher for the {@code asciidoctor-reducer} engine.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
@Slf4j
class DefaultLauncher implements AsciidoctorLauncher {

    private final StringTools stringTools
    private final ConfigCacheSafeOperations ccso
    private final DirectoryProperty logDir
    private final MultiScriptWorkerInitiator workerInitiator
    private final Property<JRubyEnvironmentInheritance> inheritEnv
    private final DirectoryProperty gemDir
    private final GrolifantSimpleJavaForkOptions forkOptions
    private final DefaultEngineOptions engineOptions
    private final ConfigurableFileCollection classpath
    private final Property<Integer> forkEvery
    private final WorkerExecutor worker

    @Inject
    DefaultLauncher(
        GrolifantSimpleJavaForkOptions forkOptions,
        DefaultEngineOptions engineOptions,
        WorkerExecutor we,
        Project tempProjectReference
    ) {
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.worker = we
        this.forkOptions = forkOptions
        this.engineOptions = engineOptions
        this.classpath = ccso.fsOperations().emptyFileCollection()
        this.stringTools = ccso.stringTools()
        this.inheritEnv = tempProjectReference.objects.property(JRubyEnvironmentInheritance).convention(ESSENTIAL_ONLY)
        this.gemDir = tempProjectReference.objects.directoryProperty()
        this.forkEvery = tempProjectReference.objects.property(Integer).convention(0)
        this.logDir = tempProjectReference.objects.directoryProperty().value(
            tempProjectReference.layout.buildDirectory.dir(LogProcessor.LOG_SUBPATH)
        )
    }

    void setForkEvery(Provider<Integer> value) {
        this.forkEvery.set(value)
    }

    /**
     * Add to the runtime classpath (Not the GEM classpath).
     *
     * @param files Collection of files to add.
     */
    void classpath(FileCollection files) {
        this.classpath.from(files)
    }

    /**
     * Sets the directory where the GEMs are located.
     * @param dir Directory
     */
    void setGemDir(Provider<Directory> dir) {
        this.gemDir.set(dir)
    }

    @Override
    String getEcosystemSignature() {
        classpath.files*.name.join('\n')
    }

    @Override
    void run(AsciidoctorExecutionSettings executionsSettings, AsciidoctorConversionSettings conversionSettings) {
        final baseOptions = allCmdLineOptions(executionsSettings, conversionSettings)
        final errorsFile = ccso.providerTools().zip3(
            logDir,
            executionsSettings.toolchainName,
            executionsSettings.formatterName
        ) { dir, tcName, fmtName ->
            dir.file("${tcName}/${fmtName}/reducer-errors.json")
        }

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
        }

        files.collect { k, v -> v.parentFile }.toSet().each {
            it.mkdirs()
        }

        final forkOnDocCount = forkEvery.get()
        final workerInitiator = new MultiScriptWorkerInitiator(this.worker, this.ccso).tap {
            resultsFile = errorsFile
            failOnNonZeroExitCode = true
            carryOnIfPossible = true
            it.forkEvery = forkOnDocCount
        }

        allExecutions.each {
            workerInitiator.scripts(it as Collection<Object>)
        }

        workerInitiator.resultsFile = errorsFile
        final wq = workerInitiator.runOnWorker(
            classpath,
            gemDir,
            forkOptions,
            inheritEnv.get(),
            engineOptions.openModules.get(),
            engineOptions.systemPath.get()
        )
        wq.await()
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
