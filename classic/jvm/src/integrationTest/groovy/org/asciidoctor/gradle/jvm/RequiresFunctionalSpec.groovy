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
package org.asciidoctor.gradle.jvm

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Timeout

import static org.asciidoctor.gradle.testfixtures.AsciidoctorjTestVersions.DIAGRAM_DITAA_SERIES_20
import static org.asciidoctor.gradle.testfixtures.AsciidoctorjTestVersions.DIAGRAM_SERIES_20

/**
 * @author Schalk W. Cronjé
 */
class RequiresFunctionalSpec extends FunctionalSpecification {

    static final List DEFAULT_ARGS = ['asciidoctor', '-s']
    static final String TASK_PATH = ":${DEFAULT_ARGS[0]}"
    static final String DITAA = 'ditaa.adoc'

    void setup() {
        createTestProject('requires')
    }

    @Timeout(value=360)
    void 'Asciidoctorj-diagram is registered and re-used across across multiple builds'() {
        given:
        final String imageFileExt = '.png'
        getBuildFile(DITAA, '')

        when:
        final BuildResult firstInvocationResult = getGradleRunner(DEFAULT_ARGS).build()
        File outputFolder = new File(buildDir, 'docs/asciidoc')
        File outputFile = new File(outputFolder, 'ditaa.html')

        then:
        firstInvocationResult.task(TASK_PATH).outcome == TaskOutcome.SUCCESS
        outputFile.exists()
        outputFolder.listFiles().findAll { it.name.endsWith(imageFileExt) }.size() == 1

        when:
        new File(projectDir, "src/docs/asciidoc/${DITAA}") << 'changes'
        final BuildResult secondInvocationResult = getGradleRunner(['clean'] + DEFAULT_ARGS).build()

        then:
        secondInvocationResult.task(TASK_PATH).outcome == TaskOutcome.SUCCESS
        outputFile.exists()
        outputFolder.listFiles().findAll { it.name.endsWith(imageFileExt) }.size() == 1
        outputFolder.listFiles().findAll { it.name.endsWith(imageFileExt) && it.name.startsWith('d') }.size() == 1
    }

    File getBuildFile(String sourceName, String extraContent) {
        getJvmConvertGroovyBuildFile("""
            asciidoctorj {
                modules {
                    diagram.version = '${DIAGRAM_SERIES_20}'
                }
            }

            configurations {
                diagram
            }
            dependencies {
                diagram 'org.asciidoctor:asciidoctorj-diagram-ditaamini:${DIAGRAM_DITAA_SERIES_20}'
            }

            
            asciidoctor {

                configurations 'diagram'
                sourceDir = 'src/docs/asciidoc'

                sources {
                    include '${sourceName}'
                }

                withIntermediateArtifacts {
                    include '**/d*.png'
                }
            }

            ${extraContent}
        """)
    }
}