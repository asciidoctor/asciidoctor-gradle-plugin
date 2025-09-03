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
package org.asciidoctor.gradle.model5.core.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.pdfthemes.DefaultGithubThemeCollection
import org.asciidoctor.gradle.model5.core.internal.pdfthemes.DefaultGitlabThemeCollection
import org.asciidoctor.gradle.model5.core.internal.pdfthemes.DefaultLocalThemeCollection
import org.asciidoctor.gradle.model5.core.internal.pdfthemes.DefaultPdfTheme
import org.asciidoctor.gradle.model5.core.pdfthemes.BuiltInThemes
import org.asciidoctor.gradle.model5.core.pdfthemes.GithubThemeCollection
import org.asciidoctor.gradle.model5.core.pdfthemes.GitlabThemeCollection
import org.asciidoctor.gradle.model5.core.pdfthemes.LocalThemeCollection
import org.asciidoctor.gradle.model5.core.pdfthemes.PdfTheme
import org.asciidoctor.gradle.model5.core.pdfthemes.PdfThemeCollection
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

/**
 * Manages PDF themes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorThemeExtension {
    public static final NAME = 'asciidocThemes'

    final ExtensiblePolymorphicDomainObjectContainer<PdfThemeCollection> pdfThemeCollections
    final ExtensiblePolymorphicDomainObjectContainer<PdfThemeCollection> revealjsThemeCollections
    final NamedDomainObjectContainer<PdfTheme> pdfThemes
    final NamedDomainObjectContainer<PdfTheme> revealjsThemes

    AsciidoctorThemeExtension(Project project) {
        final ObjectFactory objectFactory = project.objects
        this.pdfThemeCollections = objectFactory.polymorphicDomainObjectContainer(PdfThemeCollection)

        this.pdfThemes = objectFactory.domainObjectContainer(
                PdfTheme,
                objectFactory.newInstance(DefaultPdfTheme.Factory, this)
        )

        registerPdfFactories(objectFactory)
        registerBuiltInPdfThemes()
    }

    private void registerPdfFactories(ObjectFactory objects) {
        pdfThemeCollections.registerFactory(
                LocalThemeCollection,
                objects.newInstance(DefaultLocalThemeCollection.Factory)

        )
        pdfThemeCollections.registerFactory(
                GitlabThemeCollection,
                objects.newInstance(DefaultGitlabThemeCollection.Factory)
        )
        pdfThemeCollections.registerFactory(
                GithubThemeCollection,
                objects.newInstance(DefaultGithubThemeCollection.Factory)
        )
    }

    private void registerBuiltInPdfThemes() {
        BuiltInThemes.values().each { pdfThemes.create(it.themeName) }
    }
}
