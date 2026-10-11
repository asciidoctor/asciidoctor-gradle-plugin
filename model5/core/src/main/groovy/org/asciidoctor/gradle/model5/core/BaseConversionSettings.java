/**
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
package org.asciidoctor.gradle.model5.core;

import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Describes the source documents, attributes etc. required for a conversion.
 *
 * <p>
 *     This is a focus on the conversions themselves, and not the engine for
 *     running the conversion.
 * </p>
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface BaseConversionSettings {
    /**
     * Source files.
     *
     * @return Set of files. Must be present and contain at least one document.
     */
    Provider<Set<File>> getSourceFiles();

    /**
     * The root directory where source files are located.
     *
     * @return Base directory for all source files. Must be present.
     */
    Provider<Directory> getSourceRootDir();

    /**
     * Destination directory.
     *
     * @return Directory for output. Must be present.
     */
    Provider<Directory> getDestinationDir();

    /**
     * Document attributes.
     *
     * @return All attributes. Must be present, but can be an empty map.
     */
    Provider<Map<String, String>> getAttributes();

    /**
     * Patterns of warnings which must be treated as errors.
     *
     * @return List of patterns. Must be present, but list can be empty.
     */
    Provider<Set<Pattern>> getFatalWarnings();

    /**
     * Task input property.
     *
     * @return A map that can be passed as an input property to a task.
     */
    Provider<Map<String,String>> getInputProperties();
}
