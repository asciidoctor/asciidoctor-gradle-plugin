/*
 * Copyright 2013-2020 the original author or authors.
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

