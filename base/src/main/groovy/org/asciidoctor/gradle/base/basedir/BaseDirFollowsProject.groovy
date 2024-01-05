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
package org.asciidoctor.gradle.base.basedir

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.BaseDirStrategy
import org.gradle.api.Project

/** Strategy where the project directory is always to be base directory
 * for asciidoctor conversions.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.2.0
 */
@CompileStatic
class BaseDirFollowsProject implements BaseDirStrategy {

    private final File projectDir

    @Deprecated
    BaseDirFollowsProject(Project project) {
        this.projectDir = project.projectDir
    }

    /**
     * Sets the project by the project directory.
     *
     * @param projectDir Project directory.
     *
     * @since 4.0
     */
    BaseDirFollowsProject(final File projectDir) {
        this.projectDir = projectDir
    }

    /** Base directory location.
     *
     * @return Base directory.
     */
    @Override
    File getBaseDir() {
        this.projectDir
    }

    /** Base directory location.
     *
     * @param lang Ignored
     * @return Base directory.
     *
     * @since 3.0.0
     */
    @Override
    File getBaseDir(String lang) {
        baseDir
    }
}
