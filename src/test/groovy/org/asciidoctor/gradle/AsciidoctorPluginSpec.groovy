/*
 * Copyright 2013-2014 the original author or authors.
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
package org.asciidoctor.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Plugin specification.
 *
 * @author Benjamin Muschko
 */
class AsciidoctorPluginSpec extends Specification {
    private static final String ASCIIDOCTOR = 'asciidoctor'

    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    @SuppressWarnings('MethodName')
    def "Applies plugin and checks default setup"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            project.apply plugin: AsciidoctorPlugin
        then:
            Task asciidoctorTask = project.tasks.findByName(ASCIIDOCTOR)
            asciidoctorTask != null
            asciidoctorTask.group == 'Documentation'
            asciidoctorTask.sourceDir == project.file('src/asciidoc')
            asciidoctorTask.outputDir == new File(project.buildDir, 'asciidoc')

            project.tasks.findByName('clean') != null
    }
}
