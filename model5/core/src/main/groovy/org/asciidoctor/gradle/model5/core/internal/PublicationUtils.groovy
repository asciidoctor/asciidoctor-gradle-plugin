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
package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.waitingroom.AsciidoctorPublication
import org.gradle.api.tasks.util.PatternSet

/**
 * Utilities for building tasks and naming things for publications.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class PublicationUtils {
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
}
