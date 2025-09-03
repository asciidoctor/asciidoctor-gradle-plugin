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
// tag::hacking-asciidoctorj-output-formatter[]
package org.asciidoctor.gradle.model5.jvm.formatters

import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjEpub
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AsciidoctorjEpubSpec extends IntegrationSpecification {

    void 'Epub formatter will convert files and not copy resources'() {
        setup:
        final taskName = 'asciidoctorEpub' // <.>
        final outputDir = new File(buildDir, 'docs/asciidoc/epub') // <.>

        writeBuildFile()
        copyTestProject('resources')

        when:
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName, '-s', '-i']).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS

        and: 'Content exists'
        fileExists(outputDir, 'simple.epub') // <.>
        !fileExists(outputDir, 'images/fake11.txt') // <.>
    }

    void writeBuildFile() {
        writeBasicBuildFileGroovy(['org.asciidoctor.jvm.epub']) // <.>
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjEpub.DEFAULT_NAME, DEFAULT_PUBLICATION) // <.>
    }
}
// end::hacking-asciidoctorj-output-formatter[]
