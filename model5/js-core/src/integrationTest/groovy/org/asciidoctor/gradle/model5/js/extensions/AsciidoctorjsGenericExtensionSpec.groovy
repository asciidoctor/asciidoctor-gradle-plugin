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
package org.asciidoctor.gradle.model5.js.extensions

import groovy.json.JsonSlurper
import org.asciidoctor.gradle.model5.js.internal.formatters.DefaultAsciidoctorjsHtml5
import org.asciidoctor.gradle.model5.js.internal.formatters.DefaultAsciidoctorjsTemplates
import org.asciidoctor.gradle.model5.js.testfixtures.AsciidoctorjsHtmlIntegrationSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.js.plugins.AsciidoctorjsPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class AsciidoctorjsGenericExtensionSpec extends AsciidoctorjsHtmlIntegrationSpecification {

    void setup() {
        writeBuildFileForHtmlAndGenericExtensions()
    }

    void 'Can apply and configure a generic extension'() {
        setup:
        final packageJson = new File(buildDir, 'tmp/asciidoctorjs-engine/asciidoctorjs/package.json')
        final slurper = new JsonSlurper()

        copyTestProject('emoji')
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

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName, '-s', '-i']).build()
        final json = slurper.parse(packageJson)

        then:
        result.task(":${taskName}").outcome == SUCCESS
        json.devDependencies.keySet().contains('asciidoctor-emoji')
        fileContains(outputDir,'emoji.html','<span class="emoji">')
        fileContains(outputDir,'emoji.html','alt="bear"')
    }

    void writeBuildFileForHtmlAndGenericExtensions() {
        writeBasicBuildFileGroovy(
            ['org.asciidoctor.js'],
            ['org.asciidoctor.gradle.model5.js.extensions.AsciidoctorjsGenericExtension']
        )
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjsHtml5.DEFAULT_NAME, DEFAULT_PUBLICATION)
    }
}
