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
package org.asciidoctor.gradle.jvm.gems

import com.github.jrubygradle.JRubyPluginExtension
import com.github.jrubygradle.JRubyPrepare
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/** Plugin that simplifies that management of external GEMs.
 *
 * It will:
 *
 * <ul>
 *  <li> Apply the {@code org.asciidoctor.jvm.base} plugin.</li>
 *  <li> Create a {@code asciidoctorGems} configuration.</li>
 *  <li> Create a {@code asciidoctorGemsPrepare} task.</li>
 * </ul>
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorGemSupportPlugin implements Plugin<Project> {

    static final String GEM_CONFIGURATION = 'asciidoctorGems'
    static final String GEMPREP_TASK = 'asciidoctorGemsPrepare'

    @Override
    void apply(Project project) {
        project.apply plugin: 'org.asciidoctor.jvm.base'
        Configuration gemConfig = project.configurations.maybeCreate(GEM_CONFIGURATION)

        AsciidoctorGemPrepare prepTask = project.tasks.create(GEMPREP_TASK, AsciidoctorGemPrepare)
        project.extensions.getByType(AsciidoctorJExtension).gemPaths { prepTask.outputDir }

        prepTask.with {
            dependencies gemConfig
            group = 'dependencies'
            description = 'Prepare additional GEMs for AsciidoctorJ'
        }

        workaroundEarlyEvaluationInPrepareTask(prepTask)
        playNiceWithJrubyGradle(project)
    }

    @CompileDynamic
    private void workaroundEarlyEvaluationInPrepareTask(JRubyPrepare prepTask) {
        prepTask.project.afterEvaluate {
            prepTask.with {
                outputDir = prepTask.project.file("${prepTask.project.buildDir}/asciidoctorGems")
            }
        }
    }

    private void playNiceWithJrubyGradle(Project project) {
        project.pluginManager.withPlugin('com.github.jruby-gradle.base') {
            project.afterEvaluate {
                project.extensions.getByType(JRubyPluginExtension).defaultVersion =
                    project.extensions.getByType(AsciidoctorJExtension).jrubyVersion
            }
        }
    }
}
