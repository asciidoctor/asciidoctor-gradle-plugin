/*
 * Copyright 2013-2018 the original author or authors.
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
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Provides additional conventions for building PDFs.
 *
 * <ul>
 *   <li>Creates a task called {@code asciidoctorPdf}.
 *   <li>Sets a default version for asciidoctor-pdf.
 * </ul>
 *
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorJPdfPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.with {
            apply plugin: 'org.asciidoctor.jvm.base'

            extensions.create(AsciidoctorPdfThemesExtension.NAME, AsciidoctorPdfThemesExtension, project)

            AsciidoctorPdfTask task = tasks.create('asciidoctorPdf', AsciidoctorPdfTask)
            task.group = AsciidoctorJBasePlugin.TASK_GROUP
            task.description = 'Convert AsciiDoc files to PDF format'
            task.outputDir = { "${project.buildDir}/docs/asciidocPdf" }
            extensions.getByType(AsciidoctorJExtension).pdfVersion = AsciidoctorJExtension.DEFAULT_PDF_VERSION

        }
    }
}
