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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AbstractAsciidoctorBaseTask
import org.asciidoctor.gradle.base.slides.SlidesToExportAware
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.ysb33r.grolifant.api.TaskProvider

import java.util.concurrent.Callable
import java.util.regex.Pattern

/** Plugin that will conventions for conversion from slides to
 * PDFs and images.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class AsciidoctorDeckTapePlugin implements Plugin<Project> {

    private static final Pattern NAME_MATCHER = ~/(.+)Export$/

    @Override
    void apply(Project project) {
        project.apply plugin: AsciidoctorDeckTapeBasePlugin
        addDeckTapeTaskRule(project)
    }

    private void addDeckTapeTaskRule(Project project) {
        project.tasks.addRule(
                'Pattern: <TaskName>Export: Exports a slide deck to PDF and optionally an image format'
        ) { String name ->
            Optional<DeckTapeTaskRuleDetails> details = matchingTaskName(name)

            if (details.present) {
                try {
                    TaskProvider associate = TaskProvider.taskByName(project, details.get().associatedTaskName)

                    // I think it's OK to create the task at this point in time as there is a good chance that
                    // it will used. Ideally there should be a typed(Class) method on project.tasks
                    // (https://github.com/gradle/gradle-native/issues/772)

                    if (associate.get() instanceof SlidesToExportAware) {
                        Task target = project.tasks.create(details.get().targetTaskName, details.get().type)
                        Action configurator = new Action<DeckTapeTask>() {
                            @Override
                            void execute(DeckTapeTask t) {
                                t.setProfile {
                                    ((SlidesToExportAware) associate.get()).profile
                                } as Callable
                                t.slides(associate.get()) // Should really be associate.unresolved()
                                t.outputDir = {
                                    File associateOutputDir = ((AbstractAsciidoctorBaseTask) associate.get()).outputDir
                                    new File(associateOutputDir.parentFile, "${associateOutputDir.name}Export")
                                }
                            }
                        }
                        configurator.execute((DeckTapeTask)target)
                    }
                } catch (Exception) {
                    // TaskProvider really needs a findTaskByName
                }
            }
        }
    }

    @CompileDynamic
    @SuppressWarnings('VariableTypeRequired')
    private Optional<DeckTapeTaskRuleDetails> matchingTaskName(final String targetName) {
        def m = targetName =~ NAME_MATCHER
        if (m.matches()) {
            DeckTapeTaskRuleDetails.build(m[0][1], targetName)
        } else {
            Optional.empty()
        }
    }

    private static class DeckTapeTaskRuleDetails {
        final String associatedTaskName
        final String targetTaskName
        final Class type

        DeckTapeTaskRuleDetails(String associated, String target) {
            this.associatedTaskName = associated
            this.targetTaskName = target
            this.type = DeckTapeTask
        }

        static Optional<DeckTapeTaskRuleDetails> build(String associated, String target) {
            Optional.of(new DeckTapeTaskRuleDetails(associated, target))
        }
    }
}
