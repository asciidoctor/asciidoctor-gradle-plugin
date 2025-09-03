/*
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
package org.asciidoctor.gradle.model5.editorconfig

import groovy.transform.CompileStatic
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Generates {@code .asciidoctorconfig} file.
 *
 * When the file is generated attributes are applied in the following order.
 * <ol>
 *     <li>Provided attributes</li>
 *     <li>Directly specified attributes</li>
 *     <li>Appending content of files which were specified as additional providers.</li>
 * </ol>
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorEditorConfigGenerator extends GrolifantDefaultTask {
    private final MapProperty<String, String> attrs
    private final MapProperty<String, String> providedAttributes
    private final List<Provider<File>> fileProviders = []
    private final Provider<RegularFile> outputFile
    private final DirectoryProperty outputDir

    AsciidoctorEditorConfigGenerator() {
        this.attrs = providerTools().mapProperty(String, String)
        this.providedAttributes = providerTools().mapProperty(String, String)
        this.fileProviders = []
        this.outputDir = project.objects.directoryProperty().convention(project.layout.projectDirectory)
        this.outputFile = this.outputDir.map { f -> f.file('.asciidoctorconfig') }

        inputs.property('provided-attrs', providedAttributes).optional(true)
        inputs.property('attrs', attrs).optional(true)
        inputs.files(fileProviders).optional().withPathSensitivity(PathSensitivity.NONE)
    }

    /**
     * Replace existing attributes with a new set.
     *
     * @param attrs Replacement attributes
     */
    void setAttributes(Map<String, ?> attrs) {
        this.attrs.value(stringTools().provideValuesKeepNull(attrs))
    }

    /**
     * Add more attributes to the existing set
     *
     * @param attrs Additional attributes.
     */
    void attributes(Map<String, ?> attrs) {
        this.attrs.putAll(stringTools().provideValuesKeepNull(attrs))
    }

    /**
     * Add more attributes to the existing set
     *
     * @param attrs Additional attributes.
     */
    void attributes(Provider<Map<String, ?>> attrProvider) {
        this.attrs.putAll(attrProvider.flatMap { stringTools().provideValuesKeepNull(it) })
    }

    /**
     * Add attribute content from a file.
     *
     * <p>
     *     The file must already be in a correct format.
     * </p>
     *
     */
    void attributesFromFile(Object file) {
        this.fileProviders.add(fsOperations().provideFile(file))
    }

    /**
     * Destination directory.  Defaults to the project directory.
     *
     * @return Directory
     */
    @Internal
    Provider<Directory> getDestinationDir() {
        this.outputDir
    }

    /**
     * Sets destination directory.
     *
     * @param dir Anything convertible to a directory using {@code project.file}.
     */
    void setDestinationDir(Object dir) {
        fsOperations().updateDirectoryProperty(this.outputDir, dir)
    }

    /**
     * Location of generated {@code .asciidoctorconfig} file.
     *
     * @return File location.
     */
    @OutputFile
    Provider<RegularFile> getOutputFile() {
        this.outputFile
    }

    @TaskAction
    void exec() {
        outputFile.get().asFile.withWriter { w ->
            Map<String, String> mergedAttrs = [:]
            mergedAttrs.putAll(mapper(providedAttributes.get()))
            mergedAttrs.putAll(mapper(attrs.get()))

            mergedAttrs.keySet().sort().each { String k ->
                w.println ":${k}: ${attrs[k]}"
            }

            fileProviders.each { prov ->
                w << prov.get().text
            }
        }
    }

    private Map<String, String> mapper(Map<String, String> mapOfAttrs) {
        mapOfAttrs.collectEntries { k, v ->
            [k, v ?: EMPTY]
        }
    }
}
