/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.jvm

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.asciidoctor.gradle.base.AbstractDownloadableComponent
import org.gradle.api.Project
import org.ysb33r.grolifant.api.StringUtils

import static org.asciidoctor.gradle.base.internal.DeprecatedFeatures.addDeprecationMessage

/** Easy way to configure themes for Asciidoctor PDF either as local themes or
 * as downloadable.
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorPdfThemesExtension extends AbstractDownloadableComponent<PdfLocalTheme, PdfThemeDescriptor> {

    final static String NAME = 'pdfThemes'

    /** Describes tha name and location of a PDF theme.
     *
     */
    @SuppressWarnings('ClassName')
    static class PdfThemeDescriptor {
        final File themeDir
        final String themeName

        @Deprecated
        File getStyleDir() {
            migrationMessage(
                'getStyleDir() is deprecated. Use getThemeDir() instead()'
            )
            themeDir
        }

        @Deprecated
        String getStyleName() {
            migrationMessage(
                'getThemeName() is deprecated. Use getThemeName() instead()'
            )
            themeName
        }

        PdfThemeDescriptor(final String name, final File dir) {
            this.themeDir = dir
            this.themeName = name
        }

        @PackageScope
        Project project

        private void migrationMessage(final String msg) {
            if (project) {
                addDeprecationMessage(
                    project,
                    'PdfThemeDescriptor',
                    msg
                )
            }
        }
    }

    /** Defines a local theme.
     *
     */
    @SuppressWarnings('ClassName')
    static class PdfLocalTheme {

        /** Directory where local theme is to be found.
         *
         */
        Object themeDir

        /** Name of theme.
         *
         */
        Object themeName

        @Deprecated
        Object getStyleDir() {
            migrationMessage(
                'getStyleDir() is deprecated. Use getThemeDir() instead()'
            )
            themeDir
        }

        @Deprecated
        Object getStyleName() {
            migrationMessage(
                'getThemeName() is deprecated. Use getThemeName() instead()'
            )
            themeName
        }

        @Deprecated
        void setStyleDir(Object o) {
            migrationMessage(
                'setStyleDir() is deprecated. Use setThemeDir() instead()'
            )
            themeDir = o
        }

        @Deprecated
        void setStyleName(Object o) {
            migrationMessage(
                'setStyleName() is deprecated. Use setThemeName() instead()'
            )
            themeName = o
        }

        @PackageScope
        Project project

        private void migrationMessage(final String msg) {
            if (project) {
                addDeprecationMessage(
                    project,
                    'PdfLocalTheme',
                    msg
                )
            }
        }
    }

    /** Extension for configuring PDF themes.
     *
     * @param project Project this extension has been associated with.
     */
    AsciidoctorPdfThemesExtension(final Project project) {
        super(project)
    }

    /** Instantiates a component of type {@code C}.
     *
     * @param name Name of componenet
     * @return New component source description
     */
    @Override
    protected PdfLocalTheme instantiateComponentSource(String name) {
        PdfLocalTheme theme = new PdfLocalTheme()
        theme.project = project
        theme.themeName = name
        theme
    }

    /** Create a closure that will convert an instance of a {@code C} into a {@code S}.
     *
     * @param theme Component to be converted.
     * @return Converting closure.
     */
    @Override
    protected Closure convertible(PdfLocalTheme theme) {
        return { ->
            new PdfThemeDescriptor(StringUtils.stringize(theme.themeName), project.file(theme.themeDir))
        }
    }

    @Override
    protected PdfThemeDescriptor instantiateResolvedComponent(String name, File path) {
        PdfThemeDescriptor descriptor = new PdfThemeDescriptor(name, path)
        descriptor.project = project
        descriptor
    }
}
