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
// tag::hacking-asciidoctorjs-output-formatter[]
package org.asciidoctor.gradle.model5.js.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsRevealjs
import org.asciidoctor.gradle.model5.js.internal.formatters.AsciidoctorjsRevealjsFactory
import org.asciidoctor.gradle.model5.js.internal.formatters.DefaultAsciidoctorjsRevealjs
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.model5.js.JsModel.registerOutputFormatterFactory
import static org.asciidoctor.gradle.model5.js.JsModel.registerOutputFormatterOnAllToolchains

// end::hacking-asciidoctorjs-output-formatter[]

/**
 * Adds the {@code asciidoctor.js} {@code reveal.js} formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
// tag::hacking-asciidoctorjs-output-formatter[]
@CompileStatic
class AsciidoctorjsRevealjsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AsciidoctorjsPlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)
        final toolchains = asciidoc.toolchains

        registerOutputFormatterFactory(
            toolchains,
            AsciidoctorjsRevealjs, // <.>
            AsciidoctorjsRevealjsFactory, // <.>
            project.objects
        )

        project.pluginManager.withPlugin(AsciidoctorjsPlugin.PLUGIN_ID) {
            registerOutputFormatterOnAllToolchains(
                toolchains,
                AsciidoctorjsRevealjs, // <.>
                DefaultAsciidoctorjsRevealjs.DEFAULT_NAME // <.>
            )
        }
    }
}
// end::hacking-asciidoctorjs-output-formatter[]
