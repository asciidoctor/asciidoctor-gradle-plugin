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
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Issue

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class UpToDateSpec extends AsciidoctorjHtmlIntegrationSpecification {

    void setup() {
        writeHtmlBasedBuildFile()
    }

    void 'Changes to source files will cause rebuild'() {
        setup:
        copyTestProject('normal')
        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        missingIncludesAreFatal()
        """.stripIndent())

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result1.task(":${taskName}").outcome == SUCCESS

        when:
        final result2 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result2.task(":${taskName}").outcome == UP_TO_DATE

        when:
        new File(projectDir,'src/docs/asciidoc/sample.asciidoc') << 'One more line'
        final result3 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result3.task(":${taskName}").outcome == SUCCESS

        when:
        final result4 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result4.task(":${taskName}").outcome == UP_TO_DATE
    }

    @Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/599')
    void 'Changes to secondary sources will cause rebuild'() {
        setup:
        copyTestProject('issue-599-secondary-sources')
        configureSourceSetGroovy(DEFAULT_PUBLICATION, """
        sources {
            include '**/secondary.adoc'
        }
        baseDir {
            baseDirFollowsSourceDir()
        }
        attributes {
            add('includedir',  '.')
        }
        missingIncludesAreFatal()
        """.stripIndent())

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result1.task(":${taskName}").outcome == SUCCESS

        when:
        final result2 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result2.task(":${taskName}").outcome == UP_TO_DATE

        when:
        new File(projectDir,'src/docs/asciidoc/subdir/include.adoc') << 'One more line'
        final result3 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result3.task(":${taskName}").outcome == SUCCESS

        when:
        final result4 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result4.task(":${taskName}").outcome == UP_TO_DATE
    }
}