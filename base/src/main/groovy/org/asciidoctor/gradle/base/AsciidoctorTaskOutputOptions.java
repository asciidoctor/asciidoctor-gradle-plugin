/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.base;

import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectories;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Asciidoctor conversion output options.
 *
 * @authpr Schalk W. Cronje
 * @since 4.0
 */
public interface AsciidoctorTaskOutputOptions {

    /**
     * The current set of configured Asciidoctor backends.
     *
     * @return Backends. Can be empty. Never null.
     */
    Set<String> backends();

    /**
     * Copies all resources to the output directory.
     *
     * Some backends (such as {@code html5}) require all resources to be copied to the output directory.
     * This is the default behaviour for this task.
     */
    void copyAllResources();

    /** Do not copy any resources to the output directory.
     *
     * Some backends (such as {@code pdf}) process all resources in place.
     *
     */
    void copyNoResources();

    /**
     * Copy resources for a backend name.
     *
     * @param backendName Name of backend for which resources are copied
     * @param sourceDir Source directory of resources
     * @param outputDir Final output directory.
     * @param includeLang If set also copy resources for this specified language
     */
    void copyResourcesByBackend(
            final String backendName,
            final File sourceDir,
            final File outputDir,
            Optional<String> includeLang
    );

    /**
     * Copy resources to the output directory only if the backend names matches any of the specified
     * names.
     *
     * @param backendNames List of names for which resources should be copied.
     *
     */
    void copyResourcesOnlyIf(String... backendNames);

    /**
     * Returns a list of all output directories by backend
     *
     * @return Output directories
     */
    @OutputDirectories
    Set<File> getBackendOutputDirectories();

    /** List of backends for which to copy resources.
     *
     * @return List of backends. Can be {@code null}.
     */
    @Internal
    Optional<List<String>> getCopyResourcesForBackends();

    /**
     *
     * Get the output directory for a specific backend.
     *
     * @param backendName Name of backend
     * @return Output directory.
     */
    File getOutputDirForBackend(final String backendName);

    /**
     * Get the output directory for a specific backend.
     *
     * @param backendName Name of backend
     * @param language    Language for which sources are being generated.
     * @return Output directory.
     */
    File getOutputDirForBackend(final String backendName, final String language);

    /**
     * Access to the underlying {@link OutputOptions}.
     *
     * @return {@link OutputOptions} instance.
     */
    @Nested
    OutputOptions getOutputOptions();
}
