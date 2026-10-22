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
package org.asciidoctor.gradle.model5.kramdoc.toolchains

import org.asciidoctor.gradle.model5.kramdoc.KramdocModelExtension
import org.asciidoctor.gradle.model5.kramdoc.plugins.KramdocBasePlugin
import org.asciidoctor.gradle.model5.kramdoc.plugins.KramdocPlugin
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

class KramdocToolchainSpec extends UnitTestSpecification {

    KramdocModelExtension kramdocAsciidoc

    void setup() {
        project.pluginManager.apply(KramdocBasePlugin)
        kramdocAsciidoc = project.extensions.getByType(KramdocModelExtension)
    }

    void 'When the base plugin is applied, a Kramdoc toolchain can be registered'() {
        when:
        final tc = kramdocAsciidoc.toolchains.create('default')

        then:
        tc instanceof KramdocToolchain
    }

    void 'When the conventions plugin is applied a prepare task is created'() {
        setup:
        project.pluginManager.apply(KramdocPlugin)

        when:
        final taskNames = project.tasks.names

        then:
        taskNames.contains('asciidoctorPrepareKramdownToolchain')
    }
}