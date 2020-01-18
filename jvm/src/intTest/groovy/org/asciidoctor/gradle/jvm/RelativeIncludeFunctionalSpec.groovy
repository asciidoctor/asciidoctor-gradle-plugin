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
import spock.lang.Issue

/**
 * @author Lari Hotari
 */
class RelativeIncludeFunctionalSpec extends FunctionalSpecification {
    static final List DEFAULT_ARGS = ['asciidoctor', '-s']

    void setup() {
        createTestProject('relative-include')
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/454')
    void 'Includes are relative in source sub directories'() {
        given:
        getJvmConvertGroovyBuildFile( '''
        asciidoctor {
            baseDirFollowsSourceFile()
        }
        ''')

        when:
        getGradleRunner(DEFAULT_ARGS).withDebug(true).build()

        then:
        new File(testProjectDir.root, 'build/docs/asciidoc/nested/sample.html').text
                .contains('This is from _nested-include.adoc file.')
    }
}
