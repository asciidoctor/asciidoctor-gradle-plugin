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

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

/**
 * Source patterns supplied by a {@code ValueSource}.
 *
 * The configuration cache does not resolve a value source while storing, so a provider built from
 * one is first evaluated after the cache is restored. Anything the provider's closure captured has
 * been through serialisation by then, which is not true of a statically configured pattern set.
 *
 * @since 5.0.3
 */
class SourcePatternsFromValueSourceSpec extends AsciidoctorjHtmlIntegrationSpecification {

    void setup() {
        writeHtmlBasedBuildFile()
    }

    void 'Source patterns from a value source are resolved when the configuration cache is enabled'() {
        setup:
        copyTestProject('normal')
        buildFile << '''
        import org.gradle.api.provider.ValueSource
        import org.gradle.api.provider.ValueSourceParameters
        import org.gradle.api.tasks.util.PatternSet

        abstract class PageLister implements ValueSource<List<String>, ValueSourceParameters.None> {
            @Override
            List<String> obtain() {
                ['sample.asciidoc']
            }
        }

        tasks.named('asciidoctorHtml') {
            sourcePatterns = providers.of(PageLister) {}.map { new PatternSet().include(it) }
        }
        '''.stripIndent()

        when: 'the build runs with the configuration cache enabled'
        final result = getGradleRunnerConfigCache(IS_GROOVY_DSL, [taskName]).build()

        then: 'the patterns are resolved after the cache is restored and the sources are converted'
        result.task(":${taskName}").outcome == SUCCESS
        fileExists(outputDir, 'sample.html')
        result.output.contains('Configuration cache entry stored')
    }
}
