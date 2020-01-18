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
package org.asciidoctor.gradle.base

import org.asciidoctor.gradle.base.internal.FunctionalSpecification

class ApplyPluginSpec extends FunctionalSpecification {

    void 'Apply the base plugin in a Kotlin DSL'() {
        given:
        getKotlinBuildFile('')

        when:
        getGradleRunnerForKotlin(['tasks', '-s']).build()

        then:
        noExceptionThrown()
    }

    void 'Apply the base plugin in a Groovy DSL'() {
        given:
        getGroovyBuildFile('', 'base')

        when:
        getGradleRunner(['tasks']).build()

        then:
        noExceptionThrown()
    }
}