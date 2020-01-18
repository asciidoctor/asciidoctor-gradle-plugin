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
import spock.lang.Issue

@java.lang.SuppressWarnings('NoWildcardImports')
import static org.asciidoctor.gradle.testfixtures.JRubyTestVersions.*

@Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/392')
class MultiLanguageFunctionalSpec extends FunctionalSpecification {

    static final List<String> DEFAULT_ARGS = ['asciidoctor', '-s', '-i']

    void setup() {
        createTestProject('multilang')
    }

    void 'Multi-language'() {
        given:
        getBuildFile("""
        asciidoctor {
            ${defaultProcessModeForAppveyor}
    
            outputOptions {
                backends 'html5'
            }

            languages 'en', 'es'

            sourceDir 'src/docs/asciidoc'

            sources {
                include 'sample.asciidoc'
            }
        }
        """)

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/en/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/en/images/fake.txt').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/images/fake.txt').exists()
        }
    }

    void 'Multi-language, multi-backends'() {
        given:
        getBuildFile("""
        asciidoctor {
            ${defaultProcessModeForAppveyor}
    
            outputOptions {
                backends 'html5', 'docbook'
            }

            languages 'en', 'es'

            sourceDir 'src/docs/asciidoc'
        }
        """)

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/en/html5/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/html5/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/en/html5/images/fake.txt').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/html5/images/fake.txt').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/en/docbook/sample.xml').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/docbook/sample.xml').exists()
        }
    }

    void 'Multi-language, multi-backends, intermediate workdir'() {
        given:
        getBuildFile("""
        asciidoctor {
            ${defaultProcessModeForAppveyor}
    
            outputOptions {
                backends 'html5', 'docbook'
            }

            languages 'en', 'es'
            sourceDir 'src/docs/asciidoc'
            useIntermediateWorkDir()
        }
        """)

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/en/html5/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/html5/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/en/docbook/sample.xml').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/docbook/sample.xml').exists()
        }
    }

    void 'Multi-language, docinfo'() {
        given:
        getBuildFile("""
        asciidoctor {
            ${defaultProcessModeForAppveyor}
    
            outputOptions {
                backends 'html5'
            }

            languages 'en', 'es'

            sourceDir 'src/docs/asciidoc'

            attributes docinfo: 'shared'
            baseDirFollowsSourceDir()
        }
        """)

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/en/sample.html').text
                .contains('<meta name="asciidoctor-docinfo-test"/>')
            new File(testProjectDir.root, 'build/docs/asciidoc/es/sample.html').text
                .contains('<meta name="asciidoctor-docinfo-test"/>')
        }
    }

    void 'Multi-language, docinfo, intermediate workdir'() {
        given:
        getBuildFile("""
        asciidoctor {
            ${defaultProcessModeForAppveyor}
    
            outputOptions {
                backends 'html5'
            }

            languages 'en', 'es'

            sourceDir 'src/docs/asciidoc'

            attributes docinfo: 'shared'
            baseDirFollowsSourceDir()
            useIntermediateWorkDir()
        }
        """)

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/en/sample.html').text
                .contains('<meta name="asciidoctor-docinfo-test"/>')
            new File(testProjectDir.root, 'build/docs/asciidoc/es/sample.html').text
                .contains('<meta name="asciidoctor-docinfo-test"/>')
        }
    }

    void 'Multi-language, multiple resources'() {
        given:
        getBuildFile("""
        asciidoctor {
            ${defaultProcessModeForAppveyor}
    
            outputOptions {
                backends 'html5'
            }

            languages 'en', 'es'

            sourceDir 'src/docs/asciidoc'

            resources 'en', {
                from 'src/resources/en'
                into 'foo'
            }

            resources 'es', {
                from 'src/resources/es'
                into 'foo'
            }

            resources {
                from 'src/resources/common'
                into 'foo'
            }
        }
        """)

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/en/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/sample.html').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/en/foo/fake-en.txt').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/en/foo/fake-common.txt').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/foo/fake-es.txt').exists()
            new File(testProjectDir.root, 'build/docs/asciidoc/es/foo/fake-common.txt').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/en/images/fake.txt').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/es/images/fake.txt').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/en/foo/fake-es.txt').exists()
            !new File(testProjectDir.root, 'build/docs/asciidoc/es/foo/fake-en.txt').exists()
        }
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