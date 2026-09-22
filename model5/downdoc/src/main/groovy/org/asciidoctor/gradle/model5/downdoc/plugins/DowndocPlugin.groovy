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
package org.asciidoctor.gradle.model5.downdoc.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin
import org.asciidoctor.gradle.model5.downdoc.DowndocModel
import org.asciidoctor.gradle.model5.downdoc.formatters.DowndocMarkdownOutputFormatter
import org.asciidoctor.gradle.model5.downdoc.internal.formatters.DefaultDowndocMarkdownOutputFormatter
import org.asciidoctor.gradle.model5.downdoc.toolchains.DowndocToolchain
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.gradle.jse.pnpm.toolchains.JsePnpmToolchain
import org.ysb33r.gradle.jsecosystem.JsEcosystemExtension

/**
 * The {@code downdoc} plugin applies the base plugin, then creates a toolchain called {@code asciidoctorj}
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DowndocPlugin implements Plugin<Project> {
    public static final String DEFAULT_TOOLCHAIN = 'downdoc'
    public static final String PLUGIN_ID = 'org.asciidoctor.downdoc'

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(DowndocBasePlugin)
            apply(AsciidoctorCorePlugin)
            apply('org.ysb33r.jsecosystem.pnpm.base')
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)
        final toolchains = asciidoc.toolchains
        final jsToolchains = project.extensions.getByType(JsEcosystemExtension).toolchains

        jsToolchains.create("${DEFAULT_TOOLCHAIN}Pnpm", JsePnpmToolchain) {
            it.withPnpmNode()
        }

        toolchains.create(DEFAULT_TOOLCHAIN, DowndocToolchain) {
            it.usePnpmToolchain("${DEFAULT_TOOLCHAIN}Pnpm")
        }

        DowndocModel.registerOutputFormatterOnAllToolchains(
                toolchains,
                DowndocMarkdownOutputFormatter,
                DefaultDowndocMarkdownOutputFormatter.DEFAULT_NAME
        )
    }
}
