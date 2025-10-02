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
package org.asciidoctor.gradle.model5.js.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsHtml5
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Factory for {@link AsciidoctorjsHtml5}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjsHtml5Factory extends AbstractFactory implements NamedDomainObjectFactory<AsciidoctorjsHtml5> {

    @Inject
    AsciidoctorjsHtml5Factory(AsciidoctorjsToolchain toolchain, Project project) {
        super(toolchain, project)
    }

    @Override
    AsciidoctorjsHtml5 create(String name) {
        objectFactory.newInstance(DefaultAsciidoctorjsHtml5, name, toolchain)
    }
}
