/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.jvm.pdf

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.process.ProcessMode
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
@java.lang.SuppressWarnings('NoWildcardImports')
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.GradleVersion
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

/** Asciidoctor task that is specialises in PDF conversion.
 *
 * @since 2.0.0* @author Schalk W. Cronjé
 */
@CacheableTask
@CompileStatic
class AsciidoctorPdfTask extends AbstractAsciidoctorTask {

    private Object fontsDir
    private String theme

    @Inject
    AsciidoctorPdfTask(WorkerExecutor we) {
        super(we)

        configuredOutputOptions.backends = ['pdf']
        copyNoResources()
    }

    /** The directory where custom are fonts to be found.
     *
     * @return Directory or {@code null} is no directory was set.
     */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    File getFontsDir() {
        this.fontsDir != null ? project.file(this.fontsDir) : null
    }

    /** Specify a directory where to load custom fonts from.
     *
     * This will set the {@code pdf-fontsdir} attribute
     *
     * @param f Directory where custom fonts can be found. anything convertible with {@link Project#file}
     *   can be used.
     */
    void setFontsDir(Object f) {
        this.fontsDir = f
    }

    /** Set the theme to be used from the {@code pdfThemes} extension.
     *
     * @param themeName
     */
    void setTheme(final String themeName) {
        this.theme = themeName
    }

    /** The directory where custom theme is to be found.
     *
     * @return Directory or {@code null} if no directory was set.
     * @throw {@link UnknownDomainObjectException} if theme was specified, but not registered.
     */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    @SuppressWarnings('LineLength')
    File getThemesDir() {
        themeDescriptor?.themeDir
    }

    /** The theme to use.
     *
     * @return Theme name or {@code null} if no theme was set.
     */
    @SuppressWarnings('LineLength')
    @Input
    @Optional
    String getThemeName() {
        themeDescriptor?.themeName
    }

    /** Selects a final process mode of PDF processing.
     *
     * If the system is running on Windows with a Gradle version which still has classpath leakage problems
     * it will switch to using {@link #JAVA_EXEC}.
     *
     * @return Process mode to use for execution.
     */
    @Override
    protected ProcessMode getFinalProcessMode() {
        if (GradleVersion.current() <= LAST_GRADLE_WITH_CLASSPATH_LEAKAGE) {
            if (inProcess != AbstractAsciidoctorTask.JAVA_EXEC) {
                logger.warn 'This version of Gradle leaks snakeyaml on to worker classpaths which breaks ' +
                    'PDF processing. Switching to JAVA_EXEC instead.'
            }
            AbstractAsciidoctorTask.JAVA_EXEC
        } else {
            super.finalProcessMode
        }
    }

    /** The default pattern set for secondary sources.
     *
     * @return {@link #getDefaultSourceDocumentPattern} + `*docinfo*`.
     */
    @Override
    protected PatternSet getDefaultSecondarySourceDocumentPattern() {
        PatternSet ps = defaultSourceDocumentPattern
        ps.include '*-theme.y*ml'
        ps
    }

    /** A task may add some default attributes.
     *
     * If the user specifies any of these attributes, then those attributes will not be utilised.
     *
     * The default implementation will add {@code includedir}
     *
     * @param workingSourceDir Directory where source files are located.
     *
     * @return A collection of default attributes.
     */
    @Override
    protected Map<String, Object> getTaskSpecificDefaultAttributes(File workingSourceDir) {
        Map<String, Object> attrs = super.getTaskSpecificDefaultAttributes(workingSourceDir)

        boolean useOldAttributes = pdfVersion.startsWith('1.5.0-alpha')

        File fonts = getFontsDir()
        if (fonts != null) {
            attrs['pdf-fontsdir'] = fonts.absolutePath
        }

        File styles = themesDir
        if (styles != null) {
            attrs[useOldAttributes ? 'pdf-stylesdir' : 'pdf-themesdir'] = styles.absolutePath
        }

        String selectedTheme = themeName
        if (selectedTheme != null) {
            attrs[useOldAttributes ? 'pdf-style' : 'pdf-theme'] = selectedTheme
        }

        attrs
    }

    @CompileDynamic
    private AsciidoctorPdfThemesExtension.PdfThemeDescriptor getThemeDescriptor() {
        if (this.theme) {
            AsciidoctorPdfThemesExtension pdfThemes = project.extensions.getByType(AsciidoctorPdfThemesExtension)
            (AsciidoctorPdfThemesExtension.PdfThemeDescriptor) (pdfThemes.getByName(this.theme))
        } else {
            null
        }
    }

    private String getPdfVersion() {
        asciidoctorj.modules.pdf.version ?: project.extensions.getByType(AsciidoctorJExtension).modules.pdf.version
    }
}
