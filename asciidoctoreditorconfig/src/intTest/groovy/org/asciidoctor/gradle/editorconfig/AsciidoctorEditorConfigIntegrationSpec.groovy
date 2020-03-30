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
package org.asciidoctor.gradle.editorconfig

import org.asciidoctor.gradle.editorconfig.internal.FunctionalSpecification

class AsciidoctorEditorConfigIntegrationSpec extends FunctionalSpecification {

    void 'Can generate a asciidoctorconfigfile'() {
        setup:
        String key1 = 'foo1'
        String value1 = 'bar1'
        String key2 = 'foo2'
        String value2 = 'bar2'
        String key3 = 'foo3'
        String value3 = 'bar3'
        String projName = 'theProject'
        String groupName = 'the.group'
        String projVer = '1.0.0'

        File attrFile = new File(testProjectDir.root, 'inputs.adoc')
        attrFile.text = ":${key3}: ${value3}\n"

        getGroovyBuildFile("""
        group = '${groupName}'
        version = '${projVer}'
        apply plugin : 'org.asciidoctor.jvm.base'

        asciidoctorj {
            attributes ${key2}: '${value2}'
        }

        asciidoctorEditorConfig {
            attributes ${key1} : '${value1}'

            additionalAttributes 'inputs.adoc'
            additionalAttributes asciidoctorj
        }
        """)

        File outputFile = new File(testProjectDir.root, '.asciidoctorconfig')
        new File(testProjectDir.root, 'settings.gradle').text = "rootProject.name='${projName}'"

        when:
        getGradleRunner(['asciidoctorEditorConfig']).build()

        then:
        normalisedLineEndings(outputFile.text) == """:${key1}: ${value1}
:gradle-project-name: ${projName}
:gradle-project-group: ${groupName}
:gradle-project-version: ${projVer}
:${key2}: ${value2}
:${key3}: ${value3}
"""
    }

    String normalisedLineEndings(String text) {
        text.replaceAll('\\r', '')
    }
}