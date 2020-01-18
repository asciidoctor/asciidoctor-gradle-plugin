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

import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.grolifant.api.FileUtils
import spock.lang.Specification

/**
 * @uathor Schalk W.Cronjé
 */
class AsciidoctorPdfThemeExtensionSpec extends Specification {

    final static File TEST_THEMES_DIR = new File(
        System.getProperty(
            'TEST_THEMES_DIR',
            'asciidoctor-gradle-jvm/src/test/resources/themes'
        )
    ).absoluteFile

    Project project = ProjectBuilder.builder().build()
    AsciidoctorPdfThemesExtension pdfThemes

    void setup() {
        pdfThemes = project.extensions.create(
            AsciidoctorPdfThemesExtension.NAME,
            AsciidoctorPdfThemesExtension,
            project
        )
    }

    void 'Unregistered theme throws exception'() {
        when:
        pdfThemes.getByName('foo')

        then:
        thrown(UnknownDomainObjectException)
    }

    void 'Configure local theme'() {
        given:
        pdfThemes.local 'basic', {
            themeDir = 'foo'
        }

        when:
        AsciidoctorPdfThemesExtension.PdfThemeDescriptor theme = pdfThemes.getByName('basic')

        then:
        theme.themeName == 'basic'
        theme.themeDir == project.file('foo')
    }

    void 'Configure GitHub theme'() {
        given:
        pdfThemes.github 'basic', {
            baseUri = new File(TEST_THEMES_DIR, 'github').toURI()
            organisation = 'foo'
            repository = 'bar'
            branch = 'master'
            relativePath = 'some/path'
        }

        when:
        AsciidoctorPdfThemesExtension.PdfThemeDescriptor theme = pdfThemes.getByName('basic')

        then:
        theme.themeName == 'basic'
        theme.themeDir == new File(determineUnpackedDir('github-cache', 'master'), 'bar-master/some/path')
    }

    void 'Configure GitLab theme'() {
        given:
        pdfThemes.gitlab 'basic', {
            baseUri = new File(TEST_THEMES_DIR, 'gitlab').toURI()
            organisation = 'foo'
            repository = 'bar'
            branch = 'master'
            relativePath = 'some/path'
        }

        when:
        AsciidoctorPdfThemesExtension.PdfThemeDescriptor theme = pdfThemes.getByName('basic')

        then:
        theme.themeName == 'basic'
        theme.themeDir == new File(determineUnpackedDir('gitlab-cache', 'bar-master'), 'bar-master/some/path')
    }

    File determineUnpackedDir(String cacheSubDir, String pattern) {
        File baseDir = project.file("${project.buildDir}/${cacheSubDir}/foo/bar/${pattern}")
        FileUtils.listDirs(baseDir)[0]
    }
}