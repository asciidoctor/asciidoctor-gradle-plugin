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
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Plugin specification.
 *
 * @author Benjamin Muschko
 * @author Patrick Reimers
 * @author Markus Schlichting
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
            asciidoctorTask.sourceDir == project.file('src/docs/asciidoc')
            asciidoctorTask.outputDir == new File(project.buildDir, 'asciidoc')

            project.tasks.findByName('clean') != null
    }

    def "testPluginWithAlternativeAsciidoctorVersion"() {
        expect:
        project.tasks.findByName(ASCIIDOCTOR) == null

        when:
        project.apply plugin: AsciidoctorPlugin

        def expectedVersion = 'my.expected.version-SNAPSHOT'
        project.asciidoctorj.version = expectedVersion

        def expectedDslVersion = 'dsl.' + expectedVersion
        project.asciidoctorj.groovyDslVersion = expectedDslVersion

        def config = project.project.configurations.getByName('asciidoctor')
        def dependencies = config.dependencies
        assert dependencies.isEmpty();

        // mock-trigger beforeResolve() to avoid 'real' resolution of dependencies
        DependencyResolutionListener broadcast = config.getDependencyResolutionBroadcast()
        ResolvableDependencies incoming = config.getIncoming()
        broadcast.beforeResolve(incoming)
        def dependencyHandler = project.getDependencies();

        then:
        assert dependencies.contains(dependencyHandler.create(AsciidoctorPlugin.ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY + expectedDslVersion))
        assert dependencies.contains(dependencyHandler.create(AsciidoctorPlugin.ACSIIDOCTORJ_CORE_DEPENDENCY + expectedVersion))
    }
}
