/*
 * Copyright 2013 - 2026 the original author or authors.
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

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DefaultAttributesSpec extends AsciidoctorjHtmlIntegrationSpecification {

    void setup() {
        writeHtmlBasedBuildFile()
        writeSource('src/docs/asciidoc/index.adoc', '''
        = Document

        include::{includedir}/_included.adoc[]
        '''.stripIndent())
        writeSource('src/docs/asciidoc/_included.adoc', 'This text is included.\n')
    }

    void 'includedir is the source directory'() {
        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(":${taskName}").outcome == SUCCESS
        fileContains(outputDir, 'index.html', 'This text is included.')
    }

    void 'includedir is the intermediate working directory when there are external sources'() {
        setup:
        writeSource('external/ext.adoc', '''
        = External

        include::{includedir}/ext/_part.adoc[]
        '''.stripIndent())
        writeSource('external/_part.adoc', 'This text comes from the external source.\n')
        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        externalSource {
            sourceDir = 'external'
            sources {
                include 'ext.adoc'
            }
            into 'ext'
        }
        missingIncludesAreFatal()
        """.stripIndent())

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(":${taskName}").outcome == SUCCESS
        fileContains(outputDir, 'index.html', 'This text is included.')
        fileContains(outputDir, 'ext/ext.html', 'This text comes from the external source.')
    }

    void 'includedir set in the build script overrides the default'() {
        setup:
        writeSource('other/_included.adoc', 'This text is included from another directory.\n')
        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        attributes {
            add('includedir', file('other').absolutePath)
        }
        """.stripIndent())

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(":${taskName}").outcome == SUCCESS
        fileContains(outputDir, 'index.html', 'This text is included from another directory.')
    }

    void 'includedir set in the document overrides the default'() {
        setup:
        writeSource('src/docs/asciidoc/index.adoc', '''
        = Document
        :includedir: other

        include::{includedir}/_included.adoc[]
        '''.stripIndent())
        writeSource('src/docs/asciidoc/other/_included.adoc', 'The document sets its own includedir.\n')

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(":${taskName}").outcome == SUCCESS
        fileContains(outputDir, 'index.html', 'The document sets its own includedir.')
    }

    private void writeSource(String path, String content) {
        final file = new File(projectDir, path)
        file.parentFile.mkdirs()
        file.text = content
    }
}
