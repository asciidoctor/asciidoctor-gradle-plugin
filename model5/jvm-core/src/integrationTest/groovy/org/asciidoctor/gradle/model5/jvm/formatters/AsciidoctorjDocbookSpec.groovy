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
package org.asciidoctor.gradle.model5.jvm.formatters

import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjDocbook
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AsciidoctorjDocbookSpec extends IntegrationSpecification {

    void 'Docbook formatter will convert files and copy resources'() {
        setup:
        final taskName = 'asciidoctorDocbook'
        final outputDir = new File(buildDir, 'docs/asciidoc/docbook')

        writeBuildFile()
        copyTestProject('resources')

        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        resources {
            include 'images/**'
        }
        """.stripIndent())

        when:
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName]).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS

        and: 'Content exists'
        fileExists(outputDir, 'simple.xml')
        fileExists(outputDir, 'images/fake11.txt')
        fileExists(outputDir, 'images/fake12.txt')

        and: 'Resources in source dir that were not specified, were not copied'
        !fileExists(outputDir, 'images2/fake2.txt')
    }

    void writeBuildFile() {
        writeBasicBuildFileGroovy(['org.asciidoctor.jvm'])
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjDocbook.DEFAULT_NAME, DEFAULT_PUBLICATION)
    }
}
