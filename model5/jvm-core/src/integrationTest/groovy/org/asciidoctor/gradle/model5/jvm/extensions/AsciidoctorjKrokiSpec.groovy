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
package org.asciidoctor.gradle.model5.jvm.extensions


import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjHtml5
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification
import spock.lang.IgnoreIf
import spock.lang.Unroll

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AsciidoctorjKrokiSpec extends IntegrationSpecification {

    @Unroll
    @IgnoreIf(reason = 'Gradle is offline', value = { IS_OFFLINE })
    void 'Can generate images with #useProject'() {
        setup:
        final taskName = 'asciidoctorHtml'
        final outputFile = new File(buildDir, "docs/asciidoc/html/${useProject}.html")

        writeBuildFile()
        copyTestProject(useProject)

        when:
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName, '-s']).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS
        outputFile.text.contains("<img src=\"https://kroki.io/${useProject}")

        where:
        useProject << ['ditaa', 'plantuml', 'mermaid']
    }

    @IgnoreIf(reason = 'Gradle is offline', value = { IS_OFFLINE })
    void 'Can download images for #useProject'() {
        setup:
        final taskName = 'asciidoctorHtml'
        final outputDir = new File(buildDir, 'docs/asciidoc/html')

        writeBuildFile()
        copyTestProject(useProject)
        buildFile << """
        asciidoc.toolchains.asciidoctorj.asciidocExtensions {
            kroki(AsciidoctorjKrokiExtension) {
                fetchDiagrams = true
            }
        }
        """.stripIndent()
        when:
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName, '-s']).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS
        outputDir.list().count { it =~ imagePattern } == 1

        where:
        useProject | imagePattern
        'ditaa'    | ~/^diag-\w+\.svg$/
        'plantuml' | ~/^diag-\w+\.svg$/
    }

    private void writeBuildFile() {
        writeBasicBuildFileGroovy(
            ['org.asciidoctor.jvm', 'org.asciidoctor.jvm.kroki'],
            [
                'org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain',
                'org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjKrokiExtension'
            ]
        )
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjHtml5.DEFAULT_NAME, DEFAULT_PUBLICATION)

        buildFile << '''
        repositories {
            ruby.gems()
        }
        '''.stripIndent()
    }
}