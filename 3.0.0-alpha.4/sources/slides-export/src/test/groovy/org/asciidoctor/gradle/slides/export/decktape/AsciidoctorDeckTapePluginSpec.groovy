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
package org.asciidoctor.gradle.slides.export.decktape

import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AsciidoctorDeckTapePluginSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    void 'Can apply plugin: org.asciidoctor.decktape'() {
        when:
        project.allprojects {
            apply plugin: 'org.asciidoctor.decktape'
        }

        then:
        noExceptionThrown()
    }

    void 'Will not create a reveal export task if appropriate plugin not applied'() {
        when:
        project.allprojects {
            apply plugin: 'org.asciidoctor.decktape'
        }

        project.tasks.getByName('asciidoctorRevealJsExport')

        then:
        thrown(UnknownTaskException)
    }

    void 'Will not create an arbitrary task on demand that ends with Export'() {
        when:
        project.allprojects {
            apply plugin: 'org.asciidoctor.decktape'
        }

        project.tasks.getByName('fooExport')

        then:
        thrown(UnknownTaskException)
    }

    void 'Will not create an arbitrary task on demand'() {
        when:
        project.allprojects {
            apply plugin: 'org.asciidoctor.decktape'
        }

        project.tasks.getByName('foo')

        then:
        thrown(UnknownTaskException)
    }

}