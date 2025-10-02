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
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.extensions.AsciidoctorThemeExtension
import org.asciidoctor.gradle.model5.core.internal.formatters.DefaultAllowUriRead
import org.asciidoctor.gradle.model5.core.pdfthemes.BuiltInThemes
import org.asciidoctor.gradle.model5.core.pdfthemes.PdfTheme
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjPdf
import org.asciidoctor.gradle.model5.jvm.internal.PluginUtils
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskInputs
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.FileSystemOperations

import javax.inject.Inject

/**
 * Implementation of {@code asciidoctorj-pdf} output formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjPdf extends AbstractAsciidoctorjFormatterVersioned implements AsciidoctorjPdf {
    public static final String DEFAULT_NAME = 'pdf'
    public static final String BACKEND_NAME = DEFAULT_NAME
    private static final String ATTR_THEME = 'pdf-theme'
    private static final String ATTR_THEME_DIR = 'pdf-themedir'
    private static final String ATTR_FONT_DIR = 'pdf-fontsdir'

    final boolean copyResources = false
    private final NamedDomainObjectCollection<PdfTheme> availableThemes
    private final FileSystemOperations fsOperations
    private final Property<PdfTheme> theme
    private final Property<File> fontsDir

    @Delegate(includes = ['setAllowUriRead'])
    private final DefaultAllowUriRead allowUriRead

    @Inject
    DefaultAsciidoctorjPdf(String name, AsciidoctorjToolchain tc, Project project) {
        super(
            name,
            BACKEND_NAME,
            JvmModel.ASCIIDOCTORJ_PDF_DEPENDENCY,
            PluginUtils.loadDefaultVersion('asciidoctorj.pdf', project, tc.class.classLoader),
            tc,
            project
        )

        this.fsOperations = ConfigCacheSafeOperations.from(project).fsOperations()
        this.theme = project.objects.property(PdfTheme)
        this.allowUriRead = project.objects.newInstance(DefaultAllowUriRead)
        this.fontsDir = project.objects.property(File)

        availableThemes = project.extensions.getByType(AsciidoctorThemeExtension).pdfThemes
        useTheme(BuiltInThemes.DEFAULT.themeName)

        final themeAttrs = this.theme.flatMap { pt ->
            if (pt.themeDir.present) {
                pt.themeName.zip(pt.themeDir) { n, d ->
                    [(ATTR_THEME): n, (ATTR_THEME_DIR): d.asFile.absolutePath]
                }
            } else {
                pt.themeName.map { [(ATTR_THEME): it] }
            }
        }

        attributes.putAll(
            themeAttrs.zip(this.fontsDir) { attrs, dir ->
                attrs + [(ATTR_FONT_DIR): dir.absolutePath]
            }.orElse(themeAttrs)
        )
        attributes.putAll(allowUriRead.attributeProvider)
    }

    /**
     * Use the named theme from {@code asciidocPdfThemes}.
     *
     * @param themeName Name of theme. Must already have been registered.
     */
    @Override
    void useTheme(String themeName) {
        this.theme.set(availableThemes.named(themeName))
    }

    /**
     * Supply an alternative location for fonts.
     *
     * @param dir Anything convertible to a file.
     */
    @Override
    void setFontsDir(Object dir) {
        fsOperations.updateFileProperty(this.fontsDir, dir)
    }

    @Classpath
    @Override
    void configureTaskInputs(TaskInputs taskInputs) {
        taskInputs.dir(fontsDir).optional(true).withPathSensitivity(PathSensitivity.RELATIVE)
        taskInputs.dir(theme.flatMap { it.themeDir }).optional(true)
            .withPathSensitivity(PathSensitivity.RELATIVE)
    }

    @Override
    protected final Class<?> getDslType() {
        AsciidoctorjPdf
    }
}
