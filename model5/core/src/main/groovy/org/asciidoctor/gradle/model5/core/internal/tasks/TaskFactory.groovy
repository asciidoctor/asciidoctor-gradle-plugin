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
package org.asciidoctor.gradle.model5.core.internal.tasks

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTask
import org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTaskMethods
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject

/**
 * Utility to register a task and configure it.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class TaskFactory {
    private final Project project

    @Inject
    TaskFactory(Project project) {
        this.project = project
    }

    TaskProvider<? extends AsciidoctorTask> registerConversionTask(
            final String taskName,
            Action<? extends AsciidoctorTaskMethods> configurator
    ) {
        project.tasks.register(taskName,AsciidoctorTask) {
            configurator.execute(it)
        }
    }
}
