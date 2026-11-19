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
package org.asciidoctor.gradle.model5.jvm

import org.asciidoctor.gradle.model5.jvm.testfixtures.AsciidoctorjHtmlIntegrationSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

/**
 * Attributes whose values are produced by another task in the same build.
 *
 * @author Mattias Reichel
 *
 * @since 5.2.1
 */
class AttributesFromTaskOutputSpec extends AsciidoctorjHtmlIntegrationSpecification {

    void setup() {
        writeHtmlBasedBuildFile()
        buildFile << '''
        abstract class GenerateVersion extends DefaultTask {
            @Input
            abstract Property<String> getVersionText()

            @OutputFile
            abstract RegularFileProperty getOutputFile()

            @TaskAction
            void generate() {
                outputFile.get().asFile.text = versionText.get()
            }
        }

        def generateVersion = tasks.register('generateVersion', GenerateVersion) {
            versionText = providers.gradleProperty('docVersion').orElse('1.2.3')
            outputFile = layout.buildDirectory.file('version.txt')
        }
        '''.stripIndent()
        configureSourceSetGroovy(DEFAULT_PUBLICATION, '''
        attributes {
            attributeProvider(generateVersion.flatMap { it.outputFile }.map {
                ['app-version': it.asFile.text.trim()]
            })
        }
        '''.stripIndent())

        new File(projectDir, 'src/docs/asciidoc').mkdirs()
        new File(projectDir, 'src/docs/asciidoc/index.adoc').text = '''
        = Attributes from a task output

        Version: {app-version}
        '''.stripIndent()
    }

    void 'An attribute from a task output is converted after the task has run'() {
        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(':generateVersion').outcome == SUCCESS
        result.task(":${taskName}").outcome == SUCCESS
        new File(outputDir, 'index.html').text.contains('Version: 1.2.3')
    }

    void 'An attribute from a task output works with the configuration cache'() {
        when: 'the build runs on a clean project'
        final result1 = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName]).build()

        then: 'the attribute has the value that the other task produced'
        result1.task(':generateVersion').outcome == SUCCESS
        result1.task(":${taskName}").outcome == SUCCESS
        new File(outputDir, 'index.html').text.contains('Version: 1.2.3')

        when: 'the build runs again'
        final result2 = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName]).build()

        then: 'the task is up to date'
        result2.output.contains('Configuration cache entry reused')
        result2.task(":${taskName}").outcome == UP_TO_DATE

        when: 'the other task produces a different value'
        final result3 = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName, '-PdocVersion=4.5.6']).build()

        then: 'the conversion runs again with the new value'
        result3.task(":${taskName}").outcome == SUCCESS
        new File(outputDir, 'index.html').text.contains('Version: 4.5.6')
    }
}
