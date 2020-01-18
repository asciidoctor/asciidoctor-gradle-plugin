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
package org.asciidoctor.gradle.jvm.gems

import com.github.jrubygradle.api.core.JRubyCorePlugin
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.ysb33r.grolifant.api.TaskProvider

import static org.ysb33r.grolifant.api.TaskProvider.registerTask

/** Plugin that simplifies that management of external GEMs.
 *
 * It will:
 *
 * <ul>
 *  <li> Apply the {@code org.asciidoctor.jvm.base} plugin.</li>
 *  <li> Apply the {@code com.github.jrubygradle.core} plugin.</li>
 *  <li> Create a {@code asciidoctorGems} configuration.</li>
 *  <li> Create a {@code asciidoctorGemsPrepare} task.</li>
 * </ul>
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorGemSupportPlugin implements Plugin<Project> {

    public static final String GEM_CONFIGURATION = 'asciidoctorGems'
    public static final String GEMPREP_TASK = 'asciidoctorGemsPrepare'

    @Override
    void apply(Project project) {
        project.apply plugin: AsciidoctorJBasePlugin
        project.apply plugin: JRubyCorePlugin
        Configuration gemConfig = project.configurations.maybeCreate(GEM_CONFIGURATION)

        Action gemPrepDefaults = new Action<AsciidoctorGemPrepare>() {
            @Override
            void execute(AsciidoctorGemPrepare asciidoctorGemPrepare) {
                asciidoctorGemPrepare.with {
                    dependencies gemConfig
                    group = 'dependencies'
                    description = 'Prepare additional GEMs for AsciidoctorJ'
                    outputDir = "${project.buildDir}/.asciidoctorGems"
                }
            }
        }

        TaskProvider<AsciidoctorGemPrepare> prepTask = registerTask(
            project,
            GEMPREP_TASK,
            AsciidoctorGemPrepare,
            gemPrepDefaults
        )
        project.extensions.getByType(AsciidoctorJExtension).gemPaths { prepTask.get().outputDir }
    }
}
