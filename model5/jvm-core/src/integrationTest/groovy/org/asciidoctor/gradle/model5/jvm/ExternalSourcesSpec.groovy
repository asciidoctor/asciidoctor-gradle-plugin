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
package org.asciidoctor.gradle.model5.jvm

import org.asciidoctor.gradle.model5.jvm.testfixtures.AsciidoctorjHtmlIntegrationSpecification
import org.gradle.testkit.runner.TaskOutcome

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class ExternalSourcesSpec extends AsciidoctorjHtmlIntegrationSpecification {
    File otherSourceDir

    void setup() {
        writeHtmlBasedBuildFile()
        copyTestProject('normal')
        otherSourceDir = new File(TEST_PROJECTS_DIR, 'resources/src/docs/asciidoc').absoluteFile
    }

    void 'Can handle external directories as sources'() {
        setup:
        final subdir = 'someSubdir'
        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        externalSource {
            sourceDir = '${getEscapedPathString(otherSourceDir.absolutePath)}'
            sources {
                include 'simple.adoc'
            }
            resources {
                include 'images/fake12.txt'
            }
            into '${subdir}'
        }
        """.stripIndent())

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName, '-s']).build()

        then:
        result.task(":${taskName}").outcome == SUCCESS
        fileExists(outputDir, 'sample.html')
        fileExists(outputDir, 'subdir/sample2.html')
        fileExists(outputDir, "${subdir}/simple.html")
        fileExists(outputDir, "${subdir}/images/fake12.txt")
        !fileExists(outputDir, 'images/fake.txt')
    }
}