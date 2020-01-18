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
package org.asciidoctor.gradle.base

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
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

/** Base plugin for all Asciidoctor plugins (J & JS).
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.0.0
 */
@CompileStatic
class AsciidoctorBasePlugin implements Plugin<Project> {
    private static final Pattern DEPS_TASK_PATTERN = ~/^(.+)Dependencies$/

    void apply(Project project) {
        project.apply plugin: 'base'
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
                        AbstractAsciidoctorBaseTask,
                        taskBaseName(matcher)
                    )

                    tasks.create(targetTaskName, DependencyReportTask, new Action<Task>() {
                        @Override
                        void execute(Task task) {
                            AbstractAsciidoctorBaseTask asciidoctorTask = (AbstractAsciidoctorBaseTask) associate.get()
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