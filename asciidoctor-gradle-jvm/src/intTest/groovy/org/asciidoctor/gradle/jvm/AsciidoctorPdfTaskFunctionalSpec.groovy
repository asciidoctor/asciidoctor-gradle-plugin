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

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.jvm.generators.PdfBackendJRubyAsciidoctorJCombinationGenerator
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

class AsciidoctorPdfTaskFunctionalSpec extends FunctionalSpecification {

    static final String DEFAULT_TASK = 'asciidoctorPdf'
    static final String DEFAULT_OUTPUT_FILE = 'build/docs/asciidocPdf/sample.pdf'

    void setup() {
        createTestProject()
    }

    @Unroll
    void 'Run a PDF generator with intermediateWorkDir=#intermediateMode and parallel=#parallelMode'() {
        given:
        getBuildFile("""
asciidoctorPdf {
    sourceDir 'src/docs/asciidoc'

    ${intermediateMode ? 'useIntermediateWorkDir()' : ''}
    parallelMode ${parallelMode}
}
""")

        when:
        getGradleRunner([DEFAULT_TASK, '-s']).build()

        then:
        verifyAll {
            new File(testProjectDir.root, DEFAULT_OUTPUT_FILE).exists()
        }

        where:
        parallelMode | intermediateMode
        true         | true
        true         | false
        false        | true
        false        | false
    }

    @Unroll
    @SuppressWarnings('DuplicateStringLiteral')
    void '#combination (Groovy DSL)'() {
        given:
        getBuildFile("""
asciidoctorPdf {

    ${defaultProcessModeForAppveyor}

    asciidoctorj {
        version = '${combination.asciidoctorjVer}'
        jrubyVersion = '${combination.jrubyVer}'

        ${getResolutionStrategy(combination.asciidoctorjVer, combination.jrubyVer)}
    }

    sourceDir 'src/docs/asciidoc'
}
""")
        GradleRunner runner = getGradleRunner([DEFAULT_TASK, '-s', '-i'])

        when:
        combination.compatible ? runner.build() : runner.buildAndFail()

        then:
        combination.compatible && new File(testProjectDir.root, DEFAULT_OUTPUT_FILE).exists() || !combination.compatible

        where:
        combination << PdfBackendJRubyAsciidoctorJCombinationGenerator.get()
    }

    void 'Pdf generation can be run in JAVA_EXEC process mode'() {
        given:
        getBuildFile("""
        asciidoctorPdf {
            sourceDir 'src/docs/asciidoc'

            inProcess = JAVA_EXEC
        }
        """)

        when:
        getGradleRunner([DEFAULT_TASK]).build()

        then:
        verifyAll {
            new File(testProjectDir.root, DEFAULT_OUTPUT_FILE).exists()
        }
    }

    void 'Custom theme for PDF'() {
        given:
        getBuildFile("""
        pdfThemes {
            local 'basic', {
                styleDir = 'src/docs/asciidoc/pdf-theme'
            }
        }

        asciidoctorPdf {
            theme 'basic'
            sourceDir 'src/docs/asciidoc'
            fontsDir 'src/docs/asciidoc/pdf-theme'
        }
        """)

        when:
        getGradleRunner([DEFAULT_TASK, '-i']).build()

        then:
        verifyAll {
            new File(testProjectDir.root, DEFAULT_OUTPUT_FILE).exists()
        }
    }

    File getBuildFile(String extraContent) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'org.asciidoctor.jvm.pdf'
}

${offlineRepositories}

${extraContent}
"""
        buildFile
    }

    String getResolutionStrategy(final String asciidoctorjVer, final String jrubyVer) {
        if (asciidoctorjVer.startsWith('1.6') && jrubyVer.startsWith('9.')) {
            """resolutionStrategy { ResolutionStrategy rs ->
                rs.eachDependency { details ->
                    if (details.requested.name == 'jruby-complete') {
                        details.useVersion '${jrubyVer}'
                    }
                }
            }
            """
        } else {
            ''
        }
    }
}