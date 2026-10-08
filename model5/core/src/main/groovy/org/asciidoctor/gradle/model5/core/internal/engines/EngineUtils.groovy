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
package org.asciidoctor.gradle.model5.core.internal.engines

import groovy.transform.CompileStatic

/**
 * Utilities that engine implementations can share.
 *
 * @author Schalk W. Cronjé
 * @author Mattias Reichel
 *
 * @since 5.0
 */
@CompileStatic
class EngineUtils {

    /**
     * The attribute that holds the relative path from the directory of a document back to the directory the
     * sources are converted from.
     */
    public static final String RELATIVE_SRCDIR_ATTRIBUTE = 'gradle-relative-srcdir'

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

    /**
     * Adds the {@code gradle-relative-srcdir} attribute for the documents in one directory, unless the attributes
     * already contain it.
     *
     * @param attributes Attributes of the conversion.
     * @param relPath Path of the directory of the documents, relative to the directory the sources are converted
     *   from. Empty for that directory itself.
     *
     * @return The attributes, with {@code gradle-relative-srcdir}.
     */
    static Map<String, String> withRelativeSrcDir(Map<String, String> attributes, String relPath) {
        if (attributes.containsKey(RELATIVE_SRCDIR_ATTRIBUTE)) {
            return attributes
        }

        final Map<String, String> withRelativeSrcDir = new LinkedHashMap<>(attributes)
        withRelativeSrcDir.put(RELATIVE_SRCDIR_ATTRIBUTE, relativeSrcDir(relPath))
        withRelativeSrcDir
    }

    /**
     * The path from a directory back to the directory the sources are converted from, using {@code /} on every
     * platform.
     *
     * @param relPath Path of the directory, relative to the directory the sources are converted from.
     *
     * @return {@code .} for that directory itself, otherwise {@code ..} for each level, such as {@code ../..}.
     */
    static String relativeSrcDir(String relPath) {
        final levels = relPath.split(/[\/\\]/).findAll { !it.empty }.size()
        levels == 0 ? '.' : (['..'] * levels).join('/')
    }
}
