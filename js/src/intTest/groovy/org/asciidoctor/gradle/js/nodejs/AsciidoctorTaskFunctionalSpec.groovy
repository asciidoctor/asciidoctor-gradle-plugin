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
package org.asciidoctor.gradle.js.nodejs

import org.asciidoctor.gradle.js.nodejs.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.generators.AsciidoctorjsVersionGenerator
import spock.lang.Unroll

@SuppressWarnings('MethodName')
class AsciidoctorTaskFunctionalSpec extends FunctionalSpecification {

    static final List<String> DEFAULT_ARGS = ['asciidoctor', '-s', '-i']

    void setup() {
        createTestProject()
    }

    void 'Can run in subprocess build mode (Groovy DSL)'() {
        given:
        getBuildFile('''
            asciidoctor {
                outputOptions {
                    backends 'html5'
                }
                sourceDir 'src/docs/asciidoc'
            }
        ''')

        when:
        getGroovyGradleRunner(DEFAULT_ARGS).build()

        then:
        new File(testProjectDir.root, 'build/docs/asciidoc/sample.html').exists()
        new File(testProjectDir.root, 'build/docs/asciidoc/subdir/sample2.html').exists()
    }

    @Unroll
    @SuppressWarnings('LineLength')
    void 'Built-in HTML backend + added DOCBOOK backend (asciidoctor.js=#versions.version, docbook=#versions.docbookVersion)'() {
        given:
        getBuildFile("""
            asciidoctor {

                outputOptions {
                    backends 'html5', 'docbook'
                }

                asciidoctorjs {
                    version = '${versions.version}'

                    modules {
                        docbook.version = '${versions.docbookVersion}'
                    }
                }

                logDocuments = true
                sourceDir 'src/docs/asciidoc'
            }
        """)

        when:
        getGroovyGradleRunner(DEFAULT_ARGS).build()

        then: 'content is generated as HTML and XML'
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/html5/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/html5/subdir/sample2.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/docbook/sample.xml').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/docinfo/docinfo.xml').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/docinfo/sample-docinfo.xml').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/html5/subdir/_include.html').exists()
        }

        where:
        versions << AsciidoctorjsVersionGenerator.random
    }

    void 'Use default DOCBOOK version backend and global extension (asciidoctor.js=#version)'() {
        given:
        getBuildFile("""
            asciidoctorjs {
                version = '${version}'

                modules {
                    docbook.use()
                }
            }

            asciidoctor {

                outputOptions {
                    backends 'docbook'
                }

                logDocuments = true
                sourceDir 'src/docs/asciidoc'
            }
        """)

        when:
        getGroovyGradleRunner(DEFAULT_ARGS).build()

        then: 'content is generated as XML'
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/sample.xml').exists()
        }

        where:
        version = AsciidoctorjsVersionGenerator.random.first().version
    }

    File getBuildFile(String extraContent) {
        getJsConvertGroovyBuildFile("""

${extraContent}
""")
    }
}