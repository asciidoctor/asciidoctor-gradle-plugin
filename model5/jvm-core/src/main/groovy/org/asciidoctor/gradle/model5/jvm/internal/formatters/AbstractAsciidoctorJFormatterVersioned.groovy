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
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.provider.Property

@CompileStatic
abstract class AbstractAsciidoctorJFormatterVersioned extends AbstractAsciidoctorJFormatter {
    private final Property<String> moduleVersion

    void useVersion(Object ver) {

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

    protected AbstractAsciidoctorJFormatterVersioned(
            String name,
            String backendName,
            AsciidoctorjToolchain tc,
            Project project
    ) {
        super(name, backendName, tc, project)
        this.moduleVersion = project.objects.property(String)
    }
}
