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
package org.asciidoctor.gradle.model5.core.internal.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.errors.InvalidPublicationName
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorPublication
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

/**
 * Creates publications.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class PublicationFactory implements NamedDomainObjectFactory<AsciidoctorPublication> {

    private final ObjectFactory objectFactory
    private final AsciidoctorModelExtension parent
    private static final List<String> INVALID_NAMES = [
            'all',
            'toolchains'
    ].asImmutable()

    PublicationFactory(Project project, AsciidoctorModelExtension parent) {
        this.objectFactory = project.objects
        this.parent = parent
    }

    @Override
    AsciidoctorPublication create(String name) {
        if(name.toLowerCase(Locale.US) in INVALID_NAMES) {
            throw new InvalidPublicationName("'${name}' cannot be used as a publication name")
        }
        objectFactory.newInstance(AsciidoctorPublication, name, parent)
    }

}
