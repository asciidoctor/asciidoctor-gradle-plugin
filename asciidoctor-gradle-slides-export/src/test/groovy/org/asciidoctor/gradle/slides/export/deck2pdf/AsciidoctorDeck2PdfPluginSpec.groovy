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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.grolifant.api.TaskProvider
import spock.lang.Specification

import static org.asciidoctor.gradle.slides.export.deck2pdf.fixtures.TaskTypes.TASK_TYPES
import static org.asciidoctor.gradle.slides.export.deck2pdf.fixtures.TaskTypes.registerSourceTasks

class AsciidoctorDeck2PdfPluginSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    void 'Can apply plugin: org.asciidoctor.deck2pdf'() {
        when:
        project.allprojects {
            apply plugin: 'org.asciidoctor.deck2pdf'
        }

        then:
        project.extensions.getByName(Deck2PdfExtension.EXTENSION_NAME)
    }

    void 'Tasks are selectively decorated when `org.asciidoctor.deck2pdf` is applied'() {
        given: 'Three Slide2ExportAware tasks'
        final Map<String, TaskProvider> sourceTasks = registerSourceTasks(project)
        final Map<String, Class> expectedExtensions = TASK_TYPES.collectEntries { k, v ->
            [k, v.extensionType]
        }

        when: 'The deck2pdf plugin is applied'
        project.allprojects {
            apply plugin: 'org.asciidoctor.deck2pdf'
        }

        and: 'Conversion tasks are accessed'
        List<Task> exportTasks = sourceTasks.keySet().collectMany { taskName ->
            ['pdf', 'jpg', 'png'].collect { format ->
                project.tasks.findByName(exportTaskNameFor(taskName, format))
            }
        }

        Map<String, Deck2Pdf.Extension> createdExtensions = TASK_TYPES.collectEntries { k, v ->
            [k, v.extensionType ? sourceTasks[k].get().extensions.findByName(Deck2Pdf.EXTENSION_NAME) : null]
        }

        then:
        exportTasks.size() == 3 * sourceTasks.size()

        and: 'Extensions are added to source tasks dependent on profile type'
        verifyAll {
            expectedExtensions.size() == createdExtensions.size()
            compareExtensionClasses(expectedExtensions, createdExtensions)
        }
    }

    String exportTaskNameFor(final String taskName, final String format) {
        "${taskName}To${format.capitalize()}"
    }

    boolean compareExtensionClasses(
        Map<String, Deck2Pdf.Extension> expected,
        Map<String, Deck2Pdf.Extension> got
    ) {
        got.findAll { name, extInstance ->
            Class extSuperClass = extInstance?.class?.superclass
            extSuperClass == expected[name]
        }.size() == expected.size()
    }
}