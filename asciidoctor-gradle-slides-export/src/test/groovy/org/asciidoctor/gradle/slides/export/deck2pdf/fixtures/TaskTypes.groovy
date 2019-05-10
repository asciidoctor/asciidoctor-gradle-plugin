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
package org.asciidoctor.gradle.slides.export.deck2pdf.fixtures

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.slides.Profile
import org.asciidoctor.gradle.base.slides.SlidesToExportAware
import org.asciidoctor.gradle.slides.export.deck2pdf.Deck2Pdf
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.ysb33r.grolifant.api.TaskProvider

/** Task types for running Deck2PDF conversions to specific formats.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class TaskTypes {

    public static final Map<String, Descriptor> TASK_TYPES = [
        rubanFakeTask : new Descriptor(
            taskType: RubanTask,
            extensionType: Deck2Pdf.RubanExtension
        ),
        dzFakeTask    : new Descriptor(
            taskType: DzTask
        ),
        revealFakeTask: new Descriptor(
            taskType: RevealTask,
            extensionType:  Deck2Pdf.RevealJsExtension
        )
    ]

    static class Descriptor {
        Class taskType
        Class extensionType
    }

    static class DzTask extends DefaultTask implements SlidesToExportAware {
        @Override
        Profile getProfile() {
            Profile.DZ
        }
    }

    static class RubanTask extends DefaultTask implements SlidesToExportAware {
        @Override
        Profile getProfile() {
            Profile.RUBAN
        }
    }

    static class RevealTask extends DefaultTask implements SlidesToExportAware {
        @Override
        Profile getProfile() {
            Profile.REVEAL_JS
        }
    }

    static Map<String, TaskProvider> registerSourceTasks(Project project) {
        TASK_TYPES.collectEntries {
            name, descriptor ->
                [name, TaskProvider.registerTask(project, name, descriptor.taskType)]
        } as Map<String, TaskProvider>
    }
}
