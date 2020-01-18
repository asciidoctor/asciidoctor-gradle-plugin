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
package org.asciidoctor.gradle.jvm.slides

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin
import org.asciidoctor.gradle.jvm.gems.AsciidoctorGemSupportPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.jvm.gems.AsciidoctorGemSupportPlugin.GEM_CONFIGURATION
import static org.asciidoctor.gradle.jvm.slides.AsciidoctorJRevealJSTask.REVEALJS_GEM

/** Adds an extension and task to create Reveal.js slides.
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorRevealJSBasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply plugin: AsciidoctorGemSupportPlugin
        project.apply plugin: AsciidoctorJBasePlugin

        project.extensions.create(RevealJSPluginExtension.NAME, RevealJSPluginExtension, project)
        project.extensions.create(RevealJSExtension.NAME, RevealJSExtension, project)

        addGems(project)
    }

    @CompileDynamic
    void addGems(Project project) {
        project.afterEvaluate {
            project.dependencies.add(GEM_CONFIGURATION, "rubygems:${REVEALJS_GEM}:${project.revealjs.version}") {
                exclude module: 'asciidoctor'
            }
        }
    }
}
