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
package org.asciidoctor.gradle.model5.core.tasks;

import org.asciidoctor.gradle.model5.core.publications.ProvidedExternalSources;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Basic methods a conversion task should implement.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface ConversionTaskBaseMethods {
    /**
     * Sets the source directory for actual sources.
     *
     * @param dir Provider to a directory.
     */
    void setSourceDir(Provider<Directory> dir);

    /**
     * The source patterns to look for in the source directory.
     *
     * @param patterns Provider of patterns
     */
    void setSourcePatterns(Provider<PatternFilterable> patterns);

    /**
     * The set of patterns to check for fatal warnings.
     *
     * @param patterns Set of patterns
     */
    void setFatalWarnings(Provider<Set<Pattern>> patterns);

    /**
     * External sources.
     *
     * @param externalSources Provider to external sources.
     */
    void setExternalSources(Provider<? extends ProvidedExternalSources> externalSources);
}
