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
package org.asciidoctor.gradle.compat

import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

/**
 * @author Rene Groeschke
 */
class CompatExtensionMultiuseFunctionalSpec extends FunctionalSpecification {

    static final String TEST_PROJECTS_DIR = FunctionalSpecification.TEST_PROJECTS_DIR

    void setup() {
        testProjectDir.root.mkdirs()
    }

    void 'Postprocessor extensions are registered and preserved across multiple builds'() {
        given: 'A build file that declares extensions'
        def buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
        plugins {
            id 'org.asciidoctor.convert'
        }

        asciidoctor {
            extensions {
                postprocessor { document, output ->
                    return 'Hi, Mom' + output
                }
            }

            sources {
                include 'sample.adoc'
            }
        }
        """

        and: 'Some source files'
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, 'extensions'), testProjectDir.root)
        final File buildDir = new File(testProjectDir.root, 'build')

        when:
        final BuildResult firstInvocationResult = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('asciidoctor')
            .withPluginClasspath()
            .withDebug(true)
            .build()

        then:
        firstInvocationResult.task(':asciidoctor').outcome == TaskOutcome.SUCCESS
        File sampleHtmlOutput = new File(buildDir, 'asciidoc/html5/sample.html')
        sampleHtmlOutput.exists()
        sampleHtmlOutput.text.startsWith('Hi, Mom')

        when:
        new File(testProjectDir.root, 'src/docs/asciidoc/sample.asciidoc') << 'changes'
        final BuildResult secondInvocationResult = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('asciidoctor')
            .withPluginClasspath()
            .withDebug(true)
            .build()

        then:
        secondInvocationResult.task(':asciidoctor').outcome == TaskOutcome.SUCCESS
        new File(buildDir, 'asciidoc/html5/sample.html').text.startsWith('Hi, Mom')
    }

}
