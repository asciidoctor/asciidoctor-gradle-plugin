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
package org.asciidoctor.gradle.jvm.slides

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AsciidoctorRevealJSTaskSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    void 'Configure revealjs task'() {
        given:
        project.apply plugin: 'org.asciidoctor.jvm.revealjs.base'
        AsciidoctorJRevealJSTask revealjsTask = project.tasks.create('foo', AsciidoctorJRevealJSTask)

        when:
        revealjsTask.with {
            templateRelativeDir = 'foo'
            theme = 'argon'
            outputDir = 'build/docs/bar'

            revealjsOptions {
                progressBar = true
                slideNumber = 'c'
            }
        }

        revealjsTask.revealjsOptions(new Action<RevealJSOptions>() {
            @Override
            void execute(RevealJSOptions revealJSOptions) {
                revealJSOptions.mouseWheel = true
            }
        })

        then:
        verifyAll {
            revealjsTask.templateDir == project.file('build/docs/bar/foo')
            revealjsTask.theme == 'argon'
            revealjsTask.revealjsOptions.asAttributeMap.revealjs_mouseWheel == 'true'
            revealjsTask.revealjsOptions.asAttributeMap.revealjs_progress == 'true'
            revealjsTask.revealjsOptions.asAttributeMap.revealjs_slideNumber == 'c'
        }
    }
}
