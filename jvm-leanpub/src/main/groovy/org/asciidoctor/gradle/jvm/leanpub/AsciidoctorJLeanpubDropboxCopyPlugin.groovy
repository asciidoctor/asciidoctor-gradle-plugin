/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.jvm.leanpub

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Adds a task to copy Leanpub task output to a Dropbox folder.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0.0
 */
@CompileStatic
class AsciidoctorJLeanpubDropboxCopyPlugin implements Plugin<Project> {
    public final static String LEANPUB_TASK_NAME = AsciidoctorJLeanpubPlugin.TASK_NAME
    public final static String COPY_TASK_NAME = "copy${LEANPUB_TASK_NAME.capitalize()}ToDropbox"

    @Override
    void apply(Project project) {
        project.pluginManager.apply(AsciidoctorJLeanpubPlugin)
        final leanpubTask = project.tasks.named(LEANPUB_TASK_NAME, AsciidoctorLeanpubTask)
        final leanpubSourceDir = leanpubTask.map {
            new File(it.outputDir, 'manuscript')
        }

        project.tasks.register(COPY_TASK_NAME, DropboxCopyTask) { t ->
            t.dependsOn(leanpubTask)
            t.sourceDir = leanpubSourceDir
        }
    }
}
