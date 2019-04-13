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
package org.asciidoctor.gradle.slides.export

import org.asciidoctor.gradle.slides.export.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

class Deck2ExportFunctionalSpec extends FunctionalSpecification {

    void 'Standalone asciidoctor slide task can be exported'() {
        setup:
        createTestProject('generic')
        getBuildFile('''
        import org.asciidoctor.gradle.slides.export.deck2pdf.Deck2PdfTask
        
        task standalonePdfConverter(type: Deck2PdfTask) {
            outputDir "${buildDir}/generic"
            slides asciidoctorRevealJs
            profile 'reveal_js'    
        }
        ''')

        when:
        getGradleRunner(['standalonePdfConverter', '-i']).build()

        then:
        new File(testProjectDir.root, 'build/generic/index.pdf').exists()
    }

    @Unroll
    void 'Reveal.js task can be exported to #format'() {
        setup:
        createTestProject('revealjs')

        when:
        runBuildFileForRevealJS(format)

        then:
        expectedFilesForFormat(format).every { it.exists() }

        where:
        format << ['pdf', 'jpg', 'png']
    }

    void 'Run task with parameters set from command-line'() {
        setup:
        createTestProject('generic')
        getBuildFile('''
        import org.asciidoctor.gradle.slides.export.deck2pdf.Deck2PdfTask
            
        task standalonePdfConverter(type: Deck2PdfTask) {
            outputDir "${buildDir}/generic"
            slides tasks.getByName('asciidoctorRevealJs')
            profile 'revealjs'
            
            doFirst {
                println "*** Height=${height} Width=${width}"
            }
        }
        ''')

        when:
        BuildResult result = getGradleRunner(
                ['standalonePdfConverter', '--height=768', '--width==1024', '-i']
        ).build()

        then:
        result.output.contains('*** Height=768 Width=1024')
    }

    void 'Run jpg converter task with parameters set from command-line'() {
        setup:
        createTestProject('generic')
        getBuildFile('''
        import org.asciidoctor.gradle.slides.export.deck2pdf.Deck2PdfTask
            
        task standalonePdfConverter(type: Deck2JpgTask) {
            outputDir "${buildDir}/generic"
            slides tasks.getByName('asciidoctorRevealJs')
            profile 'revealjs'
            
            doFirst {
                println "*** Height=${height} Width=${width} Quality=${quality}"
            }
        }
        ''')

        when:
        BuildResult result = getGradleRunner(
                ['standalonePdfConverter', '--height=768', '--width==1024', '--quality=75', '-i']
        ).build()

        then:
        result.output.contains('*** Height=768 Width=1024 Quality=75')
    }

    private BuildResult runBuildFileForRevealJS(String outputFormat, List<String> args = ['-i']) {
        String format = outputFormat.capitalize()

        getBuildFile("""
        asciidoctorRevealJS {
            sourceDir 'src/docs/asciidoc'
            desk2pdf {
                skipFragments = true
            }
        }
        
        asciidoctorRevealJSTo${format} {
            height = 1024
            width = 1280
            ${outputFormat == 'jpg' ? 'quality = 75' : ''}
            ${outputFormat == 'pdf' ? '' : '__%02d__'}
        }
        """)

        final List<String> finalArgs = ["asciidoctorRevealJSTo${format}"]
        finalArgs.addAll(args)
        getGradleRunner(finalArgs).build()
    }

    private List<File> expectedFilesForFormat(String outputFormat) {
        String formatName = outputFormat.capitalize()
        File baseOutputDir = new File(testProjectDir.root, "build/docs/asciidocRevealJSTo${formatName}")
        List<String> paths = []

        switch (outputFormat) {
            case 'pdf':
                paths.addAll(['index.pdf', 'subdir/index2.pdf'])
                break
            case 'jpg':
            case 'png':
                paths.addAll(
                        (1..2).collectMany {
                            ["index__0${it}__.${outputFormat}", "subdir/index2__0${it}__.${outputFormat}"]
                        })
                break
        }

        paths.collect {
            new File(baseOutputDir, it)
        }
    }

    private File getBuildFile(String extraContent) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'org.asciidoctor.jvm.revealjs'
    id 'org.asciidoctor.deck2pdf'
}

${offlineRepositories}

${extraContent}
"""
        buildFile
    }

}