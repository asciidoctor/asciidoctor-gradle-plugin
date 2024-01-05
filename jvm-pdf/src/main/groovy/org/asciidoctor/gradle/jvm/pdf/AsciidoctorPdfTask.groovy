/*
 * Copyright 2013-2024 the original author or authors.
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

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.file.FileCollection
@java.lang.SuppressWarnings('NoWildcardImports')
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

import static org.ysb33r.grolifant.api.core.TaskInputFileOptions.IGNORE_EMPTY_DIRECTORIES
import static org.ysb33r.grolifant.api.core.TaskInputFileOptions.OPTIONAL

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
    private final List<Object> pdfFontDirs = []

    @Inject
    AsciidoctorPdfTask(WorkerExecutor we) {
        super(we)

        outputOptions.backends = ['pdf']
        copyNoResources()
        projectOperations.tasks.inputFiles(
                inputs,
                { -> pdfFontDirs },
                PathSensitivity.RELATIVE,
                IGNORE_EMPTY_DIRECTORIES, OPTIONAL
        )
    }

    /** Returns the directories or single directory for the fonts
     *
     * @return Directories for the pdf fonts
     * */
    @Internal
    FileCollection getFontsDirs() {
        projectOperations.fsOperations.files(this.pdfFontDirs)
    }

    /** Specify a directory or directories where to load custom fonts from.
     *
     * This will set the {@code pdf-fontsdir} attribute
     *
     * @param f Directory where custom fonts can be found. anything convertible with {@link Project#file}
     *   can be used.
     */
    void setFontsDirs(Iterable<Object> paths) {
        this.pdfFontDirs.clear()
        this.pdfFontDirs.addAll(paths)
    }

    /** Add files paths for the custom fonts
     *
     * @param f List of directories fonts can be found. anything convertible with {@link Project#file} can be used
     * */
    @SuppressWarnings('UnnecessarySetter')
    void fontsDirs(Object... f) {
        this.pdfFontDirs.addAll(f.toList())
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

//    /** Selects a final process mode of PDF processing.
//     *
//     * If the system is running on Windows with a Gradle version which still has classpath leakage problems
//     * it will switch to using {@link #JAVA_EXEC}.
//     *
//     * @return Process mode to use for execution.
//     */
//    @Override
//    protected ProcessMode getFinalProcessMode() {
//        if (GradleVersion.current() <= LAST_GRADLE_WITH_CLASSPATH_LEAKAGE) {
//            if (inProcess != AbstractAsciidoctorTask.JAVA_EXEC) {
//                logger.warn 'This version of Gradle leaks snakeyaml on to worker classpaths which breaks ' +
//                        'PDF processing. Switching to JAVA_EXEC instead.'
//            }
//            AbstractAsciidoctorTask.JAVA_EXEC
//        } else {
//            super.finalProcessMode
//        }
//    }

    /**
     * The default pattern set for secondary sources.
     *
     * @return {@link #getDefaultSourceDocumentPattern} + `*docinfo*`.
     */
    @Override
    PatternSet getDefaultSecondarySourceDocumentPattern() {
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
    Map<String, ?> getTaskSpecificDefaultAttributes(File workingSourceDir) {
        final attrs = super.getTaskSpecificDefaultAttributes(workingSourceDir)
        final fonts = fontsDirs
        attrs.putAll([
                'pdf-fontsdir' : fonts.empty ? null : fonts.asPath,
                'pdf-themesdir': themesDir?.absolutePath,
                'pdf-theme'    : themeName
        ].findAll { k, v -> v != null })
        attrs
    }

//    @CompileDynamic
    private AsciidoctorPdfThemesExtension.PdfThemeDescriptor getThemeDescriptor() {
        if (this.theme) {
            AsciidoctorPdfThemesExtension pdfThemes = project.extensions.getByType(AsciidoctorPdfThemesExtension)
            (AsciidoctorPdfThemesExtension.PdfThemeDescriptor) (pdfThemes.getByName(this.theme))
        } else {
            null
        }
    }
}
