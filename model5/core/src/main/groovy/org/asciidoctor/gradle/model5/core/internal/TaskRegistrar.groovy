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
package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

import javax.inject.Inject

@CompileStatic
class TaskRegistrar {
    private final TaskContainer tasks

    @Inject
    TaskRegistrar(Project project) {
        this.tasks = project.tasks
    }

    public <T extends DefaultTask> TaskProvider<T> register(
            String name,
            Class<T> taskType,
            Action<T> configurator
    ) {
        if (tasks.names.contains(name)) {
            tasks.named(name, taskType)
        } else {
            tasks.register(name, taskType, configurator)
        }
    }
}
