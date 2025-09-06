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
package org.asciidoctor.gradle.model5.jvm

import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjTemplates
import org.asciidoctor.gradle.model5.jvm.testfixtures.AsciidoctorjHtmlIntegrationSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GemsSpec extends AsciidoctorjHtmlIntegrationSpecification {

    void 'Will fail if templates are requested, but gem plugin has not been applied'() {
        setup:
        writeHtmlBasedBuildFile()
        copyTestProject('normal')
        buildFile << """
        asciidoc.toolchains.asciidoctorj.registeredOutputFormatters.html {
            templateDirs 'src/docs/asciidoc/templates/erb/html5'
        }
        """.stripIndent()

        when:
        getGradleRunner(IS_GROOVY_DSL, [taskName]).buildAndFail()

        then:
        noExceptionThrown()
    }

    void 'Can apply the #engine-based template'() {
        setup:
        writeGemUsingBuildFile()
        copyTestProject('normal')
        buildFile << """
        asciidoc.toolchains.asciidoctorj.registeredOutputFormatters.html {
            templateDirs 'src/docs/asciidoc/templates/${engine}/html5'
            useEngine '${engine}'
        }
        """.stripIndent()

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName, '-s']).build()

        then:
        result.task(":asciidoctorGemsAsciidoctorjPrepare").outcome == SUCCESS
        result.task(":asciidoctorGemJarAsciidoctorjPrepare").outcome == SUCCESS
        result.task(":${taskName}").outcome == SUCCESS
        result.output.contains("Successfully installed ${engine == 'erb' ? 'tilt' : engine}-")

        where:
        engine << DefaultAsciidoctorjTemplates.SUPPORTED_TEMPLATE_ENGINES
    }

    void writeGemUsingBuildFile() {
        writeHtmlBasedBuildFile(['org.asciidoctor.jvm', 'org.asciidoctor.jvm.gems'])
        buildFile << '''

        repositories {
            ruby.gems()
        }
        
        '''.stripIndent()
    }
}
