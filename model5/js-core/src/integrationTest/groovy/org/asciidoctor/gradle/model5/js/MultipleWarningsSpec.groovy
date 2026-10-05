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

import groovy.json.JsonSlurper
import org.asciidoctor.gradle.model5.js.testfixtures.AsciidoctorjsHtmlIntegrationSpecification
import org.gradle.testkit.runner.TaskOutcome

class MultipleWarningsSpec extends AsciidoctorjsHtmlIntegrationSpecification {

    void setup() {
        writeHtmlBasedBuildFile()
        copyTestProject('multiple-warnings')
    }

    void 'Multiple warnings are written to a valid JSON log'() {
        setup:
        final logFile = new File(buildDir, 'reports/asciidoc/logs/asciidoctorjs/html/log.json')

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(":${taskName}").outcome == TaskOutcome.SUCCESS

        when:
        final records = ((List) new JsonSlurper().parse(logFile)).flatten()

        then:
        records.size() == 4
    }

    void 'Log records contain the source location and the full message'() {
        setup:
        final logFile = new File(buildDir, 'reports/asciidoc/logs/asciidoctorjs/html/log.json')
        final missingInclude = new File(projectDir, 'src/docs/asciidoc/missing-include-1.adoc')

        when:
        getGradleRunner(IS_GROOVY_DSL, [taskName]).build()
        final records = ((List<Map>) new JsonSlurper().parse(logFile)).flatten() as List<Map>

        then:
        records*.path == ['sample.adoc'] * 4
        records*.line == ['3', '5', '7', '9']
        records[2].message == "include file not found: ${missingInclude.absolutePath}".toString()
    }
}
