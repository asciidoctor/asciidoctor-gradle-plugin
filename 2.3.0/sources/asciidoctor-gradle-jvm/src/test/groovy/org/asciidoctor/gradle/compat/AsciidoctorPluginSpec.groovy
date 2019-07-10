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

import org.asciidoctor.gradle.jvm.AsciidoctorJPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Plugin specification.
 *
 * @author Benjamin Muschko
 * @author Patrick Reimers
 * @author Markus Schlichting
 */
// Suppressing a number of warnings rather than fixing as this
// test will be removed in 3.0.0
@SuppressWarnings([
    'LineLength',
    'MethodReturnTypeRequired'
])
class AsciidoctorPluginSpec extends Specification {
    private static final String ASCIIDOCTOR = 'asciidoctor'
    private static final String BINTRAY = 'BintrayJCenter'
    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    @SuppressWarnings('MethodName')
    def "Compatibility plugin applies with default setup"() {
        expect:
        project.tasks.findByName(ASCIIDOCTOR) == null
        when:
        project.apply plugin: AsciidoctorCompatibilityPlugin
        then:
        Task asciidoctorTask = project.tasks.findByName(ASCIIDOCTOR)
        asciidoctorTask != null
        asciidoctorTask.group == 'Documentation'
        asciidoctorTask.sourceDir == project.file('src/docs/asciidoc')
        asciidoctorTask.outputDir == new File(project.buildDir, 'asciidoc')

        project.tasks.findByName('clean') != null
    }

    @Ignore("Method 'getDependencyResolutionBroadcast' is unknown")
    @SuppressWarnings('MethodName')
    def "Compatibility extension accepts alternative Asciidoctorj version"() {
        expect:
        project.tasks.findByName(ASCIIDOCTOR) == null

        when:
        project.apply plugin: AsciidoctorCompatibilityPlugin

        def expectedVersion = 'my.expected.version-SNAPSHOT'
        project.asciidoctorj.version = expectedVersion

        def expectedDslVersion = 'dsl.' + expectedVersion
        project.asciidoctorj.groovyDslVersion = expectedDslVersion

        def config = project.project.configurations.getByName(ASCIIDOCTOR)
        def dependencies = config.dependencies
        assert dependencies.isEmpty()

        // mock-trigger beforeResolve() to avoid 'real' resolution of dependencies
        DependencyResolutionListener broadcast = config.dependencyResolutionBroadcast
        ResolvableDependencies incoming = config.incoming
        broadcast.beforeResolve(incoming)
        def dependencyHandler = project.dependencies

        then:
        assert dependencies.contains(dependencyHandler.create(AsciidoctorCompatibilityPlugin.ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY + expectedDslVersion))
        assert dependencies.contains(dependencyHandler.create(AsciidoctorCompatibilityPlugin.ASCIIDOCTORJ_CORE_DEPENDENCY + expectedVersion))
    }

    @SuppressWarnings('MethodName')
    def "Compatibility plugin adds JCenter repository by default"() {
        when:
        project.apply plugin: AsciidoctorCompatibilityPlugin
        project.evaluate()

        then:
        project.repositories.findByName(BINTRAY)
    }

    @SuppressWarnings('MethodName')
    def "Compatibility plugin does nto add JCenter repository when noDefaultRepositories is set"() {
        when:
        project.apply plugin: AsciidoctorCompatibilityPlugin
        project.extensions.asciidoctorj.noDefaultRepositories = true
        project.evaluate()

        then:
        project.repositories.findByName(BINTRAY) == null
    }

    @SuppressWarnings('MethodName')
    def 'Cannot combine compatibility plugin with newer plugins'() {
        when:
        project.apply plugin: AsciidoctorJPlugin
        project.apply plugin: AsciidoctorCompatibilityPlugin

        then:
        thrown(GradleException)
    }
}

