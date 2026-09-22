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
package org.asciidoctor.gradle.model5.core.internal.pdfthemes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.errors.BuiltInThemeException
import org.asciidoctor.gradle.model5.core.pdfthemes.PdfTheme
import org.asciidoctor.gradle.model5.core.pdfthemes.PdfThemeCollection
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

import javax.inject.Inject

import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Implementation of {@link PdfTheme}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultBuiltInPdfTheme implements PdfTheme {
    final String name
    private final Provider<String> themeName
    private final Provider<Directory> themeDir

    @Inject
    DefaultBuiltInPdfTheme(String name, Project project) {
        this.name = name
        this.themeName = project.provider { -> name }
        this.themeDir = project.provider { -> (Directory) null }
    }

    /**
     * Extract the theme from the theme collection.
     *
     * @param collection The theme collection.
     */
    @Override
    void fromCollection(Provider<PdfThemeCollection> collection) {
        fromCollection(EMPTY)
    }

    /**
     * Extract the theme from the named theme collection
     *
     * @param collectionName The name of the theme collection.
     */
    @Override
    void fromCollection(String collectionName) {
        throw new BuiltInThemeException('Cannot the change theme collection of a built-in')
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
        throw new BuiltInThemeException('Cannot change the name of a built-in theme')
    }
}
