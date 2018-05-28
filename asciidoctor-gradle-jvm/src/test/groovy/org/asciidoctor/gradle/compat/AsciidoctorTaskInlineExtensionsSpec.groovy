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
package org.asciidoctor.gradle.compat

import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import spock.lang.Specification

/**
 * Asciidoctor task inline extensionRegistry specification
 *
 * @author Robert Panzer
 */
@SuppressWarnings(['DuplicateStringLiteral', 'MethodName', 'UnnecessaryGString', 'Println'])
class AsciidoctorTaskInlineExtensionsSpec extends Specification {

    private static final String ASCIIDOCTOR = 'asciidoctor'
    private static final String ASCIIDOC_RESOURCES_DIR = 'build/resources/test/src/asciidocextensions'
    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidocextensions'
    private static final String ASCIIDOC_MACRO_EXTENSION_SCRIPT = 'blockMacro.groovy'
    private static final String ASCIIDOC_INLINE_EXTENSIONS_FILE = 'inlineextensions.asciidoc'
    private static final String ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE = 'inlineextensions.html'

    Project project
    File testRootDir
    File srcDir
    File outDir

    def setup() {
        project = ProjectBuilder.builder().withName('test').build()
        project.configurations.create(ASCIIDOCTOR)
        testRootDir = new File('.')
        srcDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR).absoluteFile
        outDir = new File(project.projectDir, ASCIIDOC_BUILD_DIR)
    }

    def "Should apply inline BlockProcessor"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                sourceDir = srcDir
                sources {
                    include ASCIIDOC_INLINE_EXTENSIONS_FILE
                }
                outputDir = outDir
                extensions {
                    block(name: "BIG", contexts: [":paragraph"]) {
                        parent, reader, attributes ->
                        def upperLines = reader.readLines()*.toUpperCase()
                        .inject("") {a, b -> a + '\n' + b}

                        createBlock(parent, "paragraph", [upperLines], attributes, [:])
                    }
                    block("small") {
                        parent, reader, attributes ->
                        def lowerLines = reader.readLines()*.toLowerCase()
                        .inject("") {a, b -> a + '\n' + b}

                        createBlock(parent, "paragraph", [lowerLines], attributes, [:])
                    }

                }
            }
            File resultFile = new File(outDir, 'html5' + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
        when:
            task.processAsciidocSources()
        then:
            resultFile.exists()
            resultFile.text.contains("WRITE THIS IN UPPERCASE")
            resultFile.text.contains("and write this in lowercase")
    }

    def "Should apply BlockProcessor from file"() {
        given:
            print project.files('src/test/resources/src/asciidocextensions/blockMacro.groovy').each {println ">>> $it"}
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                sourceDir = srcDir
                sources {
                    include ASCIIDOC_INLINE_EXTENSIONS_FILE
                }
                outputDir = outDir
                extensions new File(sourceDir, ASCIIDOC_MACRO_EXTENSION_SCRIPT)
            }
            File resultFile = new File(outDir, 'html5' + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
        when:
            task.processAsciidocSources()
        then:
            resultFile.exists()
            resultFile.text.contains("WRITE THIS IN UPPERCASE")
            resultFile.text.contains("and write this in lowercase")
    }

    def "Should apply inline Postprocessor"() {
        given:
        String copyright = "Copyright Acme, Inc." + System.currentTimeMillis()
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir = srcDir
            sources {
                include ASCIIDOC_INLINE_EXTENSIONS_FILE
            }
            outputDir = outDir
            extensions {
                postprocessor {
                    document, String output ->
                        if (document.basebackend("html")) {
                            Document doc = Jsoup.parse(output, "UTF-8")

                            Element contentElement = doc.getElementById("footer-text")
                            contentElement.append(copyright)

                            doc.html()
                        } else {
                            throw new IllegalArgumentException("Expected html!")
                        }
                }
            }
        }
        File resultFile = new File(outDir, 'html5' + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
        when:
        task.processAsciidocSources()
        then:
        resultFile.exists()
        resultFile.text.contains(copyright)
        resultFile.text.contains("Inline Extension Test document")
    }

    def "Should fail if inline Postprocessor fails"() {
        given:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir = srcDir
            sources {
                include ASCIIDOC_INLINE_EXTENSIONS_FILE
            }
            outputDir = outDir
            extensions {
                postprocessor {
                    document, output ->
                        if (output.contains("blacklisted")) {
                            throw new IllegalArgumentException("Document contains a blacklisted word")
                        }
                }
            }
        }
        when:
        task.processAsciidocSources()
        then:
        thrown(GradleException)
    }

    def "Should apply inline Preprocessor"() {
        given:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir = srcDir
            sources {
                include ASCIIDOC_INLINE_EXTENSIONS_FILE
            }
            outputDir = outDir
            extensions {
                preprocessor {
                    document, reader ->
                        reader.advance()
                        reader
                }
            }
        }
        File resultFile = new File(outDir, 'html5' + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
        when:
        task.processAsciidocSources()
        then:
        resultFile.exists()
        !resultFile.text.contains("Inline Extension Test document")
    }

    def "Should apply inline Includeprocessor"() {
        given:
        String content = "The content of the URL " + System.currentTimeMillis()
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir = srcDir
            sources {
                include ASCIIDOC_INLINE_EXTENSIONS_FILE
            }
            outputDir = outDir
            extensions {
                include_processor(filter: { it.startsWith('http') }) {
                    document, reader, target, attributes ->
                        reader.push_include(content, target, target, 1, attributes)
                }
            }
        }
        File resultFile = new File(outDir, 'html5' + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)

        when:
        task.processAsciidocSources()

        then:
        resultFile.exists()
        resultFile.text.contains(content)
    }
}
