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
