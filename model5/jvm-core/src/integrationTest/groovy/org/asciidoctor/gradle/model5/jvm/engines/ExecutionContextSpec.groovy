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
package org.asciidoctor.gradle.model5.jvm.engines

import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.testfixtures.AsciidoctorjHtmlIntegrationSpecification
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN
import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL

class ExecutionContextSpec extends AsciidoctorjHtmlIntegrationSpecification {
    void setup() {
        writeBasicBuildFileGroovy(
                ['org.asciidoctor.jvm', 'java'],
                [
                        'org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain',
                        'org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjHtml5'
                ]
        )
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjHtml5.DEFAULT_NAME, DEFAULT_PUBLICATION)
        copyTestProject('normal')
    }

    @Unroll
    void 'Can run in separate process with different JVM #cc configuration-cache'() {
        setup:
        writeSettingsFile()
        final tasks = [taskName, '-i']

        buildFile << """
        asciidoc.toolchains.${DEFAULT_TOOLCHAIN}.registeredOutputFormatters {
            html(AsciidoctorjHtml5) {
                useProcessIsolation {
                    javaLauncher = javaToolchains.launcherFor {
                        languageVersion = JavaLanguageVersion.of('11')
                    }
                    systemProperties( ExecutionContentSpec : 'is-running-now' )
                }
            }
        }
        """

        when:
        final result = ( ccMode ? getGradleRunnerConfigCache(IS_GROOVY_DSL, tasks) : getGradleRunner(IS_GROOVY_DSL,tasks)).build()

        then:
        result.task(":${taskName}").outcome == TaskOutcome.SUCCESS
        result.output.contains('Running workers out of process')

        where:
        cc        | ccMode
        'without' | false
        'with'    | true
    }

    private void writeSettingsFile() {
        settingsFile.text = """
        pluginManagement {
            repositories {
                ${getOfflineRepositories(GROOVY_DSL)}
                gradlePluginPortal()
                mavenCentral()
            }
        }
        plugins {
            id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
        }

        rootProject.name = 'test-project'
        """.stripIndent()
    }
}