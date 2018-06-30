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
package org.asciidoctor.gradle.jvm

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Issue
import spock.lang.Timeout
import spock.lang.Unroll

import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.SERIES_15
import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.SERIES_16
@java.lang.SuppressWarnings('NoWildcardImports')
import static org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions.*

@SuppressWarnings('MethodName')
class AsciidoctorTaskFunctionalSpec extends FunctionalSpecification {

    static final List<String> DEFAULT_ARGS = ['asciidoctor', '-s', '-i']

    void setup() {
        createTestProject()
    }

    @Issue('https://github.com/gradle/gradle/issues/3698')
    @Unroll
    @Timeout(value = 90)
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
                    logger.lifecycle 'Requested: asciidoctorj=${asciidoctorjVer}, jrubyVer=${jrubyVer}. Got configuration: ' + asciidoctorj.configuration.files*.name.join(' ')
                }
            }
        """)

        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)

        when:
        compatible ? runner.build() : runner.buildAndFail()

        then: 'u content is generated as HTML and XML'
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/html5/sample.html').exists() || !compatible
            new File(testProjectDir.root, 'build/docs/asciidoc/docbook/sample.xml').exists() || !compatible
            !new File(testProjectDir.root, 'build/docs/asciidoc/docinfo/docinfo.xml').exists() || !compatible
            !new File(testProjectDir.root, 'build/docs/asciidoc/docinfo/sample-docinfo.xml').exists() || !compatible
        }

        where:
        parallelMode | jrubyVer              | asciidoctorjVer | compatible
//        true         | AJ15_ABSOLUTE_MINIMUM | SERIES_15       | true  // <- too brittle
        true         | AJ16_ABSOLUTE_MINIMUM | SERIES_16       | true
        true         | AJ15_SAFE_MINIMUM     | SERIES_15       | true
        true         | AJ16_SAFE_MINIMUM     | SERIES_16       | true
        true         | AJ15_SAFE_MAXIMUM     | SERIES_15       | true
        true         | AJ16_SAFE_MAXIMUM     | SERIES_16       | true
        false        | AJ15_ABSOLUTE_MINIMUM | SERIES_15       | true
        false        | AJ15_SAFE_MINIMUM     | SERIES_15       | true
        false        | AJ15_SAFE_MAXIMUM     | SERIES_15       | true
        false        | AJ16_SAFE_MAXIMUM     | SERIES_16       | true
        true         | AJ15_ABSOLUTE_MAXIMUM | SERIES_15       | false
        false        | AJ16_ABSOLUTE_MAXIMUM | SERIES_16       | true
    }

    @Timeout(value = 90)
    void 'Support attributes in various formats'() {
        given:
        getBuildFile("""
            asciidoctorj {
                attributes attr1 : 'a string',
                    attr2 : "A GString",
                    attr10 : [ 'a', 2, 5 ]
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
                        attr20 : [ a : 1 ]            
                }
            }
        """)
        when:
        getGradleRunner(DEFAULT_ARGS).build()

        then:
        noExceptionThrown()
    }

    void 'Can run in JAVA_EXEC process mode'() {
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

    File getBuildFile(String extraContent) {
        getJvmConvertBuildFile("""
asciidoctorj {
    jrubyVersion = '${AJ15_SAFE_MAXIMUM}'
}

${extraContent}
""")
    }
}