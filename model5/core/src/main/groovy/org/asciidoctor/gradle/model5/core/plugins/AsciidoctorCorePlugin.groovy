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
package org.asciidoctor.gradle.model5.core.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Conventions plugin which adds a main publication and a task to run all {@link AsciidoctorTask} tasks.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorCorePlugin implements Plugin<Project> {
    public final static String CONVERT_ALL_TASK = "${PublicationUtils.TASK_PREFIX}All"

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AsciidoctorCoreBasePlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)

        asciidoc.publications.create(PublicationUtils.DEFAULT_PUBLICATION)

        final allTasks = project.tasks.withType(AsciidoctorTask)
        project.tasks.register(CONVERT_ALL_TASK) {
            it.group = PublicationUtils.GROUP_NAME
            it.description = 'Runs all asciidoc conversions'
            it.dependsOn(allTasks)
        }
    }
}
