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
package org.asciidoctor.gradle.jvm.epub

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


/** Provides additional conventions for building EPUBs.
 *
 * <ul>
 *   <li>Creates a task called {@code asciidoctorEpub}.
 *   <li>Sets a default version for asciidoctor-epub.
 * </ul>
 *
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorJEpubPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.with {
            apply plugin : 'org.asciidoctor.jvm.base'
            apply plugin : 'org.asciidoctor.kindlegen.base'

            AsciidoctorEpubTask task = tasks.create('asciidoctorEpub', AsciidoctorEpubTask)
            task.outputDir = { "${project.buildDir}/docs/asciidocEpub"}
            extensions.getByType(AsciidoctorJExtension).epubVersion = AsciidoctorJExtension.DEFAULT_EPUB_VERSION

        }
    }
}
