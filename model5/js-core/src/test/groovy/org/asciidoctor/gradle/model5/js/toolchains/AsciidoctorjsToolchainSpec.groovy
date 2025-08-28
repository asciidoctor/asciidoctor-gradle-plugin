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
package org.asciidoctor.gradle.model5.js.toolchains

import org.asciidoctor.gradle.model5.core.AsciidoctorCoreExtension
import org.asciidoctor.gradle.model5.js.internal.toolchains.DefaultAsciidoctorjsToolchain
import org.asciidoctor.gradle.model5.js.plugins.AsciidoctorjsBasePlugin
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

/**
 * @author Schalk W. Cronjé
 */
class AsciidoctorjsToolchainSpec extends UnitTestSpecification {

    AsciidoctorCoreExtension asciidoc

    void setup() {
        project.pluginManager.apply(AsciidoctorjsBasePlugin)
        asciidoc = project.extensions.getByType(AsciidoctorCoreExtension)
    }

    void 'When the base plugin is applied, an asciidoctorj toolchain can be registered'() {
        when:
        final tc = asciidoc.toolchains.create('default', AsciidoctorjsToolchain)

        then:
        tc instanceof DefaultAsciidoctorjsToolchain
    }
}