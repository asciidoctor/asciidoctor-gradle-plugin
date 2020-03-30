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
package org.asciidoctor.gradle.editorconfig

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Asciidoctor editorConfig plugin.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.2.0
 */
@CompileStatic
class AsciidoctorEditorConfigPlugin implements Plugin<Project> {
    public final static String DEFAULT_TASK_NAME = 'asciidoctorEditorConfig'

    @Override
    void apply(Project project) {
        AsciidoctorEditorConfigGenerator task = project.tasks.create(
            DEFAULT_TASK_NAME,
            AsciidoctorEditorConfigGenerator
        )
        configureIdea(task)
    }

    void configureIdea(AsciidoctorEditorConfigGenerator aecg) {
        Project project = aecg.project
        project.pluginManager.withPlugin('idea') {
            project.tasks.getByName('ideaModule').dependsOn aecg
        }
    }
}
