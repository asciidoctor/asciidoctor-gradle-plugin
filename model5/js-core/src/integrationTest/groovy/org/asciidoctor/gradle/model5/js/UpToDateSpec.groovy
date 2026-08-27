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
package org.asciidoctor.gradle.model5.js

import org.asciidoctor.gradle.model5.js.internal.formatters.DefaultAsciidoctorjsHtml5
import org.asciidoctor.gradle.model5.js.testfixtures.AsciidoctorjsHtmlIntegrationSpecification
import spock.lang.PendingFeatureIf

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.js.plugins.AsciidoctorjsPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class UpToDateSpec extends AsciidoctorjsHtmlIntegrationSpecification {

    void setup() {
//        writeHtmlBasedBuildFile()
    }

    @PendingFeatureIf(value = { IS_WINDOWS }, reason = 'Windows has a node exec issue')
    void 'Changes to NPM package list will cause rebuild'() {
        setup:
        writeHtmlBasedBuildFileWithImports([
            'org.asciidoctor.gradle.model5.js.extensions.AsciidoctorjsGenericExtension'
        ])
        copyTestProject('normal')
        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        missingIncludesAreFatal()
        """.stripIndent())

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result1.task(":${taskName}").outcome == SUCCESS

        when:
        final result2 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result2.task(":${taskName}").outcome == UP_TO_DATE

        when:
        buildFile << """
        asciidoc.toolchains.asciidoctorjs {
          asciidocExtensions {
            emoji(AsciidoctorjsGenericExtension) {
                usePackage('asciidoctor-emoji', '0.5.0')
                requires 'asciidoctor-emoji'
            }
          }
        }
        """.stripIndent()
        final result3 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result3.task(":${taskName}").outcome == SUCCESS

        when:
        final result4 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result4.task(":${taskName}").outcome == UP_TO_DATE
    }

    private void writeHtmlBasedBuildFileWithImports(List<String> imports) {
        writeBasicBuildFileGroovy(
            ['org.asciidoctor.js'],
            imports
        )
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjsHtml5.DEFAULT_NAME, DEFAULT_PUBLICATION)
    }
}