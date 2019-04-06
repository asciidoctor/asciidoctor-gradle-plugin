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
package org.asciidoctor.gradle.js

import org.asciidoctor.gradle.js.internal.FunctionalSpecification

import static org.asciidoctor.gradle.testfixtures.AsciidoctorjsTestVersions.SERIES_20

@SuppressWarnings('MethodName')
class AsciidoctorTaskFunctionalSpec extends FunctionalSpecification {

    static final List<String> DEFAULT_ARGS = ['asciidoctor', '-s', '-i']

    void setup() {
        createTestProject()
    }

    void 'Built-in backends (asciidoctor.js=#asciidoctorjsVer)'() {
        given:
        getBuildFile("""
            asciidoctor {

                outputOptions {
                    backends 'html5', 'docbook'
                }

                asciidoctorjs {
                    version = '${asciidoctorjsVer}'
                }

                logDocuments = true
                sourceDir 'src/docs/asciidoc'
            }
        """)

        when:
        getGradleRunner(DEFAULT_ARGS).build()

        then: 'u content is generated as HTML and XML'
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/html5/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/html5/subdir/sample2.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/docbook/sample.xml').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/docinfo/docinfo.xml').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/docinfo/sample-docinfo.xml').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/html5/subdir/_include.html').exists()
        }

        where:
        asciidoctorjsVer << [SERIES_20]
    }
//
//    @Timeout(value = 90)
//    void 'Support attributes in various formats'() {
//        given:
//        getBuildFile("""
//            asciidoctorj {
//                attributes attr1 : 'a string',
//                    attr2 : "A GString",
//                    attr10 : [ 'a', 2, 5 ]
//
//                attributeProvider {
//                    [ attr50 : 'value' ]
//                }
//            }
//
//            asciidoctor {
//                ${defaultProcessModeForAppveyor}
//
//                outputOptions {
//                    backends 'html5'
//                }
//                logDocuments = true
//                sourceDir 'src/docs/asciidoc'
//
//                asciidoctorj {
//                    attributes attr3 : new File('abc'),
//                        attr4 : { 'a closure' },
//                        attr20 : [ a : 1 ],
//                        attrProvider : providers.provider( { 'a string provider' } )
//                }
//            }
//        """)
//        when:
//        getGradleRunner(DEFAULT_ARGS).build()
//
//        then:
//        noExceptionThrown()
//    }
//
    void 'Can run in subprocess build mode (Groovy DSL)'() {
        given:
        getBuildFile('''
            asciidoctorjs {
//                logLevel = 'INFO'
            }

            asciidoctor {

                outputOptions {
                    backends 'html5'
                }
                sourceDir 'src/docs/asciidoc'
            }
        ''')

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        new File(testProjectDir.root, "build/docs/asciidoc/sample.html").exists()
        new File(testProjectDir.root, "build/docs/asciidoc/subdir/sample2.html").exists()
    }

    File getBuildFile(String extraContent) {
        getJsConvertGroovyBuildFile("""


${extraContent}
""")
    }
}