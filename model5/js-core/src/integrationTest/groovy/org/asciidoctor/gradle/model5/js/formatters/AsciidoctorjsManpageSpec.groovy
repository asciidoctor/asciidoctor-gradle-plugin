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
package org.asciidoctor.gradle.model5.js.formatters

import org.asciidoctor.gradle.model5.js.internal.formatters.DefaultAsciidoctorjsManpage
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification
import spock.lang.Ignore

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.js.plugins.AsciidoctorjsPlugin.DEFAULT_TOOLCHAIN
import static org.asciidoctor.gradle.model5.js.plugins.AsciidoctorjsPlugin.PLUGIN_ID
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Ignore("Not sure if manpage.js needs to be supported.")
class AsciidoctorjsManpageSpec extends IntegrationSpecification {

    void 'Manpage formatter will convert files and not copy resources'() {
        setup:
        final taskName = 'asciidoctorManpage'
        final outputDir = new File(buildDir, 'docs/asciidoc/manpage')

        writeBuildFile()
        copyTestProject('resources')

        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        resources {
            include 'images/**'
        }
        """.stripIndent())

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()
//        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName]).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS

        and: 'Content exists'
        fileExists(outputDir, 'simple.1')
        !fileExists(outputDir, 'images/fake11.txt')
        !fileExists(outputDir, 'images/fake12.txt')
    }

    void writeBuildFile() {
        writeBasicBuildFileGroovy([PLUGIN_ID])
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjsManpage.DEFAULT_NAME, DEFAULT_PUBLICATION)
    }
}
