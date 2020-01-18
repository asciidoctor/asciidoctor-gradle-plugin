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
package org.asciidoctor.gradle.jvm.epub

import org.asciidoctor.gradle.jvm.epub.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.JRubyTestVersions
import org.gradle.testkit.runner.BuildResult
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Unroll

class AsciidoctorEpubTaskFunctionalSpec extends FunctionalSpecification {

    final static String JRUBY_TEST_VERSION = JRubyTestVersions.AJ20_SAFE_MAXIMUM

    void setup() {
        createTestProject()
    }

    void 'Run a EPUB generator with format EPUB3 (only in JAVA_EXEC mode)'() {
        given:
        getSingleFormatBuildFile('EPUB3')

        when:
        BuildResult result = getGradleRunner(['asciidoctorEpub', '-s', '-i']).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidocEpub/epub3.epub').exists()
            !result.output.contains('include file not found:')
        }
    }

    // kindlegen is only available as a 32-bit executable and won't run on MacOS Catalina
    @IgnoreIf({ isWindowsOr64bitOnlyMacOS() })
    void 'Non-Windows: Run a EPUB generator with format KF8 (only in JAVA_EXEC mode on)'() {
        given:
        getSingleFormatBuildFile('KF8')

        when:
        BuildResult result = getGradleRunner(['asciidoctorEpub', '-s', '-i']).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidocEpub/epub3.mobi').exists()
            !result.output.contains('include file not found:')
        }
    }

    @Issue('https://github.com/asciidoctor/asciidoctorj/issues/659')
    @IgnoreIf({ !FunctionalSpecification.OS.windows })
    @PendingFeature
    void 'Windows: Run a EPUB generator with format KF8 (only in JAVA_EXEC mode on)'() {
        given:
        getSingleFormatBuildFile('KF8')

        when:
        BuildResult result = getGradleRunner(['asciidoctorEpub', '-s', '-i']).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidocEpub/epub3.mobi').exists()
            !result.output.contains('include file not found:')
        }
    }

    @PendingFeature
    @Unroll
    void 'Run a EPUB generator with multiple formats in order #formatOrder (only in JAVA_EXEC mode)'() {
        given:
        getBuildFile("""

        asciidoctorEpub {
            sourceDir 'src/docs/asciidoc'
            ebookFormats ${formatOrder}

            kindlegen {
                agreeToTermsOfUse = true
            }

            asciidoctorj {
                jrubyVersion = '${JRUBY_TEST_VERSION}'
            }

            sources {
                include 'epub3.adoc'
            }
        }
        """)

        when:
        BuildResult result = getGradleRunner(['asciidoctorEpub', '-s', '-i']).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidocEpub/epub3.mobi').exists()
            new File(testProjectDir.root, 'build/docs/asciidocEpub/epub3.epub').exists()
            !result.output.contains('include file not found:')
        }

        where:
        formatOrder << ['EPUB3, KF8', 'KF8, EPUB3']
    }

    void 'eBookFormats may not be empty'() {
        given:
        getBuildFile('''
        asciidoctorEpub {
            sourceDir 'src/docs/asciidoc'
            ebookFormats = []
            sources {
                include 'epub3.adoc'
            }
        }
        ''')

        when:
        BuildResult result = getGradleRunner(['asciidoctorEpub' , '-i']).buildAndFail()

        then:
        result.output.contains('No eBook format specified for task')
    }

    File getSingleFormatBuildFile(final String format) {
        getBuildFile( """

        asciidoctorEpub {
            sourceDir 'src/docs/asciidoc'
            ebookFormats ${format}

            kindlegen {
                agreeToTermsOfUse = true
            }

            asciidoctorj {
                jrubyVersion = '${JRUBY_TEST_VERSION}'
            }

            sources {
                include 'epub3.adoc'
            }
        }
        """)
    }

    File getBuildFile(String extraContent) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'org.asciidoctor.jvm.epub'
}

${offlineRepositories}

${extraContent}
"""
        buildFile
    }

}