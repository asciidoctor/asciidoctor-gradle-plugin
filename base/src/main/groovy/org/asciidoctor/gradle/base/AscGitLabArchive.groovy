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
package org.asciidoctor.gradle.base

import org.ysb33r.grolifant.api.core.ProjectOperations
import org.ysb33r.grolifant.api.core.git.GitLabArchive

/** Represents a remote GitLab Archive.
 *
 * @since 2.0
 */
@SuppressWarnings('ClassNameSameAsSuperclass')
class AscGitLabArchive extends GitLabArchive {

    /** Relative path to locate the them inside the GitLab archive.
     *
     */
    Object relativePath

    /**
     * Create a downloadable Gitlab reference.
     *
     * @param po {@link ProjectOperations} that is linked to the project.
     *
     * @since 4.0
     */
    AscGitLabArchive(ProjectOperations po) {
        super(po)
    }
}
