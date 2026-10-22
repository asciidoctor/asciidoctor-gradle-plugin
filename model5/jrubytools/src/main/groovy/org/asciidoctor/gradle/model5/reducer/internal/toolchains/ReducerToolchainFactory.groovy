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
package org.asciidoctor.gradle.model5.reducer.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.reducer.toolchains.AsciidoctorReducerToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

/**
 * Factory for {@link AsciidoctorReducerToolchain}.
 *
 * @since 5.1
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class ReducerToolchainFactory implements NamedDomainObjectFactory<AsciidoctorReducerToolchain> {

    private final ObjectFactory objectFactory

    @Inject
    ReducerToolchainFactory(Project project) {
        this.objectFactory = project.objects
    }

    @Override
    AsciidoctorReducerToolchain create(String name) {
        objectFactory.newInstance(DefaultReducerToolchain, name)
    }
}
