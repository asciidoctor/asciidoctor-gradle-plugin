/**
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
package org.asciidoctor.gradle.model5.core;

import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Described the source documents, attributes etc required for a conversion.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorConversionSettings {
    /**
     * Source files.
     *
     * @return Set of files. Must be present and contain at least one document.
     */
    Provider<Set<File>> getSourceFiles();

    /**
     * Backend to use.
     *
     * @return Backend. Must be present.
     */
    Provider<AsciidoctorNamedBackend> getBackend();

    /**
     * The root directory where source files are located.
     *
     * @return Base directory for all source files. Must be present.
     */
    Provider<Directory> getSourceRootDir();

    /**
     * Base directory.
     *
     * @return Base directory as presented to Asciidoctor. Must be present.
     */
    Provider<Directory> getBaseDir();

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
     * Document type.
     *
     * @return Optional document type. Provider does not need to be present.
     */
    Provider<DocType> getDocType();
}
