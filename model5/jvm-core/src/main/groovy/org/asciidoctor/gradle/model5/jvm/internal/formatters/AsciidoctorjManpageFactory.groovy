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
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjManpage
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

/**
 * Manpage backend factory.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjManpageFactory implements NamedDomainObjectFactory<AsciidoctorjManpage> {

    private final ObjectFactory objectFactory
    private final AsciidoctorjToolchain toolchain

    @Inject
    AsciidoctorjManpageFactory(AsciidoctorjToolchain toolchain, Project project) {
        this.objectFactory = project.objects
        this.toolchain = toolchain
    }

    @Override
    AsciidoctorjManpage create(String name) {
        objectFactory.newInstance(DefaultAsciidoctorjManpage, name, toolchain)
    }
}
