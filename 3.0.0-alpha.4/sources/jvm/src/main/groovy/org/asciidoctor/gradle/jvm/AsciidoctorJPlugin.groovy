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
package org.asciidoctor.gradle.jvm

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.ysb33r.grolifant.api.TaskProvider.registerTask

/**
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorJPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.apply plugin: AsciidoctorJBasePlugin

        Action asciidoctorDefaults = new Action<AsciidoctorTask>() {
            @Override
            void execute(AsciidoctorTask asciidoctorTask) {
                asciidoctorTask.with {
                    group = AsciidoctorJBasePlugin.TASK_GROUP
                    description = 'Generic task to convert AsciiDoc files and copy related resources'
                }
            }
        }

        registerTask(project, 'asciidoctor', AsciidoctorTask, asciidoctorDefaults)
    }
}
