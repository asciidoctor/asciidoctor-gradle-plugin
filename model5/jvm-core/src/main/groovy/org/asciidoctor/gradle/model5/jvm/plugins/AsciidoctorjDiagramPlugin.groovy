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
package org.asciidoctor.gradle.model5.jvm.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjDiagram
import org.asciidoctor.gradle.model5.jvm.internal.extensions.DefaultAsciidoctorjDiagram
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.model5.jvm.JvmModel.registerExtensionFactory
import static org.asciidoctor.gradle.model5.jvm.JvmModel.registerExtensionOnAllToolchains

/**
 * Applies {@link AsciidoctorjBasePlugin} , then adds an extension for
 * {@code asciidoctorj-diagram}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjDiagramPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AsciidoctorjBasePlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)
        final toolchains = asciidoc.toolchains

        registerExtensionFactory(
            asciidoc.toolchains,
            AsciidoctorjDiagram,
            DefaultAsciidoctorjDiagram.Factory,
            project.objects
        )

        project.pluginManager.withPlugin(AsciidoctorjPlugin.PLUGIN_ID) {
            registerExtensionOnAllToolchains(toolchains, AsciidoctorjDiagram, DefaultAsciidoctorjDiagram.DEFAULT_NAME)
        }
    }
}
