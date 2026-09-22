/*
 * Copyright 2013 - 2026 the original author or authors.
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
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Timeout
import spock.lang.Unroll

import static org.asciidoctor.gradle.testfixtures.AsciidoctorjTestVersions.SERIES_20
@java.lang.SuppressWarnings('NoWildcardImports')
import static org.asciidoctor.gradle.testfixtures.JRubyTestVersions.*

class AsciidoctorTaskFunctionalSpec extends FunctionalSpecification {

    static final List<String> DEFAULT_ARGS = ['asciidoctor', '-s', '-i']

    void setup() {
        createTestProject()
    }

    @Timeout(value = 90)
    void 'Support attributes in various formats'() {
        given:
        getBuildFile("""
            asciidoctorj {
                attributes attr1 : 'a string',
                    attr2 : "A GString",
                    attr10 : [ 'a', 2, 5 ]

                attributeProvider {
                    [ attr50 : 'value' ]
                }
            }

            asciidoctor {
                outputOptions {
                    backends 'html5'
                }
                logDocuments = true
                sourceDir 'src/docs/asciidoc'

                asciidoctorj {
                    attributes attr3 : new File('abc'),
                        attr4 : { 'a closure' },
                        attr20 : [ a : 1 ],
                        attrProvider : providers.provider( { 'a string provider' } )
                }
            }
        """)
        when:
        getGradleRunner(DEFAULT_ARGS).build()

        then:
        noExceptionThrown()
    }

    @Unroll
    void "Can run in #processMode process mode (Groovy DSL)"() {
        given:
        getBuildFile("""
            asciidoctorj {
                logLevel = 'INFO'
            }

            asciidoctor {
                executionMode = ${processMode}

                outputOptions {
                    backends 'html5'
                }
                sourceDir 'src/docs/asciidoc'
            }
        """)

        when:
        getGradleRunner(DEFAULT_ARGS).build()

        then:
        noExceptionThrown()

        where:
        processMode << ['IN_PROCESS', 'OUT_OF_PROCESS'/*, 'JAVA_EXEC'*/]
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/292')
    void 'Special gradle attributes are adapted per document'() {
        given:
        getBuildFile('''
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
            }
        ''')

        when:
        getGradleRunner(DEFAULT_ARGS).build()
        String sample2 = new File(buildDir, 'docs/asciidoc/subdir/sample2.html').text

        then:
        sample2.contains('gradle-relative-srcdir = [subdir]')
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/2324')
    @SuppressWarnings('LineLength')
    void 'Run conversion with an unknown backend'() {
        given:
        getBuildFile('''
        asciidoctor {
            outputOptions {
                backends = ['html5', 'abc', 'xyz']
            }
            sourceDir 'src/docs/asciidoc'
        }
        ''')

        when:
        String result = getGradleRunner(DEFAULT_ARGS).buildAndFail().output

        then:
        result.contains("missing converter for backend 'abc'. Processing aborted")
        result.contains('org.asciidoctor.jruby.internal.AsciidoctorCoreException: org.jruby.exceptions.NotImplementedError')
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/368')
    void 'Docinfo files are processed'() {
        given:
        getBuildFile('''
        asciidoctor {
            attributes docinfo: 'shared'
            baseDirFollowsSourceDir()
        }
        ''')

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        new File(buildDir, 'docs/asciidoc/sample.html').text
                .contains('<meta name="asciidoctor-docinfo-test"/>')
    }

    File getBuildFile(String extraContent) {
        getJvmConvertGroovyBuildFile("""
asciidoctorj {
    jrubyVersion = '${AJ20_SAFE_MAXIMUM}'
}

${extraContent}
""")
    }
}