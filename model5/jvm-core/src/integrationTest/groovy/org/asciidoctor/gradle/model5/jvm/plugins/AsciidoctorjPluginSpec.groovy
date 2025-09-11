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
package org.asciidoctor.gradle.model5.jvm.plugins

import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreBasePlugin
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification
import org.gradle.testkit.runner.TaskOutcome

class AsciidoctorjPluginSpec extends IntegrationSpecification {

    void 'Can show toolchain information'() {
        setup:
        writeBasicBuildFileGroovy(['org.asciidoctor.jvm'])
        final taskName = AsciidoctorCoreBasePlugin.TOOLCHAIN_DISPLAY_TASK

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(":${taskName}").outcome == TaskOutcome.SUCCESS
        result.output.contains('.AsciidoctorjDocbook')
        result.output.contains('.AsciidoctorjHtml5')
        result.output.contains('.AsciidoctorjManpage')
        result.output.contains('.AsciidoctorjToolchain')
        !result.output.contains('.AsciidoctorjDiagram')
    }

    void 'Can show toolchain information with additional plugin'() {
        setup:
        writeBasicBuildFileGroovy(['org.asciidoctor.jvm', 'org.asciidoctor.jvm.diagram'])
        final taskName = AsciidoctorCoreBasePlugin.TOOLCHAIN_DISPLAY_TASK

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(":${taskName}").outcome == TaskOutcome.SUCCESS
        result.output.contains('.AsciidoctorjDocbook')
        result.output.contains('.AsciidoctorjHtml5')
        result.output.contains('.AsciidoctorjManpage')
        result.output.contains('.AsciidoctorjToolchain')
        result.output.contains('.AsciidoctorjDiagram')
    }
}