/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.jvm.extensions

import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjHtml5
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification
import spock.lang.Unroll

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN

class AsciidoctorjGroovyDslSpec extends IntegrationSpecification {

    String taskName = 'asciidoctorHtml'
    String taskNameDocBook = 'asciidoctorDocbook'

    void setup() {
        copyTestProject('extensions')
    }

    @Unroll
    void 'Extension can be applied from a #method'() {
        setup:
        writeBuildFile(inFile)
        final resultFile = new File(buildDir, "docs/asciidoc/html/inlineextensions.html")

        when:
        getGradleRunner(IS_GROOVY_DSL, [taskName, '-s']).build()

        then:
        resultFile.text.contains('WRITE THIS IN UPPERCASE')
        resultFile.text.contains('and write this in lowercase')

        where:
        method   | inFile
        'string' | false
        'file'   | true
    }

    void 'Fail build if extension fails to compile'() {
        setup:
        writeBuildFile(false)

        buildFile << """
        asciidoc {
            toolchains {
                asciidoctorj {
                    asciidocExtensions {
                        groovydsl(AsciidoctorjGroovyDslExtension) {
                            fromString ('''${FAILING_EXTENSION}''')
                        }
                    }
                }
            }
        }
        """.stripIndent()

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [taskName, '-s']).buildAndFail()

        then:
        result1.output.contains('Document contains a blacklisted word')
    }

    private void writeBuildFile(boolean extensionInFile) {
        if (extensionInFile) {
            final dest = new File(projectDir, SCRIPT_FILE_PATH)
            dest.parentFile.mkdirs()
            dest.text = GROOVY_EXTENSION
        }

        writeBasicBuildFileGroovy(
            ['org.asciidoctor.jvm'],
            [
                'org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain',
                'org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjGroovyDslExtension'
            ]
        )
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjHtml5.DEFAULT_NAME, DEFAULT_PUBLICATION)

        buildFile << """
        asciidoc {
            toolchains {
                asciidoctorj {
                    asciidocExtensions {
                        groovydsl(AsciidoctorjGroovyDslExtension) {
                            ${groovyDslConfig(extensionInFile)}
                        }
                    }
                }
            }
        }
        """.stripIndent()
    }

    private String groovyDslConfig(boolean extensionInFile) {
        if (extensionInFile) {
            "fromFile('${SCRIPT_FILE_PATH}')"

        } else {
            """fromString('''${GROOVY_EXTENSION}''')
            """.stripIndent()
        }
    }

    private static final String SCRIPT_FILE_PATH = 'src/docs/asciidoc/extension.groovy'
    private static final String GROOVY_EXTENSION = '''
    block(name: 'BIG', contexts: [':paragraph']) {
            parent, reader, attributes ->
            def upperLines = reader.readLines()
            .collect {it.toUpperCase()}
            .inject('') {a, b -> "\${a}\\\n\${b}"}
    
            createBlock(parent, "paragraph", [upperLines], attributes, [:])
    }
    block('small') {
            parent, reader, attributes ->
            def lowerLines = reader.readLines()
            .collect {it.toLowerCase()}
            .inject('') {a, b -> "\${a}\\\n\${b}"}
    
            createBlock(parent, 'paragraph', [lowerLines], attributes, [:])
    }
    '''.stripIndent()

    private static final String FAILING_EXTENSION = '''
    postprocessor {
        document, output ->
            if (output.contains("blacklisted")) {
                throw new IllegalArgumentException("Document contains a blacklisted word")
            }
        }
    }
    '''.stripIndent()
}
