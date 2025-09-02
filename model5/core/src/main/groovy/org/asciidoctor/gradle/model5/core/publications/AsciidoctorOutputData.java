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

import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend;
import org.asciidoctor.gradle.model5.core.DocType;
import org.asciidoctor.gradle.model5.core.SafeMode;
import org.gradle.api.Named;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;

import java.util.Set;

/**
 * Holds data regarding a publication output.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorOutputData extends Named {
    /**
     * Directory where output will be written to.
     *
     * @return Provider to directory.
     */
    Provider<Directory> getOutputDir();

    /**
     * The named backend for this formatter.
     *
     * @return Provider to the named backend.
     */
    Provider<AsciidoctorNamedBackend> getBackend();

    /**
     * Whether resources should be copied.
     *
     * @return A provider to a copy spec. Can be empty if resources should not be copied to the output directory.
     */
    Provider<PatternFilterable> getCopyResources();

    /**
     * Get the documentation type that needs to be based to the engine at run time.
     *
     * @return Documentation type. Can be empty.
     */
    Provider<DocType> getDocType();

    /**
     * List modules which need to be explicitly called out as being required.
     *
     * @return Provider to a list. Can be empty, but never {@code null}.
     */
    Provider<Set<String>> getModuleRequires();

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
     * Additional classpath to add for execution.
     *
     * @return Classpath
     */
    FileCollection getAdditionalClasspath();
}
