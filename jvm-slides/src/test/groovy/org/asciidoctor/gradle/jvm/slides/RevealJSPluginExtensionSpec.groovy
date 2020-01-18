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
package org.asciidoctor.gradle.jvm.slides

import org.asciidoctor.gradle.base.ModuleVersionLoader
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

class RevealJSPluginExtensionSpec extends Specification {

    @Shared
    Map<String, String> versionMap = ModuleVersionLoader.load('revealjs-extension')

    Project project = ProjectBuilder.builder().build()
    RevealJSPluginExtension ext

    void setup() {
        ext = new RevealJSPluginExtension(project)
    }

    void 'Can configure a local reveal.js plugin'() {
        when:
        ext.local('abc') {
            location = 'abcd'
            name = 'abcd'
        }
        then:
        ext.getByName('abc').location.name == 'abcd'
    }
}