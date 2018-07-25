/*
 * Copyright 2013-2018 the original author or authors.
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
import org.asciidoctor.gradle.internal.AsciidoctorUtils
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.GradleVersion
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.StringUtils

import javax.inject.Inject

/** Asciidoctor task that is specialises in PDF conversion.
 *
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorPdfTask extends AbstractAsciidoctorTask {

    private Object fontsDir
    private Object stylesDir
    private Object styleName

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

    /** The directory where custom theme is to be found.
     *
     * @return Directory or {@code null} is no directory was set.
     */
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    File getStylesDir() {
        this.stylesDir != null ? project.file(this.stylesDir) : null
    }

    /** Specify a directory where to load custom theme from.
     *
     * This will set the {@code pdf-stylesdir} attribute
     *
     * @param f Directory where custom syles can be found. anything convertible with {@link Project#file}
     *   can be used.
     */
    void setStylesDir(Object f) {
        this.stylesDir = f
    }

    /** The theme to use.
     *
     * @return Theme name or {@code null} if no theme was set.
     */
    @Input
    @Optional
    String getStyleName() {
        this.styleName != null ? StringUtils.stringize(this.styleName) : null
    }

    /** The name of the YAML theme file to load.
     *
     * If the name ends with {@code .yml}, it’s assumed to be the complete name of a file.
     * Otherwise, {@code -theme.yml} is appended to the name to make the file name (i.e., {@code <name>-theme.yml}).
     *
     * @param s Name of style (theme).
     */
    void setStyleName(Object s) {
        this.styleName = s
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
        if(GradleVersion.current() <= LAST_GRADLE_WITH_CLASSPATH_LEAKAGE && AsciidoctorUtils.OS.windows) {
            if(inProcess != JAVA_EXEC) {
                logger.warn 'PDF processing on this version of Gradle combined with running on Microsoft Windows will fail due to classpath issues. Switching to JAVA_EXEC instead.'
            }
            JAVA_EXEC
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

        File fonts = getFontsDir()
        if(fonts != null) {
            attrs['pdf-fontsdir'] = fonts.absolutePath
        }

        File styles = getStylesDir()
        if(styles != null) {
            attrs['pdf-stylesdir'] = styles.absolutePath
        }

        String theme = getStyleName()
        if(theme != null) {
            attrs['pdf-style'] = theme
        }

        attrs
    }
}
