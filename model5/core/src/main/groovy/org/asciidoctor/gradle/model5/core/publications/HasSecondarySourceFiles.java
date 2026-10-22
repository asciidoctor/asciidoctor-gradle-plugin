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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

/**
 * Additional files that affect the up to date status of a task.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface HasSecondarySourceFiles {

    /**
     * Configures secondary sources.
     *
     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
     */
    void secondarySources(@DelegatesTo(PatternSet.class) Closure<?> cfg);

    /**
     * Configures secondary sources.
     *
     * @param cfg Configuration {@link Action}. Is passed a {@link PatternSet}.
     */
    void secondarySources(final Action<? super PatternSet> cfg);

    /**
     * Include secondary source patterns.
     *
     * @param includePatterns ANT-style patterns for sources to include
     */
    void secondarySources(String... includePatterns);

    /**
     * Clears existing secondary sources patterns.
     */
    void clearSecondarySources();

    /**
     * A provider of secondary source patterns.
     *
     * @return Patterns of files to include for secondary sources.
     */
    Provider<PatternFilterable> getSecondarySourcePatterns();
}
