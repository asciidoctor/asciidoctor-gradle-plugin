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
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin
import org.asciidoctor.gradle.model5.reducer.ReducerModel
import org.asciidoctor.gradle.model5.reducer.formatters.AsciidoctorReducerOutputFormatter
import org.asciidoctor.gradle.model5.reducer.internal.formatters.DefaultReducerAsciidocOutputFormatter
import org.asciidoctor.gradle.model5.reducer.toolchains.AsciidoctorReducerToolchain
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The {@code Reducer} plugin applies the base plugin, then creates a toolchain called {@code asciidoctorReducer}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class ReducerPlugin implements Plugin<Project> {
    public static final String DEFAULT_TOOLCHAIN = 'asciidoctorReducer'
    public static final String PLUGIN_ID = 'org.asciidoctor.reducer'

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(ReducerBasePlugin)
            apply(AsciidoctorCorePlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorModelExtension)
        final toolchains = asciidoc.toolchains

        toolchains.create(DEFAULT_TOOLCHAIN, AsciidoctorReducerToolchain) {
        }

        ReducerModel.registerOutputFormatterOnAllToolchains(
                toolchains,
                AsciidoctorReducerOutputFormatter,
                DefaultReducerAsciidocOutputFormatter.DEFAULT_NAME
        )
    }
}
