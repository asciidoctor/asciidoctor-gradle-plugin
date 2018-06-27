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

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.SERIES_15
import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.SERIES_16
@java.lang.SuppressWarnings('NoWildcardImports')
import static org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions.*

@SuppressWarnings('MethodName')
class AsciidoctorPdfTaskFunctionalSpec extends FunctionalSpecification {

    static final String DEFAULT_TASK = 'asciidoctorPdf'
    static final String DEFAULT_OUTPUT_FILE = 'build/docs/asciidocPdf/sample.pdf'

    void setup() {
        createTestProject()
    }

    @SuppressWarnings('DuplicateStringLiteral')
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
    void 'PDF backend Is #text compatible with AsciidoctorJ=#asciidoctorjVer + min JRuby=#jrubyVer'() {
        given:
        boolean compatible = text.empty

        getBuildFile("""
asciidoctorPdf {

    ${defaultProcessModeForAppveyor}

    asciidoctorj {
        version = '${asciidoctorjVer}'  
        jrubyVersion = '${jrubyVer}'

        ${getResolutionStrategy(asciidoctorjVer, jrubyVer)}
    }
    
    sourceDir 'src/docs/asciidoc'
}
""")
        GradleRunner runner = getGradleRunner([DEFAULT_TASK, '-s'])

        when:
        compatible ? runner.build() : runner.buildAndFail()

        then:
        compatible && new File(testProjectDir.root, DEFAULT_OUTPUT_FILE).exists() || !compatible

        where:
        jrubyVer              | asciidoctorjVer | text
        AJ15_ABSOLUTE_MAXIMUM | SERIES_15       | 'not'
        AJ15_ABSOLUTE_MINIMUM | SERIES_15       | ''
        AJ15_SAFE_MAXIMUM     | SERIES_15       | ''
        AJ15_SAFE_MINIMUM     | SERIES_15       | ''
        AJ16_ABSOLUTE_MAXIMUM | SERIES_16       | ''
        AJ16_ABSOLUTE_MINIMUM | SERIES_16       | ''
        AJ16_SAFE_MAXIMUM     | SERIES_16       | ''
        AJ16_SAFE_MINIMUM     | SERIES_16       | ''
    }

    @SuppressWarnings('DuplicateStringLiteral')
    void 'Pdf generation can be run in JAVA_EXEC process mode'() {
        given:
        getBuildFile("""
asciidoctorPdf {
    sourceDir 'src/docs/asciidoc'

    inProcess = JAVA_EXEC    
}
""")

        when:
        getGradleRunner([DEFAULT_TASK, '-s']).build()

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
        if (asciidoctorjVer.startsWith('1.6') && jrubyVer.startsWith('9.0')) {

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