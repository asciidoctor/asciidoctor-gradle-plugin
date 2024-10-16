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
package org.asciidoctor.gradle.jvm.slides

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.gems.AsciidoctorGemPrepare
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

import static org.asciidoctor.gradle.base.AsciidoctorUtils.setConvention
import static org.asciidoctor.gradle.jvm.gems.AsciidoctorGemSupportPlugin.GEMPREP_TASK

/** Adds an extension and task to create Reveal.js slides.
 *
 * @author Schalk W. Cronjé
 * @author Gary Hale
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorRevealJSPlugin implements Plugin<Project> {

    public final static String REVEALJS_TASK = 'asciidoctorRevealJs'

    @Override
    @SuppressWarnings('LineLength')
    void apply(Project project) {
        project.apply plugin: AsciidoctorRevealJSBasePlugin

        TaskProvider<AsciidoctorGemPrepare> gemPrepare = project.tasks.named(GEMPREP_TASK, AsciidoctorGemPrepare)
        project.tasks.register(REVEALJS_TASK, AsciidoctorJRevealJSTask) { revealTask ->
            revealTask.with {
                dependsOn(gemPrepare)
                setConvention(project, sourceDirProperty, project.layout.projectDirectory.dir('src/docs/asciidoc'))
                setConvention(outputDirProperty, project.layout.buildDirectory.dir('docs/asciidocRevealJs'))
            }
        }
    }
}
