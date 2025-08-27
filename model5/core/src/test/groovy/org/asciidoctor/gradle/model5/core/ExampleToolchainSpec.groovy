/*
 * Copyright ${year} the original author or authors.
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
package org.asciidoctor.gradle.model5.core

import org.asciidoctor.gradle.testfixtures.IntegrationSpecification
import org.gradle.testkit.runner.TaskOutcome

import static org.asciidoctor.gradle.model5.core.AsciidoctorCorePlugin.TOOLCHAIN_DISPLAY_TASK

class ExampleToolchainSpec extends IntegrationSpecification {

    void 'Can execute an asciidoctor task from a custom toolchain'() {
        setup:
        writeBuildFile()
        final taskName = 'myexampleAsciidoctorText'

        when:
        final result = getGradleRunner(IS_GROOVY_DSL,[taskName, '-s']).build()

        then:
        result.task(":${taskName}").outcome == TaskOutcome.SUCCESS
    }

    void writeBuildFile() {
        buildFile.text = '''
        plugins {
            id 'example.asciidoctor.toolchain'
        }
        
        asciidoc {
            publications {
                main {
                    outputFormats ('myexample.text')
                }
            }
        }
        '''
    }
}