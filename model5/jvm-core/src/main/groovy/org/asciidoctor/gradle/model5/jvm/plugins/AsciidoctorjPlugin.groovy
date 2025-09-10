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
package org.asciidoctor.gradle.model5.jvm.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjDocbook
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjManpage
import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjDocbook
import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjManpage
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.model5.jvm.JvmModel.registerOutputFormatterOnAllToolchains

/**
 * The {@code asciidoctorj} plugin applies the base plugin, then creates a toolchain called {@code asciidoctorj}
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjPlugin implements Plugin<Project> {
    public static final String DEFAULT_TOOLCHAIN = 'asciidoctorj'
    public static final String PLUGIN_ID = 'org.asciidoctor.jvm'

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AsciidoctorjBasePlugin)
            apply(AsciidoctorCorePlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)
        final toolchains = asciidoc.toolchains

        toolchains.create(DEFAULT_TOOLCHAIN, AsciidoctorjToolchain)

        registerOutputFormatterOnAllToolchains(toolchains, AsciidoctorjHtml5, DefaultAsciidoctorjHtml5.DEFAULT_NAME)
        registerOutputFormatterOnAllToolchains(toolchains, AsciidoctorjDocbook, DefaultAsciidoctorjDocbook.DEFAULT_NAME)
        registerOutputFormatterOnAllToolchains(toolchains, AsciidoctorjManpage, DefaultAsciidoctorjManpage.DEFAULT_NAME)
    }
}
