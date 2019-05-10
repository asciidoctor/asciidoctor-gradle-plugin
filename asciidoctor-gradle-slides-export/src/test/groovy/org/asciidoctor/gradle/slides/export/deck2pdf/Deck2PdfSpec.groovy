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
import spock.lang.Specification
import spock.lang.Unroll

import static org.asciidoctor.gradle.slides.export.deck2pdf.fixtures.TaskTypes.TASK_TYPES

class Deck2PdfSpec extends Specification {
    Project project = ProjectBuilder.builder().build()

    @Unroll
    void '#action attach extension to task if profile is #extType'() {
        when:
        Task task = project.tasks.create(taskName, taskType.value.taskType)
        Deck2Pdf.addExtensionTo(task, task.profile)

        then:
        noExtension || task.extensions.getByName(Deck2Pdf.EXTENSION_NAME)

        where:
        taskType << TASK_TYPES
        taskName = taskType.key
        action = taskType.value.extensionType ? 'Will' : 'Will not'
        noExtension = taskType.value.extensionType == null
        extType = taskType.key.replaceAll('FakeTask', '').toUpperCase()
    }
}