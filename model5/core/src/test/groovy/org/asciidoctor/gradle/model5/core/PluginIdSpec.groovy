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
package org.asciidoctor.gradle.model5.core

import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification
import spock.lang.Unroll

class PluginIdSpec extends UnitTestSpecification {

    @Unroll
    void 'Can apply #pluginId'() {
        when:
        project.pluginManager.apply(pluginId)

        then:
        noExceptionThrown()

        where:
        pluginId << [
            'org.asciidoctor.core.base',
            'org.asciidoctor.core',
            'org.asciidoctor.themes'
        ]
    }
}
