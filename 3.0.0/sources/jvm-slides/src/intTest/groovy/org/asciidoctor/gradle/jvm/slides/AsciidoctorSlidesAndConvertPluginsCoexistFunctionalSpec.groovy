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

import org.asciidoctor.gradle.jvm.slides.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult

/**
 * @author Lari Hotari
 */
class AsciidoctorSlidesAndConvertPluginsCoexistFunctionalSpec extends FunctionalSpecification {
    void setup() {
        createTestProject('coexist')
    }

    void 'Run asciidoctor task when slides and convert plugin are used in the same project'() {
        given:
        createBuildFile()

        when:
        build('asciidoctor')

        then:
        verifyAll {
            new File(testProjectDir.root, 'build/docs/asciidoc/sample.html').exists()
        }
    }

    BuildResult build(String... targets) {
        getGradleRunner(targets.toList() + ['-s']).build()
    }

    File createBuildFile(String extraContent = '') {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
        plugins {
            id 'org.asciidoctor.jvm.revealjs'
            id 'org.asciidoctor.jvm.convert'
        }

        ${offlineRepositories}

        repositories {
            ruby.gems()
        }

        asciidoctorRevealJs {
            sourceDir 'src/docs/slides'
        }

        ${extraContent}
"""
        buildFile
    }
}

