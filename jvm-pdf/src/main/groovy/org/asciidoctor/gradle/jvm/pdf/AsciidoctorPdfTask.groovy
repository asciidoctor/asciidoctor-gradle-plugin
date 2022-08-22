/*
 * Copyright 2013-2022 the original author or authors.
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
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ReplacedBy
@java.lang.SuppressWarnings('NoWildcardImports')
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.GradleVersion
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

/** Asciidoctor task that is specialises in PDF conversion.
 *
 * @author Schalk W. Cronjé
 * @author Gary Hale
 * @author Halit Anil Dönmez
 *
 * @since 2.0.0* @author Schalk W. Cronjé
 */
@CacheableTask
@CompileStatic
class AsciidoctorPdfTask extends AbstractAsciidoctorTask {

    private String theme
    private final List<Object> fontDirs = []

    @Inject
    AsciidoctorPdfTask(WorkerExecutor we) {
        super(we)

        configuredOutputOptions.backends = ['pdf']
        copyNoResources()
    }

    /** @Deprecated Use{@link #getFontsDirs()} instead
     *
     * @return Pdf font directory as a file
     * @throws {@link PdfFontDirException} if there are either multiple directories or no directory for pdf font
     * */
    @Deprecated
    @ReplacedBy('getFontsDirs')
    File getFontsDir() {
        if (this.fontDirs.size() > 1) {
            throw new PdfFontDirException('There is more than 1 file in the fonts directory')
        }
        if (this.fontDirs.empty) {
            throw new PdfFontDirException('No directory is specified')
        }
        this.project.file(this.fontDirs.first())
    }

    /** @Deprecated Use{@link #setFontsDirs(java.lang.Iterable)} instead and specify the single directory
     *
     * Specify a directory where to load custom fonts from.
     *
     * This will set the {@code pdf-fontsdir} attribute
     *
     * @param f Directory where custom fonts can be found. anything convertible with {@link Project#file}
     *   can be used.
     */
    @SuppressWarnings('UnnecessarySetter')
    @Deprecated
    void setFontsDir(Object f) {
        setFontsDirs([f])
    }

    /** Returns the directories or single directory for the fonts
     *
     * @return Directories for the pdf fonts
     * */
    @InputFiles
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    FileCollection getFontsDirs() {
        this.project.files(this.fontDirs)
    }

    /** Specify a directory or directories where to load custom fonts from.
     *
     * This will set the {@code pdf-fontsdir} attribute
     *
     * @param f Directory where custom fonts can be found. anything convertible with {@link Project#file}
     *   can be used.
     */
    void setFontsDirs(Iterable<Object> paths) {
        this.fontDirs.clear()
        this.fontDirs.addAll(paths)
    }

    /** Add files paths for the custom fonts
     *
     * @param f List of directories fonts can be found. anything convertible with {@link Project#file} can be used
     * */
    @SuppressWarnings('UnnecessarySetter')
    void fontsDirs(Object... f) {
        this.fontDirs.addAll(f.toList())
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
    File getThemesDir() {
        themeDescriptor?.themeDir
    }

    /** The theme to use.
     *
     * @return Theme name or {@code null} if no theme was set.
     */
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
    @SuppressWarnings('UnnecessaryGetter')
    @Override
    protected Map<String, Object> getTaskSpecificDefaultAttributes(File workingSourceDir) {
        Map<String, Object> attrs = super.getTaskSpecificDefaultAttributes(workingSourceDir)

        boolean useOldAttributes = pdfVersion.startsWith('1.5.0-alpha')

        FileCollection fonts = getFontsDirs()
        if (!fonts?.empty) {
            attrs['pdf-fontsdir'] = fonts.asPath
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
