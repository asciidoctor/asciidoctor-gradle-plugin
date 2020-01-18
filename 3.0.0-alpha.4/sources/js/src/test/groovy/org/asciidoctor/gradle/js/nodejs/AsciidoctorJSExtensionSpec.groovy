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
package org.asciidoctor.gradle.js.nodejs

import org.asciidoctor.gradle.base.ModuleVersionLoader
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

@SuppressWarnings(['MethodName'])
class AsciidoctorJSExtensionSpec extends Specification {
    @Shared
    Map<String,String> versionMap = ModuleVersionLoader.load('asciidoctorjs-extension')

    Project project = ProjectBuilder.builder().build()
    AsciidoctorJSExtension asciidoctorjs

    void setup() {
        project.apply plugin : 'org.asciidoctor.js.base'
        asciidoctorjs = project.extensions.getByType(AsciidoctorJSExtension)
    }

    void 'Can set a default docbook version'() {
        when:
        asciidoctorjs.modules.docbook.use()

        then:
        asciidoctorjs.modules.docbook.version == versionMap['asciidoctorjs.docbook']
        asciidoctorjs.requires.find { it.contains('docbook-converter') }
    }
}