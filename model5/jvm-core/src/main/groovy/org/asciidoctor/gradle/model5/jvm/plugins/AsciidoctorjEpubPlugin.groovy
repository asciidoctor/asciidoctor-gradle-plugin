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
// tag::hacking-asciidoctorj-output-formatter[]
package org.asciidoctor.gradle.model5.jvm.plugins

import groovy.transform.CompileStatic

import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePdfThemesPlugin
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjEpub
import org.asciidoctor.gradle.model5.jvm.internal.formatters.AsciidoctorjEpubFactory
import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjEpub
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.model5.jvm.JvmModel.registerOutputFormatterFactory
import static org.asciidoctor.gradle.model5.jvm.JvmModel.registerOutputFormatterOnAllToolchains

// end::hacking-asciidoctorj-output-formatter[]

/**
 * The {@code asciidoctorj} plugin applies the base plugin, then creates a toolchain called {@code asciidoctorj}
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
// tag::hacking-asciidoctorj-output-formatter[]
@CompileStatic
class AsciidoctorjEpubPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AsciidoctorjPlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)
        final toolchains = asciidoc.toolchains

        registerOutputFormatterFactory(
                toolchains,
                AsciidoctorjEpub, // <.>
                AsciidoctorjEpubFactory, // <.>
                project.objects
        )

        project.pluginManager.withPlugin(AsciidoctorjPlugin.PLUGIN_ID) {
            registerOutputFormatterOnAllToolchains(
                    toolchains,
                    AsciidoctorjEpub, // <.>
                    DefaultAsciidoctorjEpub.DEFAULT_NAME // <.>
            )
        }
    }
}
// end::hacking-asciidoctorj-output-formatter[]
