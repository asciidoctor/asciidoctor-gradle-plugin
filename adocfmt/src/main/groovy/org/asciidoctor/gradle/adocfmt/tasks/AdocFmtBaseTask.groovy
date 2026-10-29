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
package org.asciidoctor.gradle.adocfmt.tasks

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.adocfmt.AdocFmtExtension
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant5.api.core.runnable.ExecOutput
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

import java.time.Instant

import static org.ysb33r.grolifant5.api.core.ExecTools.OutputType.FORWARD

/**
 * Base task for implementing Asciidoc format checks and formatting.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.2
 */
@CompileStatic
abstract class AdocFmtBaseTask extends GrolifantDefaultTask {
    public static final List<String> ASCIIDOC_PATTERNS = [
        '**/*.adoc',
        '**/*.ad',
        '**/*.asc',
        '**/*.asciidoc'
    ].asImmutable()

    private static final FileFilter IS_DIRECTORY_FILTER = { File f -> f.directory } as FileFilter
    private final DirectoryProperty srcDir
    private final Provider<RegularFile> timestamp
    private final String adocFmtCommand
    private final Provider<File> adocfmt
    private final Property<Boolean> antora
    private final Provider<List<String>> adocfmtPaths

    /**
     * Sets the source directory
     * @param dir Source directory
     */
    void setSourceDir(Object dir) {
        this.srcDir.set(fsOperations().provideDirectory(dir))
    }

    /**
     * Whether the source directory points to an Antora directory i.e where {@code antora.yml} lives.
     *
     * @param flag {@code true} for Antora mode.
     */
    void setAntoraMode(boolean flag) {
        this.antora.set(flag)
    }

    /**
     * Whether the source directory points to an Antora directory i.e where {@code antora.yml} lives.
     *
     * @param flag Provider that will yield {@code true} for Antora mode.
     */
    void setAntoraMode(Provider<Boolean> flag) {
        this.antora.set(flag)
    }

    @TaskAction
    void exec() {
        final result = execTools().exec(FORWARD, FORWARD) { spec ->
            spec.executable = adocfmt.get().absolutePath
            spec.args(adocFmtCommand)
            spec.args(adocfmtPaths.get())
            spec.ignoreExitValue = true
        }

        handleResult(result)
        timestamp.get().asFile.text = Instant.now().toString()
    }

    protected AdocFmtBaseTask(String cmd) {
        this.timestamp = fsOperations().buildDirRegularFile(".adocFmt/${fsOperations().toSafeFileName(name)}")
        this.srcDir = project.objects.directoryProperty()
        this.adocFmtCommand = "--${cmd}"
        this.adocfmt = project.extensions.getByType(AdocFmtExtension).executable
        this.antora = project.objects.property(Boolean).convention(false)

        final allDirs = this.srcDir.zip(this.antora) { dir, antoraLayout ->
            if (antoraLayout) {
                final modules = new File(dir.asFile, 'modules')
                if (modules.exists()) {
                    final children = modules.listFiles(IS_DIRECTORY_FILTER)
                    children.collectMany {
                        it.listFiles(IS_DIRECTORY_FILTER).toList()
                    }.findAll { it.name == 'pages' }
                } else {
                    (List<File>) Collections.EMPTY_LIST
                }
            } else {
                [dir.asFile]
            }
        }

        this.adocfmtPaths = allDirs.map { it*.absolutePath }

        outputs.file(this.timestamp)

        final inputFiles = allDirs.map { dirs ->
            final ft = fsOperations().emptyFileCollection()
            dirs.each { ft.from(fsOperations().fileTree(it).include(ASCIIDOC_PATTERNS)) }
        }

        inputs.files(inputFiles)
            .skipWhenEmpty(true)
            .withPathSensitivity(PathSensitivity.RELATIVE)
            .optional()
    }

    abstract protected void handleResult(ExecOutput execResult)
}
