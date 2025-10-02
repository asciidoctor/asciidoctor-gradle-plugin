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
// tag::hacking-asciidoctorjs-output-formatter[]
package org.asciidoctor.gradle.model5.js.formatters

import org.asciidoctor.gradle.model5.js.internal.formatters.DefaultAsciidoctorjsRevealjs
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification
import spock.lang.PendingFeatureIf

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.js.plugins.AsciidoctorjsPlugin.OPAL_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AsciidoctorjsRevealjsSpec extends IntegrationSpecification {

    @PendingFeatureIf(reason = 'Not yet supported on Windows', value = { IS_WINDOWS })
    void 'Reveal.js formatter will convert files and copy resources'() {
        setup:
        final taskName = 'asciidoctorRevealjs' // <.>
        final outputDir = new File(buildDir, 'docs/asciidoc/revealjs') // <.>

        writeBuildFile()
        copyTestProject('resources')

        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        resources {
            include 'images/**'
        }
        """.stripIndent())

        when:
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName, '-s']).run()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS

        and: 'Content exists'
        fileExists(outputDir, 'simple.html') // <.>
        fileExists(outputDir, 'images/fake11.txt') // <.>
    }

    void writeBuildFile() {
        writeBasicBuildFileGroovy(['org.asciidoctor.js.revealjs']) // <.>
        addOutputToSourceSetGroovy(OPAL_TOOLCHAIN, DefaultAsciidoctorjsRevealjs.DEFAULT_NAME, DEFAULT_PUBLICATION)
        // <.>
    }
}
// end::hacking-asciidoctorjs-output-formatter[]
