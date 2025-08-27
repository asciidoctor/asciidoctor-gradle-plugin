/*
 * Copyright ${year} the original author or authors.
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
package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.tasks.ShowAsciidocToolchains
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin

@CompileStatic
class AsciidoctorCorePlugin implements Plugin<Project> {
    public final static String INTERMEDIATE_RESOURCE_PATH = 'META-INF/asciidoctor.gradle'
    public final static String TOOLCHAIN_DISPLAY_TASK = 'asciidoctorToolchains'

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(GrolifantServicePlugin)
        }

        project.extensions.create(AsciidoctorCoreExtension.NAME, AsciidoctorCoreExtension, project)

        project.tasks.register(TOOLCHAIN_DISPLAY_TASK, ShowAsciidocToolchains) {
            it.group = 'help'
            it.description = 'Displays registered Asciidoctor toolchains.'
        }
    }
}
