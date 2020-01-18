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
package org.asciidoctor.gradle.js.nodejs

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.grolifant.api.TaskProvider

/** Adds a task called asciidoctor.
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorNodeJSPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.with {
            apply plugin: AsciidoctorNodeJSBasePlugin

            Action<AsciidoctorTask> asciidoctorDefaults = new Action<AsciidoctorTask>() {
                @Override
                void execute(AsciidoctorTask asciidoctorTask) {
                    asciidoctorTask.with {
                        group = TASK_GROUP
                        description = 'Generic task to convert AsciiDoc files and copy related resources'
                    }
                }
            }

            TaskProvider.registerTask(
                project,
                'asciidoctor',
                AsciidoctorTask
            ).configure((Action) asciidoctorDefaults)
        }
    }
}