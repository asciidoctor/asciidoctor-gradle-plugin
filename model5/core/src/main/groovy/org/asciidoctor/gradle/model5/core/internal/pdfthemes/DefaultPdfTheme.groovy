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
package org.asciidoctor.gradle.model5.core.internal.pdfthemes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.extensions.AsciidoctorThemeExtension
import org.asciidoctor.gradle.model5.core.pdfthemes.BuiltInThemes
import org.asciidoctor.gradle.model5.core.pdfthemes.PdfTheme
import org.asciidoctor.gradle.model5.core.pdfthemes.PdfThemeCollection
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.StringTools

import javax.inject.Inject

/**
 * Implementation of {@link PdfTheme}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultPdfTheme implements PdfTheme {
    final String name
    private final StringTools stringTools
    private final Property<String> themeName
    private final DirectoryProperty themeDir
    private final AsciidoctorThemeExtension parent

    @Inject
    DefaultPdfTheme(String name, AsciidoctorThemeExtension parent, Project project) {
        this.name = name
        this.parent = parent
        this.stringTools = ConfigCacheSafeOperations.from(project).stringTools()
        this.themeName = project.objects.property(String).convention(name)
        this.themeDir = project.objects.directoryProperty()
    }

    /**
     * Extract the theme from the theme collection.
     *
     * @param collection The theme collection.
     */
    @Override
    void fromCollection(Provider<PdfThemeCollection> collection) {
        this.themeDir.set(collection.flatMap { it.themeDir })
    }

    /**
     * Extract the theme from the named theme collection
     *
     * @param collectionName The name of the theme collection.
     */
    @Override
    void fromCollection(String collectionName) {
        fromCollection(parent.pdfThemeCollections.named(collectionName))
    }

    /**
     * Provider to where the theme is located.
     *
     * @return Provider to a {@link Directory}. If the provider is empty it indicates a built-in theme.
     */
    @Override
    Provider<Directory> getThemeDir() {
        this.themeDir
    }

    /**
     * The theme name.
     *
     * <p>
     *     By default, this is the name under which the theme was registered, but the location may contain more than
     *     one theme, and then the name can be set.
     * </p>
     *
     * @return Theme name.
     */
    @Override
    Provider<String> getThemeName() {
        this.themeName
    }

    /**
     * Override the name of the theme as described by {@link #getThemeName()}.
     *
     * @param theName Anything convertible to a string.
     */
    @Override
    void setThemeName(Object theName) {
        stringTools.updateStringProperty(this.themeName, theName)
    }

    static class Factory implements NamedDomainObjectFactory<PdfTheme> {
        private final ObjectFactory objectFactory
        private final AsciidoctorThemeExtension parent

        @Inject
        Factory(AsciidoctorThemeExtension parent, ObjectFactory objectFactory) {
            this.objectFactory = objectFactory
            this.parent = parent
        }

        /**
         * Creates a new object with the given name.
         *
         * @param name The name
         * @return The object.
         */
        @Override
        PdfTheme create(String name) {
            if (name in BuiltInThemes.values()*.themeName) {
                objectFactory.newInstance(DefaultBuiltInPdfTheme, name)
            } else {
                objectFactory.newInstance(DefaultPdfTheme, name, parent)
            }

        }
    }
}
