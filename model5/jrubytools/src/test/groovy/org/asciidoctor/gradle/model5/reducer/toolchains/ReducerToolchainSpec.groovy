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
package org.asciidoctor.gradle.model5.reducer.toolchains

import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.reducer.internal.toolchains.DefaultReducerToolchain
import org.asciidoctor.gradle.model5.reducer.plugins.ReducerBasePlugin
import org.asciidoctor.gradle.model5.reducer.plugins.ReducerPlugin
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

class ReducerToolchainSpec extends UnitTestSpecification {

    AsciidoctorModelExtension asciidoc

    void setup() {
        project.pluginManager.apply(ReducerBasePlugin)
        asciidoc = project.extensions.getByType(AsciidoctorModelExtension)
    }

    void 'When the base plugin is applied, a Reducer toolchain can be registered'() {
        when:
        final tc = asciidoc.toolchains.create('default', AsciidoctorReducerToolchain)

        then:
        tc instanceof DefaultReducerToolchain
    }

    void 'When the conventions plugin is applied a prepare task is created'() {
        setup:
        project.pluginManager.apply(ReducerPlugin)

        when:
        final tc = asciidoc.toolchains.create('default', AsciidoctorReducerToolchain)
        final taskNames = project.tasks.names

        then:
        taskNames.contains('asciidoctorPrepareAsciidoctorReducerToolchain')
    }
}