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

import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.provider.Provider;

import java.util.List;

/**
 * Defines how external sources are managed.
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface HasExternalSources {
    /**
     * Defines how duplicates between local and external sources are managed.
     *
     * @param strategy Instance of {@link org.gradle.api.file.DuplicatesStrategy}.
     */
    void setDuplicatesStrategy(DuplicatesStrategy strategy);

    /**
     * A list of all the tasks that build the external sources.
     *
     * @return Provider to list. Can be empty, but never {@code null}.
     */
    Provider<List<Object>> getExternalSourcesBuiltBy();

    /**
     * All external sources.
     *
     * @return Provider to all defined external sources.
     */
    Provider<ProvidedExternalSources> getExternalSources();
}
