/*
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
package org.asciidoctor.gradle.kramdown.internal.utils

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.kramdown.tasks.KramdownAsciidocTask
import org.asciidoctor.gradle.kramdown.tasks.KramdownAsciidocTaskMethods
import org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTask
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject

/**
 * Utility to register a task and configure it.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class TaskFactory {
    private final Project project

    @Inject
    TaskFactory(Project project) {
        this.project = project
    }

    TaskProvider<? extends KramdownAsciidocTask> registerConversionTask(
        final String taskName,
        Action<? extends KramdownAsciidocTaskMethods> configurator
    ) {
        project.tasks.register(taskName, KramdownAsciidocTask) {
            configurator.execute(it)
        }
    }

    /**
     * Looks up a task by name.
     *
     * @param taskName Name of task
     * @return Provider to an instance of {@link AsciidoctorTask}
     */
    TaskProvider<? extends KramdownAsciidocTask> lookupTask(final String taskName) {
        project.tasks.named(taskName, KramdownAsciidocTask)
    }

    void addPrerequisiteTasks(final String taskName, Iterable<String> prepareTasks) {
        project.tasks.named(taskName).configure { it.dependsOn(prepareTasks) }
    }

    void addPrerequisiteTasks(final String taskName, Provider<List<Object>> prepareTasks) {
        project.tasks.named(taskName).configure { it.dependsOn(prepareTasks) }
    }
}
