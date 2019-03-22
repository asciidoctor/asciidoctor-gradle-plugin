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
package org.asciidoctor.gradle.jvm.slides

import org.asciidoctor.gradle.jvm.slides.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions
import org.gradle.testkit.runner.BuildResult
import spock.lang.PendingFeature

@SuppressWarnings(['DuplicateStringLiteral', 'DuplicateListLiteral'])
class AsciidoctorRevealJSTaskFunctionalSpec extends FunctionalSpecification {

    final static String JRUBY_TEST_VERSION = JRubyTestVersions.AJ16_SAFE_MAXIMUM

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
            new File(testProjectDir.root, 'build/docs/asciidocRevealJs/revealjs.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidocRevealJs/reveal.js/css').exists()
            new File(testProjectDir.root, 'build/docs/asciidocRevealJs/reveal.js/lib').exists()
            new File(testProjectDir.root, 'build/docs/asciidocRevealJs/reveal.js/plugin').exists()
            new File(testProjectDir.root, 'build/docs/asciidocRevealJs/reveal.js/js').exists()
        }
    }

    void 'Run a revealJS generator with a custom template'() {

        given:
        getBuildFile('''
        revealjs {
            templateGitHub {
                organisation = 'hakimel'
                repository = 'reveal.js'
                tag = '3.6.0'
            }
        }
        ''')

        when:
        build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidocRevealJs/revealjs.html').exists()
            new File(testProjectDir.root, 'build/github-cache/hakimel/reveal.js/3.6.0').exists()
            new File(testProjectDir.root, 'build/docs/asciidocRevealJs/reveal.js/js/reveal.js').text.contains('var VERSION = \'3.6.0\';')
        }
    }

    // Only supported with reveal.js > 1.1.3
    @PendingFeature
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
            plugins 'rajgoel/chart/Chart.js'
            pluginConfigurationFile 'src/docs/asciidoc/empty-plugin-configuration.js'
            toggleBuiltinPlugin 'pdf', true
            toggleBuiltinPlugin 'notes', false
        }
        ''')

        when:
        build()
        File revealjsHtml = new File(testProjectDir.root, 'build/docs/asciidocRevealJs/revealjs.html')
        File pluginList = new File(testProjectDir.root, 'build/docs/asciidocRevealJs/reveal.js/revealjs-plugins.js')
        File pluginConfig = new File(testProjectDir.root, 'build/docs/asciidocRevealJs/reveal.js/revealjs-plugin-configuration.js')

        then:
        verifyAll {
            !revealjsHtml.text.contains("src: 'reveal.js/plugin/notes/notes.js'")
            revealjsHtml.text.contains("src: 'reveal.js/plugin/print-pdf/print-pdf.js'")
            pluginList.text.contains('reveal.js/plugin/rajgoel/chart/Chart.js')
            pluginConfig.exists()
            new File(testProjectDir.root, 'build/docs/asciidocRevealJs/reveal.js/plugin/rajgoel/chart/Chart.js').exists()
        }
    }


    @SuppressWarnings('FactoryMethodName')
    BuildResult build() {
        getGradleRunner(['asciidoctorRevealJs', '-s']).build()
    }

    File getBuildFile(String extraContent) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
        plugins {
            id 'org.asciidoctor.jvm.revealjs'
        }

        ${offlineRepositories}

        repositories {
            maven { url 'http://rubygems-proxy.torquebox.org/releases' } 
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

}

//    /** Adds files to a {@link CopySpec} for copying to final artifact.
//     *
//     * @param cs CopySpec to enhance.
//     */
//    void enhanceCopySpec(CopySpec cs) {
//        copyActionFor(cs, parallaxBackgroundImageIfFile, getParallaxBackgroundImageRelativePath())
//        copyActionFor(cs, highlightJsThemeLocationIfFile, getHighlightJsThemeRelativePath())
//        copyActionFor(cs, customThemeLocationIfFile, getCustomThemeRelativePath())


