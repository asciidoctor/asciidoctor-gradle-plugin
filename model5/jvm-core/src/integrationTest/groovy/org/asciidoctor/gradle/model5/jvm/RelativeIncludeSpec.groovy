/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.jvm

import org.asciidoctor.gradle.model5.jvm.testfixtures.AsciidoctorjHtmlIntegrationSpecification
import spock.lang.Issue

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION

/**
 * @author Lari Hotari
 * @author Schalk W. Cronjé
 */
class RelativeIncludeSpec extends AsciidoctorjHtmlIntegrationSpecification {
    void setup() {
        writeHtmlBasedBuildFile()
        copyTestProject('relative-include')
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/454')
    void 'Includes are relative in source sub directories'() {

        setup:
        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        sources 'nested/sample.adoc'
        baseDir.baseDirFollowSourceFiles()
        """.stripIndent())

        when:
        getGradleRunner(IS_GROOVY_DSL, [taskName, '-s']).build()

        then:
        fileContains(outputDir, 'nested/sample.html', 'This is from _nested-include.adoc file.')
    }
}
