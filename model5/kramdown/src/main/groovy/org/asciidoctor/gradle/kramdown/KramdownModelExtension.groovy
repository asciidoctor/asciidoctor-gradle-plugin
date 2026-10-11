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
package org.asciidoctor.gradle.kramdown

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.kramdown.publications.KramdownPublication
import org.asciidoctor.gradle.kramdown.publications.PublicationFactory
import org.asciidoctor.gradle.kramdown.toolchains.KramdownToolchain
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * The extension for the this plugin suite. It described the model.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class KramdownModelExtension {
    public static final String NAME = 'kramdownAsciidoc'

    final NamedDomainObjectContainer<KramdownPublication> publications
    final NamedDomainObjectContainer<KramdownToolchain> toolchains

    @SuppressWarnings(['ThisReferenceEscapesConstructor'])
    KramdownModelExtension(Project project) {
        final publicationFactory = new PublicationFactory(project, this)
        this.publications = project.objects.domainObjectContainer(
            KramdownPublication, publicationFactory
        )

        this.toolchains = project.objects.polymorphicDomainObjectContainer(KramdownToolchain)
    }
}
