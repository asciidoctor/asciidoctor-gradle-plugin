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

import org.asciidoctor.gradle.jvm.pdf.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.CachingTest

/** AsciidoctorPdfTaskCachingFunctionalSpec
 *
 * @author Gary Hale
 */
class AsciidoctorPdfTaskCachingFunctionalSpec extends FunctionalSpecification implements CachingTest {
    static final String DEFAULT_TASK = 'asciidoctorPdf'
    static final String DEFAULT_OUTPUT_FILE = 'build/docs/asciidocPdf/sample.pdf'

    void setup() {
        setupCache()
        createTestProject()
    }

    void "PDF task is cacheable and relocatable"() {
        given:
        getBuildFile("""
            asciidoctorPdf {
                sourceDir 'src/docs/asciidoc'
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        assertDefaultTaskIsCachedAndRelocatable()

        then:
        outputFile.exists()
        outputFileInRelocatedDirectory.exists()
    }

    void "PDF task is not cached when pdf-specific inputs change"() {
        given:
        getBuildFile("""
            pdfThemes {
                local 'basic', {
                    themeDir = 'src/docs/asciidoc/pdf-theme'
                }
            }
            
            asciidoctorPdf {
                sourceDir 'src/docs/asciidoc'
                fontsDir 'src/docs/asciidoc/pdf-theme'
                theme 'basic'
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        file('src/docs/themes/pdf-theme').mkdirs()
        file('src/docs/themes/pdf-theme/basic-theme.yml').text =
            file('src/docs/asciidoc/pdf-theme/basic-theme.yml').text
                .replace('333333', '333334')

        changeBuildConfigurationTo("""
            pdfThemes {
                local 'basic', {
                    themeDir = 'src/docs/themes/pdf-theme'
                }
            }
            
            asciidoctorPdf {
                sourceDir 'src/docs/asciidoc'
                fontsDir 'src/docs/themes/pdf-theme'
                theme 'basic'
            }
        """)

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()

        and:
        outputFile.exists()
        outputFileInRelocatedDirectory.exists()
    }

    File getBuildFile(String extraContent) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'org.asciidoctor.jvm.pdf'
            }
            
            ${ -> scan ? buildScanConfiguration : '' }
            ${offlineRepositories}
            
            ${extraContent}
        """
        buildFile
    }

    String getDefaultTask() {
        ":${DEFAULT_TASK}"
    }

    File getOutputFile() {
        file(DEFAULT_OUTPUT_FILE)
    }
}
