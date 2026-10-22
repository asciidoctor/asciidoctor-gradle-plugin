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
package org.asciidoctor.gradle.model5.reducer.plugins

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreBasePlugin
import org.asciidoctor.gradle.model5.jrubytools.plugins.JRubyToolsBasePlugin
import org.asciidoctor.gradle.model5.reducer.ReducerModel
import org.asciidoctor.gradle.model5.reducer.formatters.AsciidoctorReducerOutputFormatter
import org.asciidoctor.gradle.model5.reducer.internal.formatters.DefaultReducerAsciidocOutputFormatter
import org.asciidoctor.gradle.model5.reducer.internal.toolchains.ReducerToolchainFactory
import org.asciidoctor.gradle.model5.reducer.toolchains.AsciidoctorReducerToolchain
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * {@code Reducer} base plugin.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class ReducerBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AsciidoctorCoreBasePlugin)
            apply(JRubyToolsBasePlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)

        asciidoc.toolchains.registerFactory(
                AsciidoctorReducerToolchain,
                project.objects.newInstance(ReducerToolchainFactory)
        )

        ReducerModel.registerOutputFormatterFactory(
                asciidoc.toolchains,
                AsciidoctorReducerOutputFormatter,
                DefaultReducerAsciidocOutputFormatter.Factory,
                project.objects
        )
    }
}
