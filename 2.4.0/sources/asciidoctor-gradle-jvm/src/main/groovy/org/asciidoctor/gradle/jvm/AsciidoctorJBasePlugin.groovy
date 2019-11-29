/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.jvm

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorBasePlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.ysb33r.grolifant.api.TaskProvider

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.ysb33r.grolifant.api.TaskProvider.registerTask

/**
* @author Schalk W. Cronjé
*
* @since 2.0.0
 */
@CompileStatic
class AsciidoctorJBasePlugin implements Plugin<Project> {

    static final String TASK_GROUP = 'Documentation'
    static final String DEPS_REPORT = 'asciidoctorjDependencies'
    private static final Pattern DEPS_TASK_PATTERN = ~/^(.+)Dependencies$/

    void apply(Project project) {
        project.with {
            apply plugin: AsciidoctorBasePlugin

            AsciidoctorJExtension asciidoctorj = extensions.create(
                AsciidoctorJExtension.NAME,
                AsciidoctorJExtension,
                project
            )

            registerTask(project, DEPS_REPORT, DependencyReportTask, new Action<Task>() {
                @Override
                void execute(Task task) {
                    ((DependencyReportTask) task).configurations = [asciidoctorj.configuration].toSet()

                    task.doLast {
                        project.logger.warn "${task.name} is deprecated. " +
                            "Use pattern <asciidocTaskName>Dependencies instead to get specific task's dependencies"
                    }
                }
            })
        }

        registerDependencyReportRules(project)
    }

    private void registerDependencyReportRules(Project project) {
        TaskContainer tasks = project.tasks
        tasks.addRule(
            '<asciidocTaskName>Dependencies: Report dependencies for AsciidoctorJ tasks'
        ) { String targetTaskName ->
            Matcher matcher = targetTaskName =~ DEPS_TASK_PATTERN
            if (matcher.matches()) {
                try {
                    TaskProvider associate = TaskProvider.taskByTypeAndName(
                        project,
                        AbstractAsciidoctorTask,
                        taskBaseName(matcher)
                    )

                    tasks.create(targetTaskName, DependencyReportTask, new Action<Task>() {
                        @Override
                        void execute(Task task) {
                            AbstractAsciidoctorTask asciidoctorTask = (AbstractAsciidoctorTask) associate.get()
                            DependencyReportTask reportTask = (DependencyReportTask) task
                            reportTask.configurations = asciidoctorTask.reportableConfigurations
                        }
                    })
                } catch (UnknownTaskException e) {
                    return
                }
            }
        }
    }

    @CompileDynamic
    private String taskBaseName(Matcher matcher) {
        matcher[0][1]
    }

}
