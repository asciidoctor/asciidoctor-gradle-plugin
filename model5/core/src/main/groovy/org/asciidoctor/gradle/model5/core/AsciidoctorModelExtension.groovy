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
package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationFactory
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorPublication
import org.asciidoctor.gradle.model5.core.toolchains.AsciidoctorToolchain
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * The extension for the this plugin suite. It described the model.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorModelExtension {
    public static final String NAME = 'asciidoc'

    final NamedDomainObjectContainer<AsciidoctorPublication> publications
    final ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains

    @SuppressWarnings(['ThisReferenceEscapesConstructor'])
    AsciidoctorModelExtension(Project project) {
        final publicationFactory = new PublicationFactory(project, this)
        this.publications = project.objects.domainObjectContainer(
                AsciidoctorPublication, publicationFactory
        )

        this.toolchains = project.objects.polymorphicDomainObjectContainer(AsciidoctorToolchain)
    }
}
