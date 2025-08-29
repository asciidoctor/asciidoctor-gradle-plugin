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
package org.asciidoctor.gradle.model5.core.publications;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.ysb33r.grolifant5.api.core.ClosureUtils;

import java.io.File;

/**
 * Source definitions.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface HasAsciidoctorSource {

    /**
     * The top directory for asciidoc sources for this publication.
     *
     * @return Provider to location.
     */
    Provider<Directory> getSourceDir();

    /**
     * The directory where the root of the Asciidoc documents will be found.
     *
     * <p>
     *   Usually this is {@code src/docs/asciidoc} or {@code src/docs/asciidoc<PUBLICATION>}.
     * </p>
     * @param srcDir New source directory
     */
    void setSourceDir(Object srcDir);

    /**
     * Configures sources.
     *
     * @param cfg Configuration closure. Is passed a {@link org.gradle.api.tasks.util.PatternSet}.
     */
    void sources(@DelegatesTo(PatternSet.class) Closure<?> cfg);

    /**
     * Configures sources.
     *
     * @param cfg Configuration {@link org.gradle.api.Action}. Is passed a {@link PatternSet}.
     */
    void sources(final Action<? super PatternSet> cfg);

    /**
     * Include source patterns.
     *
     * @param includePatterns ANT-style patterns for sources to include
     */
    void sources(String... includePatterns);

    /**
     * Clears existing sources patterns.
     */
    void clearSources();

    /**
     * A provider of source patterns.
     *
     * @return Patterns of files to include for Asciidoc sources.
     */
    Provider<PatternFilterable> getSourcePatterns();

//    /**
//     * Configures secondary sources.
//     *
//     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
//     */
//    void secondarySources(@DelegatesTo(PatternSet.class) Closure<?> cfg);
//
//    /**
//     * Configures sources.
//     *
//     * @param cfg Configuration {@link Action}. Is passed a {@link PatternSet}.
//     */
//    void secondarySources(final Action<? super PatternSet> cfg);
//
//    /**
//     * Clears any of the existing secondary source patterns.
//     *
//     * This should be used if none of the default patterns should be monitored.
//     */
//    void clearSecondarySources();
//
//    /**
//     * A provider of secondary source patterns.
//     *
//     * @return Patterns of non-Asciidoc source files to monitor.
//     */
//    Provider<PatternFilterable> getSecondarySourcePatterns();

}
