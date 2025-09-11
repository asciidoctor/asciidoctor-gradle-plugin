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
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreBasePlugin
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjGenericExtension
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjDocbook
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjGenericOutputFormatter
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjManpage
import org.asciidoctor.gradle.model5.jvm.internal.extensions.DefaultAsciidoctorjGenericExtension
import org.asciidoctor.gradle.model5.jvm.internal.formatters.AsciidoctorjDocbookFactory
import org.asciidoctor.gradle.model5.jvm.internal.formatters.AsciidoctorjGenericOutputFormatterFactory
import org.asciidoctor.gradle.model5.jvm.internal.formatters.AsciidoctorjHtml5Factory
import org.asciidoctor.gradle.model5.jvm.internal.formatters.AsciidoctorjManpageFactory
import org.asciidoctor.gradle.model5.jvm.internal.toolchains.AsciidoctorjToolchainFactory
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.model5.jvm.JvmModel.registerExtensionFactory
import static org.asciidoctor.gradle.model5.jvm.JvmModel.registerOutputFormatterFactory

/**
 * The {@code asciidoctorj} base plugin applies the core plugin, registers factories for {@link AsciidoctorjToolchain},
 * {@link AsciidoctorjHtml5} and {@link ???}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjBasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AsciidoctorCoreBasePlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)

        asciidoc.toolchains.registerFactory(
            AsciidoctorjToolchain,
            project.objects.newInstance(AsciidoctorjToolchainFactory)
        )

        registerOutputFormatterFactory(
            asciidoc.toolchains,
            AsciidoctorjHtml5,
            AsciidoctorjHtml5Factory,
            project.objects
        )

        registerOutputFormatterFactory(
            asciidoc.toolchains,
            AsciidoctorjDocbook,
            AsciidoctorjDocbookFactory,
            project.objects
        )

        registerOutputFormatterFactory(
            asciidoc.toolchains,
            AsciidoctorjManpage,
            AsciidoctorjManpageFactory,
            project.objects
        )

        registerOutputFormatterFactory(
            asciidoc.toolchains,
            AsciidoctorjGenericOutputFormatter,
            AsciidoctorjGenericOutputFormatterFactory,
            project.objects
        )

        registerExtensionFactory(
            asciidoc.toolchains,
            AsciidoctorjGenericExtension,
            DefaultAsciidoctorjGenericExtension.Factory,
            project.objects
        )
    }
}
