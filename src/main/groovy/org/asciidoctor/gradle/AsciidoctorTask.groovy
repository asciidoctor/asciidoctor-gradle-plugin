/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidoctor.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildException

/**
 * @author Noam Tenne
 * @author Andres Almiray
 */
class AsciidoctorTask extends DefaultTask {
    @Input File sourceDir
    @OutputDirectory File outputDir
    @Input String backend

    AsciidoctorTask() {
        sourceDir = project.file('src/asciidoc')
        outputDir = new File("${project.buildDir}/asciidoc")
        backend = 'html5'
    }

    @TaskAction
    void gititdone() {
        AsciidoctorWorker worker = new AsciidoctorWorker(
            sourceDir: sourceDir,
            outputDir: outputDir,
            backend: backend
        )

        try {
            worker.execute()
        } catch (Exception e) {
            project.getLogger().error('Error running ruby script', e)
            throw new BuildException('Error running ruby script', e)
        }
    }
}
