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
package org.asciidoctor.gradle.compat

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

/**
 * Asciidoctor task inline extensionRegistry specification
 *
 * @author Robert Panzer
 * @author Schalk W. Cronjé
 */
class ExtensionsFunctionalSpec extends FunctionalSpecification {

    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidocextensions'
    private static final String ASCIIDOC_MACRO_EXTENSION_SCRIPT = 'blockMacro.groovy'
    private static final String ASCIIDOC_INLINE_EXTENSIONS_FILE = 'inlineextensions.asciidoc'
    private static final String ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE = 'inlineextensions.html'

    File resultFile

    @SuppressWarnings('LineLength')
    void setup() {
        createTestProject('extensions')
        resultFile = new File(testProjectDir.root, "${ASCIIDOC_BUILD_DIR}/html5/${ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE}")
    }

    @Unroll
    void 'Compat Task: Should apply #description'() {
        given:
        getBuildFile(script)

        when:
        getGradleRunner(['asciidoctor', '-i']).build()

        then:
        resultFile.exists()
        resultFile.text.contains(content[0])
        content.size() == 1 || resultFile.text.contains(content[1])

        where:
        description << [
            'inline BlockProcessor',
            'BlockProcessor from file',
            'inline PostProcessor',
            'inline IncludeProcessor'
        ]

        and:
        script << [
            """{
                    block(name: 'BIG', contexts: [':paragraph']) {
                        parent, reader, attributes ->
                        def upperLines = reader.readLines()*.toUpperCase()
                        .inject('') {a, b -> a + '\\n' + b}

                        createBlock(parent, 'paragraph', [upperLines], attributes, [:])
                    }
                    block('small') {
                        parent, reader, attributes ->
                        def lowerLines = reader.readLines()*.toLowerCase()
                        .inject('') {a, b -> a + '\\n' + b}

                        createBlock(parent, 'paragraph', [lowerLines], attributes, [:])
                    }

                }
            """,
            "file('src/docs/asciidoc/${ASCIIDOC_MACRO_EXTENSION_SCRIPT}')",
            '''{
                postprocessor { document, String output ->
                    if (document.basebackend("html")) {
                        output + '<!-- Copyright Acme, Inc. -->\'
                    } else {
                        throw new IllegalArgumentException("Expected html!")
                    }
                }
            }
            ''',
            '''{
                include_processor(filter: { it.startsWith('http') }) {
                    document, reader, target, attributes ->
                        reader.push_include('Content of URL', target, target, 1, attributes)
                }
            }
            '''
        ]

        and:
        content << [
            ['WRITE THIS IN UPPERCASE', 'and write this in lowercase'],
            ['WRITE THIS IN UPPERCASE', 'and write this in lowercase'],
            ['Copyright Acme, Inc.', 'Inline Extension Test document'],
            ['Content of URL']
        ]
    }

    void 'Compat Task: Should apply inline Preprocessor'() {
        given:
        getBuildFile('''{
                        preprocessor {
                            document, reader ->
                                reader.advance()
                                reader
                        }
                    }
        ''')

        when:
        gradleRunner.build()

        then:
        resultFile.exists()
        !resultFile.text.contains('Inline Extension Test document')
    }

    void 'Compat Task: Should fail if inline PostProcessor fails'() {
        given:
        getBuildFile('''{
                postprocessor {
                    document, output ->
                        if (output.contains("blacklisted")) {
                            throw new IllegalArgumentException("Document contains a blacklisted word")
                        }
                }
            }
        ''')

        when:
        BuildResult result = gradleRunner.buildAndFail()

        then:
        result.output.contains('Document contains a blacklisted word')
    }

    File getBuildFile(final String extension) {
        getJvmConvertGroovyBuildFile(
            """
                asciidoctor {
                    sourceDir = file('src/docs/asciidoc')
                    sources {
                        include '${ASCIIDOC_INLINE_EXTENSIONS_FILE}'
                    }
                    outputDir = file('${ASCIIDOC_BUILD_DIR}')
                    extensions ${extension}
                }
            """,
            'org.asciidoctor.convert'
        )
    }
}
