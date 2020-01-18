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
package org.asciidoctor.gradle.jvm.leanpub

import org.asciidoctor.gradle.jvm.leanpub.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.generators.ProcessGenerator
import spock.lang.Unroll

@SuppressWarnings(['DuplicateStringLiteral', 'MethodName', 'DuplicateListLiteral'])
class AsciidoctorLeanpubTaskFunctionalSpec extends FunctionalSpecification {

    void setup() {
        createTestProject()
    }

    @Unroll
    void 'Run a Leanpub generator (#processMode)'() {
        given:
        getBuildFile('')

        when:
        getGradleRunner(['asciidoctorLeanpub', '-s', '-i']).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidocLeanpub/manuscript/Book.txt').exists()
        }

        where:
        processMode << ProcessGenerator.get()
    }

//    File getSingleFormatBuildFile(final String format) {
//        getBuildFile( """
//
//        asciidoctorEpub {
//            sourceDir 'src/docs/asciidoc'
//            ebookFormats ${format}
//
//            kindlegen {
//                agreeToTermsOfUse = true
//            }
//
//            asciidoctorj {
//                jrubyVersion = '${JRUBY_TEST_VERSION}'
//            }
//
//            sources {
//                include 'epub3.adoc'
//            }
//        }
//        """)
//    }

    File getBuildFile(String extraContent, boolean withDropbox = false) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'org.asciidoctor.jvm.leanpub'

    ${withDropbox ? "id 'org.asciidoctor.jvm.leanpub.dropbox-copy'" : ''}
}

${offlineRepositories}

${extraContent}
"""
        buildFile
    }

}