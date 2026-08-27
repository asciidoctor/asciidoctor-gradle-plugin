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
import org.gradle.api.file.CopySpec;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;

/**
 * Configuration of Asciidoctor resource files.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface HasAsciidoctorResources {
    /**
     * Adds these patterns that are relative to the source directory or the intermediate source directory.
     *
     * @param cfg {@link PatternFilterable} instance that can be configured.
     */
    void resources(Action<? super PatternFilterable> cfg);

    /**
     * Adds these patterns that are relative to the source directory or the intermediate source directory.
     *
     * @param cfg A closre that can configure a {@link PatternFilterable} instance.
     */
    void resources(@DelegatesTo(PatternFilterable.class) Closure<?> cfg);

    /**
     * Patterns that can be added to a copy spec for copying resources to a target directory.
     *
     * @return Provider to patterns for a copy specification
     */
    Provider<PatternFilterable> getResourcesPatterns();
}
