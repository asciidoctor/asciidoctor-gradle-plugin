/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.jvm.epub

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.base.AsciidoctorUtils.setConvention

/**
 * Provides additional conventions for building EPUBs.
 *
 * <ul>
 *   <li>Creates a task called {@code asciidoctorEpub}.
 *   <li>Sets a default version for asciidoctor-epub.
 * </ul>
 *
 * @author Schalk W. Cronjé
 * @author Gary Hale
 *
 * @since 2.0.0
 */
@CompileStatic
class AsciidoctorJEpubPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.pluginManager.apply(AsciidoctorJBasePlugin)
        project.extensions.getByType(AsciidoctorJExtension).modules.epub.use()

        Action epubDefaults = new Action<AsciidoctorEpubTask>() {
            @Override
            void execute(AsciidoctorEpubTask task) {
                task.group = AsciidoctorJBasePlugin.TASK_GROUP
                task.description = 'Convert AsciiDoc files to EPUB3 formats'
                setConvention(project, task.sourceDirProperty,
                        project.layout.projectDirectory.dir('src/docs/asciidoc'))
                setConvention(task.outputDirProperty,
                        task.project.layout.buildDirectory.dir('docs/asciidocEpub'))
            }
        }

        project.tasks.register('asciidoctorEpub', AsciidoctorEpubTask, epubDefaults)
    }
}
