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
package org.asciidoctor.gradle.model5.core.internal.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorPublication
import org.gradle.api.tasks.util.PatternSet
import org.ysb33r.grolifant5.api.core.FileSystemOperations

/**
 * Utilities for building tasks and naming things for publications.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class PublicationUtils {
    public static final String TASK_PREFIX = 'asciidoctor'
    public static final String CACHE_SUBDIR_BASE = '.asciidoctor-cache'
    public static final String DEFAULT_PUBLICATION = 'main'
    public static final String UNDERSCORE_LED_FILES = '**/_*'
    public static final PatternSet UNDERSCORE_LED_PATTERN = new PatternSet().exclude(UNDERSCORE_LED_FILES)
    public static final String GROUP_NAME = 'Documentation'
    public static final List<String> ASCIIDOC_PATTERNS = [
        '**/*.adoc',
        '**/*.ad',
        '**/*.asc',
        '**/*.asciidoc'
    ].asImmutable()

    /**
     * The relative source path for a publication
     * @param pub Publication.
     * @return The relative source path from the project directory.
     */
    static String sourcePathFor(AsciidoctorPublication pub) {
        sourcePathFor(pub.name)
    }

    /**
     * The relative source path for a publication
     * @param name Publication name.
     * @return The relative source path from the project directory.
     */
    static String sourcePathFor(String name) {
        name == DEFAULT_PUBLICATION ? 'src/docs/asciidoc' : "src/docs/asciidoc${name.capitalize()}"
    }

    /**
     * A relative output path for a publication output.
     *
     * @param fsOperations An instance of {@link FileSystemOperations}.
     * @param publicationName The name of the publication.
     * @param outputName The name of the output.
     * @return Relative path below build directory.
     */
    static String outputPathFor(FileSystemOperations fsOperations, String publicationName, String outputName) {
        if (publicationName == DEFAULT_PUBLICATION) {
            "docs/asciidoc/${outputName}"
        } else {
            "docs/asciidoc${fsOperations.toSafeFileName(publicationName).capitalize()}/${outputName}"
        }
    }

    /**
     * The name of the task for converting asciidoc to a specific output format.
     *
     * @param publicationName Name of publication.
     * @param outputName Name of output
     * @return Task name.
     */
    static String conversionTaskName(String publicationName, String outputName) {
        if (publicationName == DEFAULT_PUBLICATION) {
            "${TASK_PREFIX}${outputName.capitalize()}"
        } else {
            "${TASK_PREFIX}${publicationName.capitalize()}${outputName.capitalize()}"
        }
    }
}
