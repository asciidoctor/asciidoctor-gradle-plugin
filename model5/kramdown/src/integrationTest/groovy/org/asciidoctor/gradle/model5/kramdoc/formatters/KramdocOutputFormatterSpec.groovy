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
package org.asciidoctor.gradle.model5.kramdoc.formatters

import org.asciidoctor.gradle.model5.kramdoc.KramdocModelExtension
import org.asciidoctor.gradle.model5.kramdoc.internal.formatters.DefaultKramdocOutputFormatter
import org.asciidoctor.gradle.model5.kramdoc.plugins.KramdocPlugin
import org.asciidoctor.gradle.model5.reducer.internal.formatters.DefaultReducerAsciidocOutputFormatter
import org.asciidoctor.gradle.model5.reducer.plugins.ReducerPlugin
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class KramdocOutputFormatterSpec extends IntegrationSpecification {

    void 'Kramdoc formatter will convert files and copy resources'() {
        setup:
        final taskName = 'kramdocAsciidoc'
        final outputDir = new File(buildDir, 'docs/kramdown/asciidoc')

        writeBuildFile()
        copyTestProject('kramdoc')

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName, '-s', '-i']*.toString()).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS
        result.task(":asciidoctorPrepareKramdownToolchain").outcome == SUCCESS

        and: 'Content exists'
        fileExists(outputDir, 'plantuml.adoc')
        fileExists(outputDir, 'sample.adoc')
        fileExists(outputDir, 'images/fake.txt')
        fileExists(outputDir, 'images/fake2.txt')
    }

    void writeBuildFile() {
        writeBasicBuildFileGroovy(['org.asciidoctor.kramdoc'])
        buildFile << """
        ${KramdocModelExtension.NAME} {
            publications {
                main {
                    sourceSet {
                        sourceDir = 'src/docs/markdown'
                        
                        resources {
                            include 'images/**'
                        }
                    }
                    output('${KramdocPlugin.DEFAULT_TOOLCHAIN}', '${DefaultKramdocOutputFormatter.DEFAULT_NAME}')
                }
            }
        }
        
        repositories {
            ruby.gems()
            ruby.coop()
        }
        """.stripIndent()
    }
}
