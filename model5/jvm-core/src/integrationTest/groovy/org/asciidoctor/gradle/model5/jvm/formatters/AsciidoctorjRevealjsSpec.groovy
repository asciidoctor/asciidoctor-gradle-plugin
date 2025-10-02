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

import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjRevealjs
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AsciidoctorjRevealjsSpec extends IntegrationSpecification {

    void 'Reveal.js formatter will convert files and copy resources'() {
        setup:
        final taskName = 'asciidoctorRevealjs'
        final outputDir = new File(buildDir, 'docs/asciidoc/revealjs')

        writeBuildFile()
        copyTestProject('revealjs')

        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        resources {
            include '*.js'
        }
        """.stripIndent())

        when:
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName,'-s']).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS

        and: 'Content exists'
        fileExists(outputDir, 'revealjs.html')
        fileExists(outputDir, 'subdir/revealjs2.html')
        fileExists(outputDir, 'empty-plugin-configuration.js')
    }

    void writeBuildFile() {
        writeBasicBuildFileGroovy(['org.asciidoctor.jvm.revealjs'])
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjRevealjs.DEFAULT_NAME, DEFAULT_PUBLICATION)
    }
}
