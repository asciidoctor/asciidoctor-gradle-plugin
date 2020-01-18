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
package org.asciidoctor.gradle.base

import groovy.transform.CompileStatic
import org.gradle.api.tasks.Input

/**
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class OutputOptions {

    private Boolean separateOutputDirPerBackend
    private final Set<String> backends = []

    /** If set to true each backend will be output to a separate subfolder below {@code outputDir}
     *
     * @since 1.5.1
     */
    @Input
    @SuppressWarnings('UnnecessaryGetter')
    boolean getSeparateOutputDirs() {
        this.separateOutputDirPerBackend != null ? this.separateOutputDirPerBackend : (this.backends.size() > 1)
    }

    /** Sets whether multiple output directories must be used.
     *
     * @param v {@code true} for multiple directories.
     */
    @SuppressWarnings('UnnecessarySetter')
    void setSeparateOutputDirs(boolean v) {
        this.separateOutputDirPerBackend = v
    }

    /** Returns the set of backends that was configured for this task
     *
     * @return Set of backends.
     */
    @Input
    Set<String> getBackends() {
        this.backends.empty ? ['html5'].toSet() : this.backends
    }

    /** Sets a new set of backends.
     *
     * @param newBackends New collection of backends
     */
    void setBackends(Iterable<String> newBackends) {
        this.backends.clear()
        this.backends.addAll newBackends
    }

    /** Adds additional backends.
     *
     * @param addBackends Additional backends
     */
    void backends(String... addBackends) {
        this.backends.addAll addBackends
    }
}
