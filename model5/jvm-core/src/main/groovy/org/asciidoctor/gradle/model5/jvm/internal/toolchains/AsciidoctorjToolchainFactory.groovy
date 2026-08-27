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
package org.asciidoctor.gradle.model5.jvm.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

/**
 * Factory for creating AsciidoctorjToolchain instances.
 * Provides object creation capabilities for the toolchain infrastructure.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjToolchainFactory implements NamedDomainObjectFactory<AsciidoctorjToolchain> {

    private final ObjectFactory objectFactory

    /**
     * Creates a new factory instance.
     *
     * @param project The Gradle project this factory is associated with
     */
    @Inject
    AsciidoctorjToolchainFactory(Project project) {
        this.objectFactory = project.objects
    }

    /**
     * Creates a new AsciidoctorjToolchain instance.
     *
     * @param name The name for the new toolchain instance
     * @return A new toolchain instance
     */
    @Override
    AsciidoctorjToolchain create(String name) {
        objectFactory.newInstance(DefaultAsciidoctorjToolchain, name).tap {
//            registeredOutputFormatters.
        }
    }
}
