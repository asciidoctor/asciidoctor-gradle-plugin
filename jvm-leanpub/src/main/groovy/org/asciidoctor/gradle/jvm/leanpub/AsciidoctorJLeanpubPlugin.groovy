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
package org.asciidoctor.gradle.jvm.leanpub

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.grolifant.api.TaskProvider

import static org.asciidoctor.gradle.base.AsciidoctorUtils.setConvention

/** Provides additional conventions for creating Leanpub markdown.
 *
 * <ul>
 *   <li>Creates a task called {@code asciidoctorLeanpub}.
 *   <li>Sets a default version for asciidoctor-leanpub.
 * </ul>
 *
 * @author Schalk W. Cronjé
 * @author Gary Hale
 *
 * @since 3.0.0
 */
@CompileStatic
class AsciidoctorJLeanpubPlugin implements Plugin<Project> {

    final static String TASK_NAME = 'asciidoctorLeanpub'

    void apply(Project project) {
        project.with {
            apply plugin: AsciidoctorJBasePlugin

            Action leanpubDefaults = new Action<AsciidoctorLeanpubTask>() {
                @Override
                void execute(AsciidoctorLeanpubTask task) {
                    task.group = AsciidoctorJBasePlugin.TASK_GROUP
                    task.description = 'Convert AsciiDoc files to Leanpub-structured Markdown'
                    setConvention(task.project, task.sourceDirProperty,
                            project.layout.projectDirectory.dir('src/docs/asciidoc'))
                    setConvention(task.outputDirProperty,
                            task.project.layout.buildDirectory.dir('docs/asciidocLeanpub'))
                }
            }

            TaskProvider.registerTask(project, TASK_NAME, AsciidoctorLeanpubTask, leanpubDefaults)
            extensions.getByType(AsciidoctorJExtension).modules.leanpub.use()
        }
    }
}
