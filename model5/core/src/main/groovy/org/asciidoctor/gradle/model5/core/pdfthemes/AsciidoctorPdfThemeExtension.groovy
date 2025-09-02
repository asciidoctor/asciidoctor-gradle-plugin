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
package org.asciidoctor.gradle.model5.core.pdfthemes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.pdfthemes.DefaultGithubThemeCollection
import org.asciidoctor.gradle.model5.core.internal.pdfthemes.DefaultGitlabThemeCollection
import org.asciidoctor.gradle.model5.core.internal.pdfthemes.DefaultLocalThemeCollection
import org.asciidoctor.gradle.model5.core.internal.pdfthemes.DefaultPdfTheme
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectCollection
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
class AsciidoctorPdfThemeExtension {
    public static final NAME = 'asciidocPdfThemes'

    final ExtensiblePolymorphicDomainObjectContainer<PdfThemeCollection> themeCollections
    final NamedDomainObjectCollection<PdfTheme> themes

    AsciidoctorPdfThemeExtension(Project project) {
        this.themeCollections = project.objects.polymorphicDomainObjectContainer(PdfThemeCollection).tap {
            registerFactory(LocalThemeCollection, project.objects.newInstance(DefaultLocalThemeCollection.Factory))
            registerFactory(GithubThemeCollection, project.objects.newInstance(DefaultGithubThemeCollection.Factory))
            registerFactory(GitlabThemeCollection, project.objects.newInstance(DefaultGitlabThemeCollection.Factory))
        }

        final ObjectFactory objectFactory = project.objects
        this.themes = project.objects.domainObjectContainer(
                PdfTheme,
                objectFactory.newInstance(DefaultPdfTheme.Factory, this)
        ).tap {ndoc ->
            BuiltInThemes.values().each {
                ndoc.create(it.themeName)
            }
        }
    }
}
