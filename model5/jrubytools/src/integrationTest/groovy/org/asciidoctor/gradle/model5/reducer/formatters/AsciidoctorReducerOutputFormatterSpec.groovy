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
package org.asciidoctor.gradle.model5.reducer.formatters

import org.asciidoctor.gradle.model5.reducer.internal.formatters.DefaultReducerAsciidocOutputFormatter
import org.asciidoctor.gradle.model5.reducer.plugins.ReducerPlugin
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AsciidoctorReducerOutputFormatterSpec extends IntegrationSpecification {

    // asciidoctorPrepareAsciidoctorReducerToolchain
    void 'Reducer formatter will convert files and copy resources'() {
        setup:
        final taskName = 'asciidoctorReducedAdoc'
        final outputDir = new File(buildDir, 'docs/asciidoc/reducedAdoc')

        writeBuildFile()
        copyTestProject('relative-include-with-images')

        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        resources {
            include 'nested/images/**'
        }
        """.stripIndent())

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName, '-s', '-i']).build()

        then: 'Task completed successfully'
        result.task(":${taskName}").outcome == SUCCESS

        and: 'Content exists'
        fileExists(outputDir, 'nested/sample.reduced.adoc')
        fileExists(outputDir, 'nested/sample2.reduced.adoc')
        fileExists(outputDir, 'nested/images/fake.txt')
        fileExists(outputDir, 'nested/images/fake2.txt')
    }

    void writeBuildFile() {
        writeBasicBuildFileGroovy([ReducerPlugin.PLUGIN_ID])
        addOutputToSourceSetGroovy(
            ReducerPlugin.DEFAULT_TOOLCHAIN,
            DefaultReducerAsciidocOutputFormatter.DEFAULT_NAME,
            DEFAULT_PUBLICATION
        )
        buildFile << '''
        repositories {
            ruby.gems()
            ruby.coop()
        }
        '''.stripIndent()
    }
}
