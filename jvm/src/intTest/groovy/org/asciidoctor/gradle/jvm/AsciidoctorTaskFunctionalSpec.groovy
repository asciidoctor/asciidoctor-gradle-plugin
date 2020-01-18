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
package org.asciidoctor.gradle.jvm

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Issue
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

    @Issue('https://github.com/gradle/gradle/issues/3698')
    @Unroll
    @Timeout(value = 90)
    @SuppressWarnings('LineLength')
    void 'Built-in backends (parallelMode=#parallelMode, asciidoctorj=#asciidoctorjVer, min jRuby=#jrubyVer, compatible=#compatible)'() {
        given:
        getBuildFile("""
            asciidoctor {

                ${defaultProcessModeForAppveyor}

                outputOptions {
                    backends 'html5', 'docbook'
                }

                asciidoctorj {
                    version = '${asciidoctorjVer}'
                    jrubyVersion = '${jrubyVer}'
                }
                logDocuments = true
                sourceDir 'src/docs/asciidoc'
                parallelMode ${parallelMode}

                doFirst {
                    logger.lifecycle 'Requested: asciidoctorj=${asciidoctorjVer}, ' +
                          'jrubyVer=${jrubyVer}. Got configuration: ' +
                          asciidoctorj.configuration.files*.name.join(' ')
                }
            }
        """)

        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)

        when:
        compatible ? runner.build() : runner.buildAndFail()

        then: 'u content is generated as HTML and XML'
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/html5/sample.html').exists() || !compatible
            new File(testProjectDir.root, 'build/docs/asciidoc/html5/subdir/sample2.html').exists() || !compatible
            new File(testProjectDir.root, 'build/docs/asciidoc/docbook/sample.xml').exists() || !compatible
            !new File(testProjectDir.root, 'build/docs/asciidoc/docinfo/docinfo.xml').exists() || !compatible
            !new File(testProjectDir.root, 'build/docs/asciidoc/docinfo/sample-docinfo.xml').exists() || !compatible
            !new File(testProjectDir.root, 'build/docs/asciidoc/html5/subdir/_include.html').exists() || !compatible
        }

        where:
        parallelMode | jrubyVer              | asciidoctorjVer | compatible
        true         | AJ20_ABSOLUTE_MINIMUM | SERIES_20       | true
        true         | AJ20_ABSOLUTE_MINIMUM | SERIES_20       | true
        true         | AJ20_SAFE_MINIMUM     | SERIES_20       | true
        true         | AJ20_SAFE_MINIMUM     | SERIES_20       | true
        true         | AJ20_SAFE_MAXIMUM     | SERIES_20       | true
        true         | AJ20_SAFE_MAXIMUM     | SERIES_20       | true
        false        | AJ20_ABSOLUTE_MINIMUM | SERIES_20       | true
        false        | AJ20_SAFE_MINIMUM     | SERIES_20       | true
        false        | AJ20_SAFE_MAXIMUM     | SERIES_20       | true
        false        | AJ20_SAFE_MAXIMUM     | SERIES_20       | true
        true         | AJ20_ABSOLUTE_MAXIMUM | SERIES_20       | true
        false        | AJ20_ABSOLUTE_MAXIMUM | SERIES_20       | true
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
                ${defaultProcessModeForAppveyor}

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

    void 'Can run in JAVA_EXEC process mode (Groovy DSL)'() {
        given:
        getBuildFile('''
            asciidoctorj {
                logLevel = 'INFO'
            }

            asciidoctor {
                inProcess = JAVA_EXEC

                outputOptions {
                    backends 'html5'
                }
                sourceDir 'src/docs/asciidoc'
            }
        ''')

        when:
        getGradleRunner(DEFAULT_ARGS).build()

        then:
        noExceptionThrown()
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
        String sample2 = new File(testProjectDir.root, 'build/docs/asciidoc/subdir/sample2.html').text

        then:
        sample2.contains('gradle-relative-srcdir = [..]')
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

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/2324')
    @SuppressWarnings('LineLength')
    void 'Run conversion with an unknown backend using JAVA_EXEC'() {
        given:
        getBuildFile('''
        asciidoctor {
            inProcess = JAVA_EXEC
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
        !result.contains('ArrayIndexOutOfBoundsException')
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/368')
    void 'Docinfo files are processed'() {
        given:
        getBuildFile( '''
        asciidoctor {
            attributes docinfo: 'shared'
            baseDirFollowsSourceDir()
        }
        ''')

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        new File(testProjectDir.root, 'build/docs/asciidoc/sample.html').text
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