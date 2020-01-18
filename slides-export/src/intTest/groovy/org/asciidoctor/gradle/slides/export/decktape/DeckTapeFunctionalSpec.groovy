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
package org.asciidoctor.gradle.slides.export.decktape

import org.asciidoctor.gradle.slides.export.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult
import org.ysb33r.grolifant.api.OperatingSystem
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.Stepwise
import spock.lang.Timeout
import spock.lang.Unroll

// Only using Stepwise as an interim measure
// See https://discuss.gradle.org/t/component-metadata-supplier-rule-executor-seems-to-be-causing-issues/31844
@Stepwise
class DeckTapeFunctionalSpec extends FunctionalSpecification {

    private static final boolean BASE_ONLY = true

    @Unroll
    void 'Cannot set only #measure'() {
        setup:
        createTestProject('generic')
        getBuildFile("""
        import org.asciidoctor.gradle.slides.export.decktape.DeckTapeTask
        
        task standalonePdfConverter(type: DeckTapeTask) {
            outputDir "\${buildDir}/generic"
            slides asciidoctorRevealJs
            profile 'reveal_js' 
            ${measure.startsWith('screenshots') ? 'screenshots.format = "png"' : ''}
            ${measure} = 1024
        }
        """, BASE_ONLY)

        when:
        BuildResult result = getGradleRunner(['standalonePdfConverter', '-i']).buildAndFail()

        then:
        result.output.contains('Must specify both height and width')

        where:
        measure << ['height', 'width', 'screenshots.height', 'screenshots.width']
    }

    @Unroll
    @Timeout(240)
    void 'Standalone asciidoctor slide task can be exported with #profile profile'() {
        setup:
        withBuildScan = true
        createTestProject('generic')
        getBuildFile("""
        import org.asciidoctor.gradle.slides.export.decktape.DeckTapeTask
        
        asciidoctorRevealJs {
            sourceDir 'src/docs/asciidoc'
            sources {
                include 'index.adoc'
            }
        }

        task standalonePdfConverter(type: DeckTapeTask) {
            outputDir "\${buildDir}/generic"
            slides = [asciidoctorRevealJs] // Exercise setSlides()
            width = 1024
            height = 768
            ${profileDSL}  
            ${chromeSandbox}  
        }
        """, BASE_ONLY)

        when:
        getGradleRunner(['standalonePdfConverter', '-i']).build()

        then:
        new File(projectDir, 'build/generic/index.pdf').exists()

        where:
        profile     | profileDSL
        'reveal.js' | "profile 'reveal_js'"
        'generic'   | 'useGenericProfile()'
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/381')
    @IgnoreIf({ OperatingSystem.current().isWindows() })
    @Timeout(240)
    @Unroll
    void 'Standalone task can also export to #format in addition to PDF'() {
        setup:
        withBuildScan = true
        createTestProject('generic')
        getBuildFile("""
        import org.asciidoctor.gradle.slides.export.decktape.DeckTapeTask
        
        asciidoctorRevealJs {
            sourceDir 'src/docs/asciidoc'
            sources {
                include 'index.adoc'
            }
            theme 'beige'
        }

        task standalonePdfConverter(type: DeckTapeTask) {
            outputDir "\${buildDir}/generic"
            slides asciidoctorRevealJs
            profile 'reveal_js'
            screenshots {
                format = '${format}'
                width = 1024
                height = 768
            }    
            ${chromeSandbox}  
        }
        """, BASE_ONLY)

        when:
        getGradleRunner(['standalonePdfConverter', '-i']).build()

        then:
        new File(projectDir, "build/generic/index_1_1024x768.${format}").exists()

        where:
        format << ['jpg', 'png']
    }

    void 'Run task with parameters set from command-line'() {
        setup:
        createTestProject('generic')
        getBuildFile("""
        import org.asciidoctor.gradle.slides.export.decktape.DeckTapeTask
        
        asciidoctorRevealJs {
            sourceDir 'src/docs/asciidoc'
            sources {
                include 'index.adoc'
            }
            theme 'beige'
        }

        task standalonePdfConverter(type: DeckTapeTask) {
            outputDir "\${buildDir}/generic"
            slides asciidoctorRevealJs
            profile 'reveal_js' 
            ${chromeSandbox}  

            doLast {
                println "*** Height=\${height} Width=\${width} Range=\${range}" + 
                    " LoadPause=\${loadPause} Pause=\${interSlidePause}"
            }
        }
        """, BASE_ONLY)

        when:
        BuildResult result = getGradleRunner([
            '-i',
            'standalonePdfConverter',
            '--width=1024',
            '--height=768',
            '--range=2-3',
            '--pause=1000',
            '--load-pause=500'
        ]).build()

        then:
        result.output.contains('*** Height=768 Width=1024 Range=2-3 LoadPause=500 Pause=1000')
    }

    void 'Export tasks can be auto-created to compliment Reveal.JS tasks'() {
        setup:
        createTestProject('revealjs')
        getBuildFile("""
        asciidoctorRevealJs {
            sourceDir 'src/docs/asciidoc'
        }
        
        asciidoctorRevealJsExport {
            ${chromeSandbox}
        }  
        """)

        when:
        getGradleRunner(['-i', 'asciidoctorRevealJsExport']).build()

        then:
        new File(projectDir, 'build/docs/asciidocRevealJs/index.html').exists()
        new File(projectDir, 'build/docs/asciidocRevealJsExport/index.pdf').exists()
    }

    private File getBuildFile(String extraContent, boolean baseOnly = false) {
        File buildFile = new File(projectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'org.asciidoctor.jvm.revealjs'
                id 'org.asciidoctor.decktape${baseOnly ? '.base' : ''}'
            }
 
            ${buildScanTerm}

            ${offlineRepositories}
            
            ${extraContent}
            
            repositories {
                ruby.gems()
            }
        """
        buildFile
    }

    private String getBuildScanTerm() {
        if (withBuildScan) {
            '''
            buildScan {
                termsOfServiceUrl = 'https://gradle.com/terms-of-service'
                termsOfServiceAgree = 'yes'
            }           
            '''
        } else {
            ''
        }
    }

    private String getChromeSandbox() {
        if (System.getProperty('NO_CHROME_SANDBOX')) {
            "chromeArgs '--no-sandbox'"
        } else {
            ''
        }
    }
}