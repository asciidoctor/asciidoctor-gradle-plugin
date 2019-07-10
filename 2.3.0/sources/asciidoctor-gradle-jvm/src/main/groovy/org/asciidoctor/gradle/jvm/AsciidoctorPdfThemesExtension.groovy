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
import org.asciidoctor.gradle.base.AbstractDownloadableComponent
import org.gradle.api.Project
import org.ysb33r.grolifant.api.StringUtils

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
        final File styleDir
        final String styleName

        PdfThemeDescriptor(final String name, final File dir) {
            this.styleDir = dir
            this.styleName = name
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
        Object styleDir

        /** Name of theme.
         *
         */
        Object styleName
    }

    /** Extension for configuring PDF themes.
     *
     * @param project Prjoect this extension has been associated with.
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
        theme.styleName = name
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
            new PdfThemeDescriptor(StringUtils.stringize(theme.styleName), project.file(theme.styleDir))
        }
    }

    @Override
    protected PdfThemeDescriptor instantiateResolvedComponent(String name, File path) {
        new PdfThemeDescriptor(name, path)
    }
}
