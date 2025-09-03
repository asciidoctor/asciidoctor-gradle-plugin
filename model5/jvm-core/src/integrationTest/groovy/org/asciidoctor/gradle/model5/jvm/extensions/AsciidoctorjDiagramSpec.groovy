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
package org.asciidoctor.gradle.model5.jvm.extensions

import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.jvm.internal.extensions.DefaultAsciidoctorjDiagram
import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjHtml5
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification
import spock.lang.Unroll

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.CACHE_SUBDIR_BASE
import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.internal.extensions.DefaultAsciidoctorjDiagram.DEFAULT_NAME
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AsciidoctorjDiagramSpec extends IntegrationSpecification {

    @Unroll
    void 'Can generate images with #useProject'() {
        setup:
        final taskName = 'asciidoctorHtml'
        final outputDir = new File(buildDir, 'docs/asciidoc/html')

        writeBuildFile()
        copyTestProject(useProject)
        activateExtension(activator)

        when:
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName]).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS
        outputDir.list().count { it =~ imagePattern } == imageCount

        and: 'The cache directory is in the correct location'
        new File(buildDir,"${CACHE_SUBDIR_BASE}/${DEFAULT_TOOLCHAIN}-${DEFAULT_NAME}").exists()

        where:
        useProject | activator     | imagePattern                     | imageCount
        'ditaa'    | 'useDitaa'    | ~/^diag-ditaa-.+\.png$/          | 1
        'plantuml' | 'usePlantUml' | ~/^plantuml-example\.svg$/       | 1
        'diagram'  | 'useDiagram'  | ~/^(testd2.svg|testqrcode.png)$/ | 1
//        'diagram'  | 'useDiagram'  | ~/^(testd2.svg|testqrcode.png)$/ | 2
        'jsyntrax' | 'useSyntrax' | ~/^diag-syntrax-.+\.png$/ | 1
//        'batik'    | 'useBatik'    | ~/foo/ | 1
    }

    private void writeBuildFile() {
        writeBasicBuildFileGroovy(
                ['org.asciidoctor.jvm'],
                [
                        'org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain',
                        'org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjDiagram'
                ]
        )
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjHtml5.DEFAULT_NAME, DEFAULT_PUBLICATION)
    }

    private void activateExtension(String activator) {
        buildFile << """
        asciidoc.toolchains {
            ${DEFAULT_TOOLCHAIN} {
                asciidocExtensions {
                    ${DEFAULT_NAME}(AsciidoctorjDiagram) {
                        ${activator}()
                    }
                }
            }
        }
        """.stripIndent()
    }
}