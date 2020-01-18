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
package org.asciidoctor.gradle.jvm.pdf

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.base.AsciidoctorUtils.setConvention
import static org.ysb33r.grolifant.api.TaskProvider.registerTask

/** Provides additional conventions for building PDFs.
 *
 * <ul>
 *   <li>Creates a task called {@code asciidoctorPdf}.
 *   <li>Sets a default version for asciidoctor-pdf.
 * </ul>
 *
 * @author Schalk W. Cronjé
 * @author Gary Hale
 *
 * @since 2.0.0
 */
@CompileStatic
class AsciidoctorJPdfPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.with {
            apply plugin: AsciidoctorJBasePlugin

            extensions.create(AsciidoctorPdfThemesExtension.NAME, AsciidoctorPdfThemesExtension, project)
            extensions.getByType(AsciidoctorJExtension).modules.pdf.use()

            Action pdfDefaults = new Action<AsciidoctorPdfTask>() {
                @Override
                void execute(AsciidoctorPdfTask task) {
                    task.with {
                        group = AsciidoctorJBasePlugin.TASK_GROUP
                        description = 'Convert AsciiDoc files to PDF format'
                        setConvention(project, sourceDirProperty,
                                project.layout.projectDirectory.dir('src/docs/asciidoc'))
                        setConvention(outputDirProperty, project.layout.buildDirectory.dir('docs/asciidocPdf'))
                    }
                }
            }
            registerTask(project, 'asciidoctorPdf', AsciidoctorPdfTask, pdfDefaults)
        }
    }
}
