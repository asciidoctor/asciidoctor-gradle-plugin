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
package org.asciidoctor.gradle.base.internal

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.asciidoctor.gradle.base.AsciidoctorMultiLanguageException
import org.asciidoctor.gradle.base.AsciidoctorTaskFileOperations
import org.asciidoctor.gradle.base.AsciidoctorTaskOutputOptions
import org.asciidoctor.gradle.base.OutputOptions
import org.asciidoctor.gradle.base.Transform
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileTree
import org.ysb33r.grolifant.api.core.ProjectOperations

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * Implementation of output options for Asciidoctor tasks
 *
 * @author Schalk W. Cronjé
 *
 * @since 4.0
 */
@CompileStatic
@Slf4j
class DefaultAsciidoctorOutputOptions implements AsciidoctorTaskOutputOptions {

    private final AsciidoctorTaskFileOperations fileOperations
    private final OutputOptions configuredOutputOptions = new OutputOptions()
    private final ProjectOperations po
    private final String taskName
    private List<String> copyResourcesForBackendsList = []

    DefaultAsciidoctorOutputOptions(
            ProjectOperations po,
            String taskName,
            AsciidoctorTaskFileOperations atfo
    ) {
        this.po = po
        this.fileOperations = atfo
        this.taskName = taskName
    }

    /**
     * The current set of configured Asciidoctor backends.
     *
     * @return Backends. Can be empty. Never null.
     */
    @Override
    Set<String> backends() {
        configuredOutputOptions.backends
    }

    /**
     * Configures output options for this task.
     *
     * @param cfg Closure which will delegate to a {@link org.asciidoctor.gradle.base.OutputOptions} instance.
     */
    void configureOutputOptions(@DelegatesTo(OutputOptions) Closure cfg) {
        Closure configurator = (Closure) cfg.clone()
        configurator.delegate = this.configuredOutputOptions
        configurator.resolveStrategy = DELEGATE_FIRST
        configurator.call()
    }

    /** Configures output options for this task.
     *
     * @param cfg Action which will be passed an instances of {@link org.asciidoctor.gradle.base.OutputOptions}
     *   to configure.
     */
    void configureOutputOptions(Action<OutputOptions> cfg) {
        cfg.execute(this.configuredOutputOptions)
    }

    /**
     * Copies all resources to the output directory.
     *
     * Some backends (such as {@code html5}) require all resources to be copied to the output directory.
     * This is the default behaviour for this task.
     */
    @Override
    void copyAllResources() {
        this.copyResourcesForBackendsList = []
    }

    /**
     * Do not copy any resources to the output directory.
     *
     * Some backends (such as {@code pdf}) process all resources in place.
     */
    @Override
    void copyNoResources() {
        this.copyResourcesForBackendsList = null
    }

    /**
     * Copy resources for a backend.
     *
     * @param backendName Name of backend for which resources are copied
     * @param sourceDir Source directory of resources
     * @param outputDir Final output directory.
     * @param includeLang If set also copy resources for this specified language
     */
    @Override
    void copyResourcesByBackend(String backendName, File sourceDir, File outputDir, Optional<String> includeLang) {
        if (copyResourcesForBackendsList != null &&
                (copyResourcesForBackendsList.empty || backendName in copyResourcesForBackendsList)
        ) {
            CopySpec rcs = fileOperations.getResourceCopySpec(includeLang)
            log.info "Copy resources for '${backendName}' to ${outputDir}"

            FileTree ps = fileOperations.intermediateArtifactPatternProvider.present ?
                    po.fileTree(sourceDir).matching(fileOperations.intermediateArtifactPatternProvider.get()) :
                    null

            CopySpec langSpec = includeLang.present ?
                    fileOperations.getLanguageResourceCopySpec(includeLang.get()) :
                    null

            po.copy(new Action<CopySpec>() {
                @Override
                void execute(CopySpec copySpec) {
                    copySpec.with {
                        into outputDir
                        with rcs

                        if (ps != null) {
                            from ps
                        }

                        if (langSpec) {
                            with langSpec
                        }
                    }
                }
            })
        }
    }

    /**
     * Copy resources to the output directory only if the backend names matches any of the specified
     * names.
     *
     * @param backendNames List of names for which resources should be copied.
     */
    @Override
    void copyResourcesOnlyIf(String... backendNames) {
        this.copyResourcesForBackendsList = []
        this.copyResourcesForBackendsList.addAll(backendNames)
    }

    /**
     * Returns a list of all output directories by backend
     *
     * @return Output directories
     */
    @Override
    Set<File> getBackendOutputDirectories() {
        if (fileOperations.languages.empty) {
            Transform.toSet(configuredOutputOptions.backends) {
                String it -> getOutputDirForBackend(it)
            }
        } else {
            configuredOutputOptions.backends.collectMany { String backend ->
                Transform.toList(fileOperations.languages) { String lang ->
                    getOutputDirForBackend(backend, lang)
                }
            }.toSet()
        }
    }

    /**
     * List of backends for which to copy resources.
     *
     * @return List of backends. Can be {@code null}.
     */
    @Override
    Optional<List<String>> getCopyResourcesForBackends() {
        Optional.ofNullable(this.copyResourcesForBackendsList) as Optional<List<String>>
    }

    /**
     * Get the output directory for a specific backend.
     *
     * @param backendName Name of backend
     * @return Output directory.
     */
    @Override
    File getOutputDirForBackend(String backendName) {
        if (!fileOperations.outputDirProperty.present) {
            throw new GradleException("outputDir has not been defined for task '${taskName}'")
        }
        if (!fileOperations.languages.empty) {
            throw new AsciidoctorMultiLanguageException('Use getOutputDir(backendname,language) instead.')
        }
        configuredOutputOptions.separateOutputDirs ? new File(fileOperations.outputDir, backendName) :
                fileOperations.outputDir
    }

    /**
     * Get the output directory for a specific backend.
     *
     * @param backendName Name of backend
     * @param language Language for which sources are being generated.
     * @return Output directory.
     */
    @Override
    File getOutputDirForBackend(String backendName, String language) {
        if (!fileOperations.outputDirProperty.present) {
            throw new GradleException("outputDir has not been defined for task '${taskName}'")
        }
        configuredOutputOptions.separateOutputDirs ?
                new File(fileOperations.outputDir, "${language}/${backendName}") :
                new File(fileOperations.outputDir, language)
    }

    /**
     * Access to the underlying {@link OutputOptions}.
     *
     * @return {@link OutputOptions} instance.
     */
    @Override
    OutputOptions getOutputOptions() {
        this.configuredOutputOptions
    }
}
