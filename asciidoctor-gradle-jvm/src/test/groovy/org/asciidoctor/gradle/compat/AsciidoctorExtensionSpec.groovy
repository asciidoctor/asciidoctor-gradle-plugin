/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.compat

import org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

@SuppressWarnings(['MethodName'])
class AsciidoctorExtensionSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    void setup() {
        project.allprojects {
            apply plugin : 'org.asciidoctor.convert'
        }
    }

    void 'Default version should be the same as AsciidoctorJExtension'() {
        expect:
        project.extensions.asciidoctorj.version == AsciidoctorjTestVersions.SERIES_20
    }

    void 'Default GroovyDSL version should be the same as AsciidoctorJExtension'() {
        expect:
        project.extensions.asciidoctorj.groovyDslVersion == AsciidoctorjTestVersions.GROOVYDSL_SERIES_20
    }

}