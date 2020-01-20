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

import org.asciidoctor.gradle.jvm.gems.AsciidoctorGemPrepare
import org.asciidoctor.gradle.testfixtures.internal.TestFixtureVersionLoader
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static org.asciidoctor.gradle.jvm.gems.AsciidoctorGemSupportPlugin.GEM_CONFIGURATION
import static org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask.REVEALJS_GEM
import static org.asciidoctor.gradle.jvm.slides.AsciidoctorRevealJSPlugin.REVEALJS_TASK

class AsciidoctorRevealJSPluginSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    void 'Can apply plugin'() {
        given:
        ConfigurationContainer configurations = project.configurations
        TaskContainer tasks = project.tasks
        ExtensionContainer extensions = project.extensions

        when:
        project.apply plugin: 'org.asciidoctor.jvm.revealjs'
        project.evaluate()

        then: 'reveal.js GEM has been added to the dependencies'
        configurations.getByName(GEM_CONFIGURATION).dependencies.find { Dependency e ->
            e.group == 'rubygems'
            e.name == REVEALJS_GEM
            e.version == TestFixtureVersionLoader.VERSIONS['revealjs.gem']
        }

        and: 'Revels.js Asciiidoctor task is configured'
        tasks.getByName(REVEALJS_TASK).templateRelativeDir == 'reveal.js'
        tasks.getByName(REVEALJS_TASK).outputDir == project.file("${project.buildDir}/docs/asciidocRevealJs")
        tasks.getByName(REVEALJS_TASK).dependsOn.find { it instanceof AsciidoctorGemPrepare }

        and: 'Reveal.JS extension is created'
        extensions.getByName(RevealJSExtension.NAME) instanceof RevealJSExtension

        and: 'Reveal.JS plugine extension is created'
        extensions.getByName(RevealJSPluginExtension.NAME) instanceof RevealJSPluginExtension
    }
}