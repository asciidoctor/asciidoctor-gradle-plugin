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
package org.asciidoctor.gradle.adocfmt.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.adocfmt.tasks.AdocFmtCheckTask
import org.asciidoctor.gradle.adocfmt.tasks.AdocFmtTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin

/**
 * Registers two lifecycle tasks called {@code adocFormatAll} and {@code adocFormatCheckAll}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.2
 */
@CompileStatic
class AdocFmtPlugin implements Plugin<Project> {
    public static final String FORMAT_TASK = 'asciidoctorFormatAll'
    public static final String CHECK_TASK = 'asciidoctorFormatCheckAll'

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AdocFmtBasePlugin)
            apply(LifecycleBasePlugin)
        }

        final formatTasks = project.tasks.withType(AdocFmtTask)
        final checkTasks = project.tasks.withType(AdocFmtCheckTask)

        project.tasks.register(FORMAT_TASK) {
            it.group = 'Formatting'
            it.description = 'Runs all AsciiDoc formatting tasks'
            it.dependsOn(formatTasks)
        }

        project.tasks.register(CHECK_TASK) {
            it.group = LifecycleBasePlugin.VERIFICATION_GROUP
            it.description = 'Runs all AsciiDoc format check tasks'
            it.dependsOn(checkTasks)
        }

        project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
            it.dependsOn(CHECK_TASK)
        }

        project.pluginManager.withPlugin('org.ysb33r.lifecycles.base') {
            project.tasks.named('formatAll') {
                it.dependsOn(FORMAT_TASK)
            }
        }
    }
}
