/*
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.kramdown.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.kramdown.KramdownModelExtension
import org.asciidoctor.gradle.kramdown.tasks.KramdownAsciidocTask
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.ConfigurationPhaseOperations

/**
 * {@code Kramdown} base plugin.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class KramdownPlugin implements Plugin<Project> {
    public static final String DEFAULT_TOOLCHAIN = 'kramdown'

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(KramdownBasePlugin)
        }

        project.extensions.getByType(KramdownModelExtension).toolchains.create(DEFAULT_TOOLCHAIN)

        final allTasks = project.tasks.withType(KramdownAsciidocTask)
        ConfigurationPhaseOperations.from(project).taskTools().whenNamed(AsciidoctorCorePlugin.CONVERT_ALL_TASK) {
            it.dependsOn(allTasks)
        }
    }
}
