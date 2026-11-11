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

import groovy.json.JsonSlurper
import org.asciidoctor.gradle.model5.jvm.testfixtures.AsciidoctorjHtmlIntegrationSpecification
import org.gradle.testkit.runner.TaskOutcome

class LogDirectoryPerTaskSpec extends AsciidoctorjHtmlIntegrationSpecification {

    void setup() {
        writeBasicBuildFileGroovy(['org.asciidoctor.jvm'])
        copyTestProject('multiple-warnings')
        buildFile << '''
        asciidoc {
            publications {
                first {
                    sourceSet {
                        sourceDir = 'src/docs/asciidoc'
                    }
                    output('asciidoctorj', 'html')
                }
                second {
                    sourceSet {
                        sourceDir = 'src/docs/asciidoc'
                    }
                    output('asciidoctorj', 'html')
                }
            }
        }
        '''.stripIndent()
    }

    void 'Tasks with the same toolchain and formatter write their logs to directories of their own'() {
        setup:
        final tasks = ['asciidoctorFirstHtml', 'asciidoctorSecondHtml']

        when:
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, tasks).build()

        then:
        tasks.every { result.task(":${it}").outcome == TaskOutcome.SUCCESS }

        and:
        tasks.every { logRecords(it).size() == 4 }
        !fileExists(buildDir, 'reports/asciidoc/logs/asciidoctorj')
    }

    private List logRecords(String task) {
        ((List) new JsonSlurper().parse(new File(buildDir, "reports/asciidoc/logs/${task}/log.json"))).flatten()
    }
}
