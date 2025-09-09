/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.core.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.internal.toolchains.ToolchainInfo
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorPublication
import org.asciidoctor.gradle.model5.core.waitingroom.ShowAsciidocToolchains
import org.asciidoctor.gradle.model5.editorconfig.AsciidoctorEditorConfigGenerator
import org.asciidoctor.gradle.model5.editorconfig.AsciidoctorEditorConfigPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.TASK_PREFIX

/**
 * Plugin that provides the {@code asciidoc} extension and the {@code showAsciidoctorToolchains} task.
 * It also reacts to the {@code org.asciidoctor.editorconfig} plugin.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorCoreBasePlugin implements Plugin<Project> {
    public final static String INTERMEDIATE_RESOURCE_PATH = 'META-INF/asciidoctor.gradle'
    public final static String TOOLCHAIN_DISPLAY_TASK = "show${TASK_PREFIX.capitalize()}Toolchains"

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(GrolifantServicePlugin)
        }

        final asciidoc = project.extensions.create(
            AsciidoctorModelExtension.NAME,
            AsciidoctorModelExtension,
            project
        )

        final satp = project.provider { ->
            ToolchainInfo.buildFrom(asciidoc)
        }

        final satTask = project.tasks.register(TOOLCHAIN_DISPLAY_TASK, ShowAsciidocToolchains, satp)
        satTask.configure {
            it.group = 'help'
            it.description = 'Displays registered Asciidoctor toolchains.'
        }

        configureForAsciidoctorEditorConfig(project, asciidoc)
    }

    private void configureForAsciidoctorEditorConfig(Project project, AsciidoctorModelExtension asciidoc) {
        project.pluginManager.withPlugin('org.asciidoctor.editorconfig') {
            asciidoc.publications.whenObjectAdded { AsciidoctorPublication pub ->
                project.tasks.named(AsciidoctorEditorConfigPlugin.DEFAULT_TASK_NAME, AsciidoctorEditorConfigGenerator) {
                    it.attributes(pub.sourceSet.attributes.attributeResolver)
                }
            }
        }
    }
}
