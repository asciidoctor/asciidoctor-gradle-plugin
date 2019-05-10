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
package org.asciidoctor.gradle.slides.export.deck2pdf

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AbstractAsciidoctorBaseTask
import org.asciidoctor.gradle.base.slides.SlidesToExportAware
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.ysb33r.grolifant.api.TaskProvider

import java.util.concurrent.Callable
import java.util.regex.Pattern

/** Plugin that adds conventions for specific tasks including
 * the creation of specific tasks for conversion to PDF, PNG & JPG.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class AsciidoctorDeck2PdfPlugin implements Plugin<Project> {

    private static final Pattern NAME_MATCHER = ~/(.+)To(Pdf|Png|Jpg)$/

    @Override
    @SuppressWarnings('DeadCode')
    void apply(Project project) {
        throw new GradleException(
                'Deck2Pdf not currently supported due to JavaFX issues. See ' +
                        'https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/374'
        )
        project.apply plugin: AsciidoctorDeck2PdfBasePlugin
        addDeck2PdfTaskRule(project)
    }

    private void addDeck2PdfTaskRule(Project project) {
        project.tasks.addRule(
            'Pattern: <TaskName>To(Pdf|Jpg|Png): Exports a slide deck to specific format'
        ) { String name ->
            Optional<Deck2PdfTaskRuleDetails> details = matchingTaskName(name)

            if (details.present) {
                try {
                    TaskProvider associate = TaskProvider.taskByName(project, details.get().associatedTaskName)

                    // I think it's OK to create the task at this point in time as there is a good chance that
                    // it will used. Ideally there should be a typed(Class) method on project.tasks
                    // (https://github.com/gradle/gradle-native/issues/772)

                    if (associate.get() instanceof SlidesToExportAware) {
                        Deck2Pdf.Extension deckExt = Deck2Pdf.addExtensionTo(
                                associate.get(),
                                ((SlidesToExportAware) associate.get()).profile
                        )
                        TaskProvider target = TaskProvider.registerTask(
                                project, details.get().targetTaskName,
                                details.get().type
                        )
                        Action configurator = new Action<Deck2ExportBaseTask>() {
                            @Override
                            void execute(Deck2ExportBaseTask t) {
                                t.setProfile {
                                    ((SlidesToExportAware) associate.get()).profile
                                } as Callable
                                t.slides { associate.get() } // Should really be associate.unresolved()
                                t.outputDir = {
                                    File associateOutputDir = ((AbstractAsciidoctorBaseTask) associate.get()).outputDir
                                    new File(associateOutputDir.parentFile, "${details.get().targetTaskName}")
                                }
                                if (deckExt) {
                                    t.parametersProvider = deckExt.parametersProvider
                                }
                            }
                        }
                        target.configure(configurator as Action<Task>)
                    }
                } catch (Exception) {
                    // TaskProvider really needs a findTaskByName
                }
            }
        }
    }

    @CompileDynamic
    @SuppressWarnings('VariableTypeRequired')
    private Optional<Deck2PdfTaskRuleDetails> matchingTaskName(final String targetName) {
        def m = targetName =~ NAME_MATCHER
        if (m.matches()) {
            Deck2PdfTaskRuleDetails.build(m[0][1], targetName, m[0][2])
        } else {
            Optional.empty()
        }
    }

    private static class Deck2PdfTaskRuleDetails {
        final String associatedTaskName
        final String targetTaskName
        final Class type

        Deck2PdfTaskRuleDetails(String associated, String target, String type) {
            this.associatedTaskName = associated
            this.targetTaskName = target

            switch (type.toLowerCase(Locale.US)) {
                case 'pdf':
                    this.type = Deck2PdfTask
                    break
                case 'png':
                    this.type = Deck2PngTask
                    break
                case 'jpg':
                    this.type = Deck2JpgTask
                    break
            }
        }

        static Optional<Deck2PdfTaskRuleDetails> build(String associated, String target, String type) {
            Optional.of(new Deck2PdfTaskRuleDetails(associated, target, type))
        }
    }
}
