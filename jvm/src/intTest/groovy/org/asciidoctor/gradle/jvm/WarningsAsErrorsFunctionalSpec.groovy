/*
 * Copyright 2013-2020 the original author or authors.
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
import org.asciidoctor.gradle.testfixtures.generators.AsciidoctorjVersionProcessModeGenerator
import org.gradle.testkit.runner.BuildResult
import spock.lang.Ignore
import spock.lang.Unroll

class WarningsAsErrorsFunctionalSpec extends FunctionalSpecification {

    void setup() {
        createTestProject('missing-include')
    }

    @Unroll
    void 'Warnings can be treated as errors (#model, Groovy DSL)'() {
        given:
        getJvmConvertGroovyBuildFile("""
        asciidoctorj {
            fatalWarnings missingIncludes()
            version = '${model.version}'
        }

        asciidoctor {
            inProcess ${model.processMode}
            sources {
                include 'sample.adoc'
            }
        }
        """)

        when:
        BuildResult result = gradleRunner.buildAndFail()

        then:
        result.output.contains('ERROR: The following messages from AsciidoctorJ are treated as errors')
        result.output.contains('include file not found')

        where:
        model << AsciidoctorjVersionProcessModeGenerator.get()
    }

    @Unroll
    @Ignore
    void 'Warnings can be treated as errors (#model, Kotlin DSL)'() {
        given:
        getJvmConvertKotlinBuildFile("""
        asciidoctorj {
            fatalWarnings(missingIncludes())
            version = "${model.version}"
        }

        asciidoctor {
            inProcess ProcessMode.${model.processMode}
            sources "sample.adoc"
        }
        """)

        when:
        BuildResult result = getGradleRunner(['asciidoctor', '-s', '-i']).buildAndFail()

        then:
        result.output.contains('ERROR: The following messages from AsciidoctorJ are treated as errors')
        result.output.contains('include file not found')

        where:
        model << AsciidoctorjVersionProcessModeGenerator.random
    }
}