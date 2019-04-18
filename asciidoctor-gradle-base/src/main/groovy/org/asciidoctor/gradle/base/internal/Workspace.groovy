package org.asciidoctor.gradle.base.internal

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import org.gradle.api.file.FileTree

/** Presents the workspace for a conversion.
 *
 * @since 3.0
 */
@CompileStatic
class Workspace {
    final File workingSourceDir
    final FileTree sourceTree

    @Builder
    Workspace( File workingSourceDir, FileTree sourceTree ) {
        this.workingSourceDir = workingSourceDir
        this.sourceTree = sourceTree
    }
}

