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
package org.asciidoctor.gradle.model5.core.publications;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;

/**
 * Provides the necessary information on an external source to a
 * {@link org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTask}.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface ProvidedExternalSourceSet {

    /**
     * A collection of files from the external source area.
     * This can contain more than tjust AsciiDoc sources.
     *
     * @return Files
     */
    FileCollection getSourcesAndResources();

    /**
     * A provider of source patterns.
     *
     * @return Patterns of files to include for Asciidoc sources.
     */
    Provider<PatternFilterable> getSourcePatterns();

    /**
     * Patterns that can be added to a copy spec for copying resources to a target directory.
     *
     * @return Provider to patterns for a copy specification
     */
    Provider<PatternFilterable> getResourcesPatterns();

    /**
     * Whether the external source should be placed in a folder below the local source directory and also in the
     * destination directory.
     *
     * @return Provider to a subpath. Can be empty.
     */
    Provider<String> getInto();
}
