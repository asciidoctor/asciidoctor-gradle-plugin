/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.base.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorMultiLanguageException
import org.asciidoctor.gradle.base.AsciidoctorTaskFileOperations
import org.asciidoctor.gradle.base.AsciidoctorTaskTreeOperations
import org.asciidoctor.gradle.base.AsciidoctorTaskWorkspacePreparation
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileTree
import org.ysb33r.grolifant.api.core.ProjectOperations

/**
 * Default implementation of an Asciidoctor workspace preparation.
 *
 * @author Schalk W> Cronje
 *
 * @since 4.0
 */
@CompileStatic
class DefaultAsciidoctorWorkspacePreparation implements AsciidoctorTaskWorkspacePreparation {
    private final AsciidoctorTaskFileOperations fileOperations
    private final AsciidoctorTaskTreeOperations treeOperations
    private final ProjectOperations po

    DefaultAsciidoctorWorkspacePreparation(
            ProjectOperations po,
            AsciidoctorTaskFileOperations atfo,
            AsciidoctorTaskTreeOperations atto
    ) {
        this.po = po
        this.fileOperations = atfo
        this.treeOperations = atto
    }
    /**
     * Prepares a workspace prior to conversion.
     *
     * @return A presentation of the working source directory and the source tree.
     */
    @Override
    Workspace prepareWorkspace() {
        if (!fileOperations.languages.empty) {
            throw new AsciidoctorMultiLanguageException('Use prepareWorkspace(lang) instead.')
        }
        if (fileOperations.hasIntermediateWorkDir()) {
            File tmpDir = fileOperations.intermediateWorkDir
            prepareTempWorkspace(tmpDir)
            Workspace.builder()
                    .workingSourceDir(tmpDir)
                    .sourceTree(treeOperations.getSourceFileTreeFrom(tmpDir))
                    .build()
        } else {
            Workspace.builder()
                    .workingSourceDir(fileOperations.sourceDir)
                    .sourceTree(fileOperations.sourceFileTree).build()
        }
    }

    /**
     * Prepares a workspace for a specific language prior to conversion.
     *
     * @param language Language to prepare workspace for.
     * @return A presentation of the working source directory and the source tree.
     */
    @Override
    Workspace prepareWorkspace(String language) {
        if (fileOperations.hasIntermediateWorkDir()) {
            File tmpDir = new File(fileOperations.intermediateWorkDir, language)
            prepareTempWorkspace(tmpDir, language)
            Workspace.builder()
                    .workingSourceDir(tmpDir)
                    .sourceTree(treeOperations.getSourceFileTreeFrom(tmpDir))
                    .build()
        } else {
            File srcDir = new File(fileOperations.sourceDir, language)
            Workspace.builder()
                    .workingSourceDir(srcDir)
                    .sourceTree(treeOperations.getSourceFileTreeFrom(srcDir))
                    .build()
        }
    }

    private void prepareTempWorkspace(final File tmpDir) {
        if (!fileOperations.languages.empty) {
            throw new AsciidoctorMultiLanguageException('Use prepareTempWorkspace(tmpDir,lang) instead')
        }
        prepareTempWorkspace(
                tmpDir,
                fileOperations.sourceFileTree,
                fileOperations.secondarySourceFileTree,
                fileOperations.getResourceCopySpec(Optional.empty()),
                Optional.empty()
        )
    }

    private void prepareTempWorkspace(final File tmpDir, final String lang) {
            prepareTempWorkspace(
                    tmpDir,
                    treeOperations.getLanguageSourceFileTree(lang),
                    treeOperations.getLanguageSecondarySourceFileTree(lang),
                    fileOperations.getResourceCopySpec(Optional.of(lang)),
                    Optional.ofNullable(fileOperations.getLanguageResourceCopySpec(lang))
            )
    }

    private void prepareTempWorkspace(
            final File tmpDir,
            final FileTree mainSourceTree,
            final FileTree secondarySourceTree,
            final CopySpec resourceTree,
            final Optional<CopySpec> langResourcesTree
    ) {
        if (tmpDir.exists()) {
            tmpDir.deleteDir()
        }
        tmpDir.mkdirs()
        po.copy { CopySpec cs ->
            cs.with {
                into tmpDir
                from mainSourceTree
                from secondarySourceTree
                with resourceTree
                if (langResourcesTree.present) {
                    with langResourcesTree.get()
                }
            }
        }
    }

}
