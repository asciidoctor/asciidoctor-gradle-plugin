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
package org.asciidoctor.gradle.jvm.slides

import org.asciidoctor.gradle.jvm.slides.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.CachingTest
import spock.lang.Issue
import spock.lang.PendingFeature

import static org.asciidoctor.gradle.testfixtures.JRubyTestVersions.AJ20_SAFE_MAXIMUM

@SuppressWarnings(['UnnecessaryGetter'])
class AsciidoctorRevealJSTaskCachingFunctionalSpec extends FunctionalSpecification implements CachingTest {
    private static final String DEFAULT_TASK = 'asciidoctorRevealJs'
    private static final String JRUBY_TEST_VERSION = AJ20_SAFE_MAXIMUM
    private static final String DEFAULT_REVEALJS_PATH = 'build/docs/asciidocRevealJs'

    void setup() {
        setupCache()
        createTestProject('revealjs')
    }

    @PendingFeature
    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/485')
    void "Revealjs task is cacheable and relocatable"() {
        given:
        getBuildFile()

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

    @PendingFeature
    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/485')
    void "Revealjs task is cached when only output directory is changed"() {
        given:
        getBuildFile()

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo('''
            asciidoctorRevealJs {
                outputDir 'build/asciidoc'
            }
        ''')

        then:
        assertDefaultTaskIsCachedAndRelocatable()

        then:
        file('build/asciidoc/revealjs.html').exists()
        fileInRelocatedDirectory('build/asciidoc/revealjs.html').exists()
    }

    @PendingFeature
    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/485')
    void "Revealjs task is not cached when templates are added"() {
        given:
        getBuildFile()

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo('''
            revealjs {
                templateGitHub {
                    organisation = 'hakimel'
                    repository = 'reveal.js'
                    tag = '3.6.0'
                }
            }
        ''')

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()

        and:
        outputFile.exists()
        outputFileInRelocatedDirectory.exists()
    }

    @PendingFeature
    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/485')
    void "Revealjs task is not cached when plugins are added"() {
        given:
        getBuildFile

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo('''
            revealjsPlugins {
                github 'rajgoel', {
                    organisation = 'rajgoel'
                    repository = 'reveal.js-plugins'
                    branch = 'master'
                }
            }
    
            asciidoctorRevealJs {
                plugins 'rajgoel/chart/Chart.js'
                pluginConfigurationFile 'src/docs/asciidoc/empty-plugin-configuration.js'
                toggleBuiltinPlugin 'pdf', true
                toggleBuiltinPlugin 'notes', false
            }
        ''')

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()

        and:
        outputFile.exists()
        outputFileInRelocatedDirectory.exists()
    }

    @PendingFeature
    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/485')
    void "Revealjs task is not cached when attributes change"() {
        given:
        getBuildFile('''
            asciidoctorRevealJs {
                revealjsOptions {
                    controls = false
                }
            }
        ''')

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo('''
            asciidoctorRevealJs {
                revealjsOptions {
                    controls = true
                }
            }
        ''')

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()

        and:
        outputFile.exists()
        outputFileInRelocatedDirectory.exists()
    }

    @Override
    File getBuildFile(String extraContent) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'org.asciidoctor.jvm.revealjs'
            }
    
            ${scan ? buildScanConfiguration : ''}
            ${offlineRepositories}
    
            repositories {
                ruby.gems()
            }
    
            asciidoctorRevealJs {
                sourceDir 'src/docs/asciidoc'
    
                asciidoctorj {
                    jrubyVersion = '${JRUBY_TEST_VERSION}'
                }
    
                sources {
                    include 'revealjs.adoc'
                }
            }
    
            ${extraContent}
        """
        buildFile
    }

    @Override
    File getOutputFile() {
        file("${DEFAULT_REVEALJS_PATH}/revealjs.html")
    }

    @Override
    String getDefaultTask() {
        ":${DEFAULT_TASK}"
    }
}
