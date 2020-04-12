/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.editorconfig

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorAttributeProvider
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.Callable

import static org.ysb33r.grolifant.api.MapUtils.stringizeValues

/** Generates {@code .asciidoctorconfig} file.
 *
 * When the file is generated attributes are applied in the following order.
 * <ol>
 *     <li>Directly specified attributes</li>
 *     <li>Attributes obtaining via {@link AsciidoctorAttributeProvider} instances</li>
 *     <li>Appending content of files which were specified as additional providers.</li>
 * </ol>
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.2.0
 */
@CompileStatic
class AsciidoctorEditorConfigGenerator extends DefaultTask {

    private final Map<String, Object> attributes = [:]
    private final List<Provider<File>> fileProviders = []
    private final List<Provider<Map<String, String>>> attributeProviders = []
    private final Provider<File> outputFile
    private Object outputDir

    AsciidoctorEditorConfigGenerator() {
        this.outputDir = project.projectDir
        this.outputFile = project.provider({
            new File(destinationDir, '.asciidoctorconfig')
        } as Callable<File>)
    }

    /** Replace existing attributes with a new set.
     *
     * @param attrs Replacement attributes
     */
    void setAttributes(Map<String, Object> attrs) {
        this.attributes.clear()
        this.attributes.putAll(attrs)
    }

    /** Add more attributes to the existing set
     *
     * @param attrs Additional attributes.
     */
    void attributes(Map<String, Object> attrs) {
        this.attributes.putAll(attrs)
    }

    /** Returns the set of attributes after values have been converted to strings.
     *
     * @return Attributes as key-value pairs.
     */
    @Input
    Map<String, String> getAttributes() {
        stringizeValues(this.attributes)
    }

    /** Add an additional attribute provider.
     *
     * A provider can be a file of something that implements {@link AsciidoctorAttributeProvider} (such as
     * an {@code asciidoctorj} or {@code asciidoctorjs extension}).
     *
     *
     * @param attrs Anything convertible to a file using {@code project.file} or that implements
     * {@link AsciidoctorAttributeProvider}.
     */
    void additionalAttributes(Object attrs) {
        switch (attrs) {
            case AsciidoctorAttributeProvider:
                this.attributeProviders.add(project.provider({
                    stringizeValues(((AsciidoctorAttributeProvider) attrs).attributes)
                } as Callable<Map<String, String>>))
                break
            default:
                this.fileProviders.add(project.provider({
                    project.file(attrs)
                } as Callable<File>))
        }
    }

    /** Returns list of file providers.
     *
     * Content of these files will simply be appended to the genrated content.
     *
     * @return List of file providers
     */
    @InputFiles
    List<Provider<File>> getAdditionalFileProviders() {
        this.fileProviders
    }

    /** Returns list of attribute providers. THese providers will return attributes as key-value pairs.
     *
     * @return List of attribute providers
     */
    @Input
    List<Provider<Map<String, String>>> getAdditionalAttributeProviders() {
        this.attributeProviders
    }

    /** Destination directory.  Defaults to the project directory.
     *
     * @return Directory
     */
    @Internal
    File getDestinationDir() {
        project.file(this.outputDir)
    }

    /** Sets destination directory.
     *
     * @param dir Anything convertible to a directory using {@code project.file}.
     */
    void setDestinationDir(Object dir) {
        this.outputDir = dir
    }

    /** Location of generated {@code .asciidoctorconfig} file.
     *
     * @return File location.
     */
    @OutputFile
    Provider<File> getOutputFile() {
        this.outputFile
    }

    @TaskAction
    void exec() {
        outputFile.get().withWriter { w ->
            getAttributes().each { k, v ->
                w.println ":${k}: ${v}"
            }

            additionalAttributeProviders.each { prov ->
                prov.get().each { k, v ->
                    w.println ":${k}: ${v}"
                }
            }

            additionalFileProviders.each { prov ->
                w << prov.get().text
            }
        }
    }
}
