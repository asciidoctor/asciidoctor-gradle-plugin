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
package org.asciidoctor.gradle.jvm.slides

import org.asciidoctor.gradle.jvm.slides.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.JRubyTestVersions
import org.gradle.testkit.runner.BuildResult

class AsciidoctorRevealJSTaskFunctionalSpec extends FunctionalSpecification {

    final static String JRUBY_TEST_VERSION = JRubyTestVersions.AJ20_SAFE_MAXIMUM
    final static String REVEALJS_DIR_NAME = 'reveal.js'
    final static String DEFAULT_REVEALJS_PATH = "build/docs/asciidocRevealJs/${REVEALJS_DIR_NAME}"

    void setup() {
        createTestProject('revealjs')
    }

    void 'Run a RevealJS generator'() {
        given:
        getBuildFile('')

        when:
        build()

        then:
        verifyAll {
            new File(projectDir, 'build/docs/asciidocRevealJs/revealjs.html').exists()
            new File(projectDir, 'build/docs/asciidocRevealJs/subdir/revealjs2.html').exists()
            new File(projectDir, "${DEFAULT_REVEALJS_PATH}/css").exists()
            new File(projectDir, "${DEFAULT_REVEALJS_PATH}/dist").exists()
            new File(projectDir, "${DEFAULT_REVEALJS_PATH}/plugin").exists()
            new File(projectDir, "${DEFAULT_REVEALJS_PATH}/js").exists()
        }
    }

    void 'Run a revealJS generator with a custom template'() {
        given:
        getBuildFile('''
        revealjs {
            templateGitHub {
                organisation = 'hakimel'
                repository = 'reveal.js'
                tag = '5.1.0'
            }
        }
        ''')

        when:
        build()

        then:
        verifyAll {
            new File(projectDir, 'build/docs/asciidocRevealJs/revealjs.html').exists()
            new File(projectDir, 'build/github-cache/hakimel/reveal.js/5.1.0').exists()
            new File(projectDir, "${DEFAULT_REVEALJS_PATH}/dist").exists()
            new File(projectDir, "${DEFAULT_REVEALJS_PATH}/js/reveal.js").
                text.contains('export const VERSION = \'5.1.0\';')
        }
    }

    // Only supported with reveal.js > 1.1.3
    void 'Run a revealJS generator with plugins'() {
        given:
        getBuildFile('''
        revealjsPlugins {
            github 'rajgoel', {
                organisation = 'rajgoel'
                repository = 'reveal.js-plugins'
                branch = 'master'
            }
        }

        asciidoctorRevealJs {
            plugins 'rajgoel/chart/plugin.js'
            toggleBuiltinPlugin 'search', true
            toggleBuiltinPlugin 'notes', false
        }
        ''')

        when:
        build()
        String revealjsHtml = new File(projectDir, 'build/docs/asciidocRevealJs/revealjs.html').text
        File pluginList = new File(projectDir, "${DEFAULT_REVEALJS_PATH}/revealjs-plugins.js")

        then:
        verifyAll {
            !pluginList.exists()
            !revealjsHtml.contains('plugin/notes')
            revealjsHtml.contains("src: '${REVEALJS_DIR_NAME}/plugin/search/search.js'")
            // plugins must be registered using presentation-docinfo-footer,
            // see https://docs.asciidoctor.org/reveal.js-converter/5.0/converter/revealjs-plugins/#additional-plugins
            // revealjsHtml.contains("src: '${REVEALJS_DIR_NAME}/plugin/rajgoel/chart/plugin.js'")
            new File(projectDir, "${DEFAULT_REVEALJS_PATH}/plugin/rajgoel/chart/plugin.js").exists()
        }
    }

    BuildResult build() {
        getGradleRunner(['asciidoctorRevealJs', '-s']).build()
    }

    File getBuildFile(String extraContent) {
        buildFile << """
        plugins {
            id 'org.asciidoctor.jvm.revealjs'
        }

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
                include 'subdir/revealjs2.adoc'
            }
        }

        ${extraContent}
"""
        buildFile
    }
}

