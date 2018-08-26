/*
 * Copyright 2013-2018 the original author or authors.
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
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.asciidoctor.gradle.jvm.gems.AsciidoctorGemPrepare
import org.asciidoctor.gradle.jvm.gems.AsciidoctorGemSupportPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.asciidoctor.gradle.jvm.gems.AsciidoctorGemSupportPlugin.GEM_CONFIGURATION

/** Adds an extension and task to create Reveal.js slides.
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorRevealJSBasePlugin implements Plugin<Project> {

    final static String REVEALJS_GEM = 'asciidoctor-revealjs'

    @Override
    void apply(Project project) {
        project.apply plugin: 'org.asciidoctor.jvm.gems'
        project.apply plugin: 'org.asciidoctor.jvm.base'

        AsciidoctorJExtension asciidoctorj = project.extensions.getByType(AsciidoctorJExtension)
        AsciidoctorGemPrepare gemPrepare = (AsciidoctorGemPrepare) (project.tasks.getByName(AsciidoctorGemSupportPlugin.GEMPREP_TASK))

        project.extensions.create(RevealJSPluginExtension.NAME, RevealJSPluginExtension, project)
        project.extensions.create(RevealJSExtension.NAME, RevealJSExtension, project)

        asciidoctorj.with {
            requires(REVEALJS_GEM)
            gemPaths { gemPrepare.outputDir }
        }

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
