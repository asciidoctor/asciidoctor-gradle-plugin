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
package org.asciidoctor.gradle.jvm.epub

import org.asciidoctor.gradle.jvm.epub.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult
import spock.lang.Issue

class LinkedChaptersFunctionalSpec extends FunctionalSpecification {

    void setup() {
        createTestProject('issue-409-link-regression')
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/408')
    void 'Linked chapters should not produce a warning'() {
        given:
        getBuildFile("""
        
        asciidoctorEpub {
            asciidoctorj {
                fatalWarnings ~/.*invalid reference to (unknown )?anchor.*/
            }
            
            ebookFormats EPUB3

            sources {
                include 'epub3.adoc'
            }
            
            baseDirFollowsSourceDir()
        }
        """)

        when:
        BuildResult result = getGradleRunner(['asciidoctorEpub', '-s', '-i']).build()

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidocEpub/epub3.epub').exists()
            !result.output.contains('invalid reference to anchor')
            !result.output.contains('invalid reference to unknown anchor')
        }
    }

    File getBuildFile(String extraContent) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'org.asciidoctor.jvm.epub'
}

${offlineRepositories}

${extraContent}
"""
        buildFile
    }

}