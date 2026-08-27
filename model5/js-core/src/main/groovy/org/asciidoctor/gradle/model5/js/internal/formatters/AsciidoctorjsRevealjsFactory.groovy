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
// tag::hacking-asciidoctorjs-output-formatter[]
package org.asciidoctor.gradle.model5.js.internal.formatters

import groovy.transform.CompileStatic

// end::hacking-asciidoctorjs-output-formatter[]

import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsRevealjs
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Reveal.js factory implementation.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
// tag::hacking-asciidoctorjs-output-formatter[]
@CompileStatic
class AsciidoctorjsRevealjsFactory extends AbstractFactory
    implements NamedDomainObjectFactory<AsciidoctorjsRevealjs> { // <.>

    @Inject
    AsciidoctorjsRevealjsFactory(AsciidoctorjsToolchain toolchain, Project project) {
        super(toolchain, project)
    }

    @Override
    AsciidoctorjsRevealjs create(String name) { // <.>
        objectFactory.newInstance(DefaultAsciidoctorjsRevealjs, name, toolchain) // <.>
    }
}
// end::hacking-asciidoctorjs-output-formatter[]
