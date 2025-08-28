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
package org.asciidoctor.gradle.model5.jvm.toolchains

import org.asciidoctor.gradle.model5.core.AsciidoctorCoreExtension
import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjDocbook
import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.internal.toolchains.DefaultAsciidoctorjToolchain
import org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjBasePlugin
import org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

/**
 * @author Schalk W. Cronjé
 */
class AsciidoctorjToolchainSpec extends UnitTestSpecification {

    AsciidoctorCoreExtension asciidoc

    void setup() {
        project.pluginManager.apply(AsciidoctorjBasePlugin)
        asciidoc = project.extensions.getByType(AsciidoctorCoreExtension)
    }

    void 'When the base plugin is applied, an asciidoctorj toolchain can be registered'() {
        when:
        final tc = asciidoc.toolchains.create('default', AsciidoctorjToolchain)

        then:
        tc instanceof DefaultAsciidoctorjToolchain
    }

    void 'When the standard plugin is applied, there is a default toolchain with two output formatters'() {
        when:
        project.pluginManager.apply(AsciidoctorjPlugin)
        final tc = asciidoc.toolchains.getByName(AsciidoctorjPlugin.DEFAULT_TOOLCHAIN)

        then:
        tc instanceof DefaultAsciidoctorjToolchain

        when:
        final formatters = tc.registeredOutputFormatters

        then:
        formatters.getByName('html') instanceof DefaultAsciidoctorjHtml5
        formatters.getByName('docbook') instanceof DefaultAsciidoctorjDocbook
    }
}