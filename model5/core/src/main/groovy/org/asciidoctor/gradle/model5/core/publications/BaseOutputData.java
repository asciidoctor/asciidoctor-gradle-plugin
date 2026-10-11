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

import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend;
import org.asciidoctor.gradle.model5.core.ConversionTemplate;
import org.asciidoctor.gradle.model5.core.DocType;
import org.gradle.api.Named;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;

import java.util.Map;
import java.util.Set;

/**
 * Holds data regarding a publication output.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface BaseOutputData extends Named {
    /**
     * Directory where output will be written to.
     *
     * @return Provider to directory.
     */
    Provider<Directory> getOutputDir();

    /**
     * Whether resources should be copied.
     *
     * @return A provider to a copy spec. Can be empty if resources should not be copied to the output directory.
     */
    Provider<PatternFilterable> getCopyResources();

    /**
     * The actual name of the formatter, but not the alias name.
     * @return Provider to the name.
     */
    Provider<String> getFormatterName();

    /**
     * The name of the toolchain.
     *
     * @return Provider to the name.
     */
    Provider<String> getToolchainName();

    /**
     * Custom properties that can be passed to the engine.
     *
     * <p>This is rarely used and primarily aimed at engines which are not real Asciidoctor engines</p>
     *
     * @return Provider to a map of properties which could be interpreted by a specific engine.
     *
     * @since 5.1
     */
    Provider<Map<String,String>> getCustomProperties();

    /**
     * Attributes that can be passed to the engine.
     *
     * @return Provider to a map of attributes which could be interpreted by a specific engine.
     *
     * @since 5.1
     */
    Provider<Map<String,String>> getAttributes();
}
