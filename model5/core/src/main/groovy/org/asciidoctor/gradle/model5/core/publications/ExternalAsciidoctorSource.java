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

import java.util.List;

/**
 * For defining external sources.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface ExternalAsciidoctorSource extends ExternalSource, HasAsciidoctorSource, HasAsciidoctorResources {
    /**
     * Links the input of this source set to one of the outputs on another source set.
     *
     * @param publicationName Name of publication.
     * @param formatterOrAliasName Name of the output or in cases where the output has an alias, the alias name.
     *
     * @since 5.1
     */
    void fromPublication(String publicationName, String formatterOrAliasName);
}
