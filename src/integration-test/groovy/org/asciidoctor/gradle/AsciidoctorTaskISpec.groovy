/*
 * Copyright 2013-2014 the original author or authors.
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
package org.asciidoctor.gradle

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.asciidoctor.Asciidoctor
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 *
 * @author Rob Winch
 */
class AsciidoctorTaskISpec extends Specification {

    private static final String ASCIIDOCTOR = 'asciidoctor'

    Project project

    def 'fopub backend renders pdf successfully'() {
        setup:
            project = ProjectBuilder.builder().withProjectDir(new File('samples/fopub')).build()
            project.buildDir = new File(project.buildDir, 'fopub-' + System.currentTimeMillis())
        when:
            Task task = project.tasks.add(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                backends = ['fopub','html5']
                options = [
                        attributes: [
                                icons: 'font',
                                'source-highlighter': 'prettify',
                                experimental: true,
                                copycss: true
                        ]
                ]
            }

            task.gititdone()
            File pdfFile = new File(project.buildDir,'asciidoc/index.pdf')
        then: 'verify the pdf is rendered correctly'
            // NOTE the output varies by OS
            pdfFile.length() > 100L
        cleanup:
            FileUtils.deleteDirectory(project.buildDir)
    }
}
