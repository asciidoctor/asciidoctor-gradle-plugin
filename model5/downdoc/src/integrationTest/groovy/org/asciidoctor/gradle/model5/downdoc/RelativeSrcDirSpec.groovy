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
package org.asciidoctor.gradle.model5.downdoc

import org.asciidoctor.gradle.model5.downdoc.internal.formatters.DefaultDowndocMarkdownOutputFormatter
import org.asciidoctor.gradle.model5.downdoc.plugins.DowndocPlugin
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification
import spock.lang.Issue

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/803')
class RelativeSrcDirSpec extends IntegrationSpecification {

    void 'gradle-relative-srcdir is the path from each document back to the source directory'() {
        setup:
        final taskName = 'asciidoctorMarkdown'
        final outputDir = new File(buildDir, 'docs/asciidoc/markdown')
        writeBasicBuildFileGroovy(['org.asciidoctor.downdoc'])
        addOutputToSourceSetGroovy(
            DowndocPlugin.DEFAULT_TOOLCHAIN,
            DefaultDowndocMarkdownOutputFormatter.DEFAULT_NAME,
            DEFAULT_PUBLICATION
        )
        writeSource('src/docs/asciidoc/index.adoc')
        writeSource('src/docs/asciidoc/sub/one.adoc')
        writeSource('src/docs/asciidoc/sub/deeper/two.adoc')

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result.task(":${taskName}").outcome == SUCCESS
        fileContains(outputDir, 'index.md', 'relative=[.]')
        fileContains(outputDir, 'sub/one.md', 'relative=[..]')
        fileContains(outputDir, 'sub/deeper/two.md', 'relative=[../..]')
    }

    private void writeSource(String path) {
        final file = new File(projectDir, path)
        file.parentFile.mkdirs()
        file.text = '= Document\n\nrelative=[{gradle-relative-srcdir}]\n'
    }
}
