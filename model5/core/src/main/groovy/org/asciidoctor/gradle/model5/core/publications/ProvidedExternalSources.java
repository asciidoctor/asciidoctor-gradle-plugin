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
 * The full set of provided external sources.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface ProvidedExternalSources {
    /**
     * The list of provided external sources.
     *
     * @return Provider toi external sources. Provider can be empty indicating not lists. Provider can also contain an
     *   empty list.
     */
    Provider<List<ProvidedExternalSourceSet>> getExternalSources();

    /**
     * How to deal with duplicates from various external sources.
     *
     * @return Strategy. Not {@code null} and not empty.
     */
    Provider<DuplicatesStrategy> getDuplicatesStrategy();
}
