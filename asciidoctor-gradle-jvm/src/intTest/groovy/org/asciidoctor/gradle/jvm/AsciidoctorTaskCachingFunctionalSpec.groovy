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
import org.asciidoctor.gradle.testfixtures.jvm.CachingTest
import spock.lang.PendingFeature

import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.SERIES_16
import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.SERIES_20
import static org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions.AJ16_ABSOLUTE_MINIMUM
import static org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions.AJ16_SAFE_MAXIMUM
import static org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions.AJ20_ABSOLUTE_MINIMUM

class AsciidoctorTaskCachingFunctionalSpec extends FunctionalSpecification implements CachingTest {
    static final String DEFAULT_TASK = 'asciidoctor'
    static final String DEFAULT_OUTPUT_FILE = 'build/docs/asciidoc/html5/sample.html'
    static final String DOCBOOK_OUTPUT_FILE = 'build/docs/asciidoc/docbook/sample.xml'

    void setup() {
        setupCache()
        createTestProject()
    }

    @PendingFeature
    void "asciidoctor task is cacheable and relocatable"() {
        given:
        getBuildFile("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'

                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()
        file(DOCBOOK_OUTPUT_FILE).exists()

        when:
        assertDefaultTaskIsCachedAndRelocatable()

        then:
        outputFile.exists()
        file(DOCBOOK_OUTPUT_FILE).exists()
        outputFileInRelocatedDirectory.exists()
        fileInRelocatedDirectory(DOCBOOK_OUTPUT_FILE).exists()
    }

    @PendingFeature
    void "asciidoctor task is cached when only output directory is changed"() {
        given:
        getBuildFile("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'

                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                outputDir 'build/asciidoc'

                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        then:
        assertDefaultTaskIsCachedAndRelocatable()

        and:
        file('build/asciidoc/html5/sample.html').exists()
        file('build/asciidoc/docbook/sample.xml').exists()

        and:
        fileInRelocatedDirectory('build/asciidoc/html5/sample.html').exists()
        fileInRelocatedDirectory('build/asciidoc/docbook/sample.xml').exists()
    }

    @PendingFeature
    void "asciidoctor task is not cached when backends change"() {
        given:
        getBuildFile("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'

                outputOptions {
                    backends 'html5', 'html'
                }
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'

                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()
    }

    @PendingFeature
    void "asciidoctor task is not cached when asciidoctorj/jruby versions change"() {
        given:
        getBuildFile("""
            asciidoctorj {
                version = '${SERIES_20}'
                jrubyVersion = '${AJ20_ABSOLUTE_MINIMUM}'
            }
            asciidoctor {
                sourceDir 'src/docs/asciidoc'

                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo("""
            asciidoctorj {
                version = '${SERIES_16}'
                jrubyVersion = '${AJ16_ABSOLUTE_MINIMUM}'
            }

            asciidoctor {
                sourceDir 'src/docs/asciidoc'

                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()
    }

    @PendingFeature
    void "asciidoctor task is not cached when attributes change"() {
        given:
        getBuildFile("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'

                outputOptions {
                    backends 'html5', 'docbook'
                }

                attributes 'source-highlighter': 'coderay',
                            'imagesdir': 'images',
                            'toc': 'left',
                            'icons': 'font',
                            'setanchors': '',
                            'idprefix': '',
                            'idseparator': '-'
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'

                outputOptions {
                    backends 'html5', 'docbook'
                }

                attributes 'source-highlighter': 'coderay',
                            'imagesdir': 'images',
                            'toc': 'right',
                            'icons': 'font',
                            'setanchors': '',
                            'idprefix': '',
                            'idseparator': '--'
            }
        """)

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()
    }

    @Override
    File getOutputFile() {
        file(DEFAULT_OUTPUT_FILE)
    }

    @Override
    String getDefaultTask() {
        ":${DEFAULT_TASK}"
    }

    File getBuildFile(String extraContent) {
        getJvmConvertGroovyBuildFile("""
            ${ -> scan ? buildScanConfiguration : '' }

            asciidoctorj {
                jrubyVersion = '${AJ16_SAFE_MAXIMUM}'
            }

            ${extraContent}
        """)
    }
}
