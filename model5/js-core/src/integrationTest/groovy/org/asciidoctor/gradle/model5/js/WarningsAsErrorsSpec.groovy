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
package org.asciidoctor.gradle.model5.js

import org.asciidoctor.gradle.model5.js.testfixtures.AsciidoctorjsHtmlIntegrationSpecification
import org.gradle.testkit.runner.TaskOutcome

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION

class WarningsAsErrorsSpec extends AsciidoctorjsHtmlIntegrationSpecification {

    void setup() {
        writeHtmlBasedBuildFile()
        copyTestProject('missing-include')
    }

    void 'Warnings can be treated as errors'() {
        setup:
        final logFile = new File(buildDir, 'reports/asciidoc/logs/asciidoctorjs/html/log.json')
        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        missingIncludesAreFatal()
        """.stripIndent())

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).buildAndFail()

        then:
        result.task(":${taskName}").outcome == TaskOutcome.FAILED
        result.output.contains('fatal issues where discovered')
        logFile.exists()
    }
}