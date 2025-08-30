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
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjOutputFormatterVersioned
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ProjectOperations

@CompileStatic
abstract class AbstractAsciidoctorjFormatterVersioned extends AbstractAsciidoctorjFormatter
        implements AsciidoctorjOutputFormatterVersioned {


    protected final Property<String> moduleVersion
    private final ConfigurableFileCollection classpath

    @Override
    void useVersion(Object ver) {
        ccso.stringTools().updateStringProperty(this.moduleVersion, ver)
    }

    @Override
    FileCollection getClasspath() {
        this.classpath
    }

//    protected final Project project

//    /**
//     * Registers the tasks associated with this given output formatter, its toolchain and the corresponding publication.
//     *
//     * @param publication Publication
//     */
//    @Override
//    void registerTasksIfAbsent(AsciidoctorPublication publication) {
//        final taskName = ToolchainUtils.asciidoctorTaskName(toolchain,this,publication)
//        // Register the task by that name
//        project.tasks.register(taskName/*,AsciidoctorJTask*/) {t ->
//            t.group = PublicationUtils.GROUP_NAME
//            t.description = "Convert Asdiidoc source to format identified as '${name}'"
//
//            // configure a bunch of stuff from the publication.
//        }
//    }

    /**
     *
     * @param name Name of the output formatter.
     * @param backendName Name of the backend.
     * @param componentModule THe maven module notation, excluding the version
     * @param componentDefaultVersion THe default version of the component.
     * @param tc The toolchain the formatter is attached to.
     * @param tempProjectReference A temporary reference to a {@link Project} instance.
     */
    protected AbstractAsciidoctorjFormatterVersioned(
            String name,
            String backendName,
            String componentModule,
            Provider<String> componentDefaultVersion,
            AsciidoctorjToolchain tc,
            Project tempProjectReference
    ) {
        super(name, backendName, tc, tempProjectReference)
        this.moduleVersion = tempProjectReference.objects.property(String).convention(componentDefaultVersion)

        final cfgName = JvmModel.nameForOutputFormatterConfiguration(tc.name, name)
        final runtime = JvmModel.nameForOutputFormatterConfigurationResolvable(tc.name, name)

        ProjectOperations.find(tempProjectReference).configurations
                .createLocalRoleFocusedConfiguration(cfgName, runtime, true)
        tempProjectReference.dependencies.addProvider(
                cfgName,
                this.moduleVersion.map { "${componentModule}:${it}".toString()}
        )
        this.classpath = ccso.fsOperations().emptyFileCollection()
        this.classpath.from(tempProjectReference.configurations.getByName(runtime))
    }
}
