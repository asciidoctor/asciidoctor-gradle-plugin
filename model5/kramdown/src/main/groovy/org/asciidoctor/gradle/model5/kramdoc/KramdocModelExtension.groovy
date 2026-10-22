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
package org.asciidoctor.gradle.model5.kramdoc

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.kramdoc.internal.toolchains.DefaultKramdocToolchain
import org.asciidoctor.gradle.model5.kramdoc.publications.KramdocPublication
import org.asciidoctor.gradle.model5.kramdoc.publications.PublicationFactory
import org.asciidoctor.gradle.model5.kramdoc.toolchains.KramdocToolchain
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
class KramdocModelExtension {
    public static final String NAME = 'kramdownAsciidoc'

    final NamedDomainObjectContainer<KramdocPublication> publications
    final NamedDomainObjectContainer<KramdocToolchain> toolchains

    @SuppressWarnings(['ThisReferenceEscapesConstructor'])
    KramdocModelExtension(Project project) {
        final publicationFactory = new PublicationFactory(project, this)
        this.publications = project.objects.domainObjectContainer(
            KramdocPublication, publicationFactory
        )

        this.toolchains = project.objects.domainObjectContainer(
            KramdocToolchain,
            project.objects.newInstance(DefaultKramdocToolchain.Factory)
        )
    }
}
