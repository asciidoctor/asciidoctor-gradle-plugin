/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.core.internal.engines

import groovy.transform.CompileStatic

/**
 * Utilities that engine implementations can share.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class EngineUtils {
    /**
     * Takes a set of files and group them by their parent files.
     *
     * @param sourceFiles Set of source files
     *
     * @return Grouped files
     */
    static Map<File, List<File>> groupByParent(Set<File> sourceFiles) {
        sourceFiles.groupBy { it.parentFile }
    }
}
