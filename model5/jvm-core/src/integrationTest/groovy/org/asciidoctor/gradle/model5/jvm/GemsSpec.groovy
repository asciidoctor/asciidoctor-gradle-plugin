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

import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjTemplates
import org.asciidoctor.gradle.model5.jvm.internal.gems.GemUtils
import org.asciidoctor.gradle.model5.jvm.testfixtures.AsciidoctorjHtmlIntegrationSpecification
import spock.lang.IgnoreIf

import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

@IgnoreIf(reason = 'Gradle is offline', value = { IS_OFFLINE })
class GemsSpec extends AsciidoctorjHtmlIntegrationSpecification {

    void 'Will fail if templates are requested, but gem plugin has not been applied'() {
        setup:
        writeHtmlBasedBuildFile()
        copyTestProject('normal')
        buildFile << """
        asciidoc.toolchains.asciidoctorj.registeredOutputFormatters.html {
            templateDirs 'src/docs/asciidoc/templates/erb/html5'
        }
        """.stripIndent()

        when:
        getGradleRunner(IS_GROOVY_DSL, [taskName]).buildAndFail()

        then:
        noExceptionThrown()
    }

    void 'Can apply the #engine-based template'() {
        setup:
        writeGemUsingBuildFile()
        copyTestProject('normal')
        buildFile << """
        asciidoc.toolchains.asciidoctorj.registeredOutputFormatters.html {
            templateDirs 'src/docs/asciidoc/templates/${engine}/html5'
            useEngine '${engine}'
        }
        """.stripIndent()

        when:
        final result = getGradleRunner(IS_GROOVY_DSL, [taskName, '-s']).build()

        then:
        result.task(":asciidoctorGemsAsciidoctorjPrepare").outcome == SUCCESS
        result.task(":asciidoctorGemJarAsciidoctorjPrepare").outcome == SUCCESS
        result.task(":${taskName}").outcome == SUCCESS
        result.output.contains("Successfully installed ${engine == 'erb' ? 'tilt' : engine}-")

        where:
        engine << DefaultAsciidoctorjTemplates.SUPPORTED_TEMPLATE_ENGINES
    }

    void 'Prepare tasks are cacheable and relocatable'() {
        setup:
        final gemPrepare = GemUtils.nameForGemPrepareTask(DEFAULT_TOOLCHAIN)
        final jarPrepare = GemUtils.nameForJarPrepareTask(DEFAULT_TOOLCHAIN)

        writeGemUsingBuildFile()
        copyTestProject('ditaa')
        addBuildCacheAndCopyProjectToAlternateArea()

        when:
        final result1 = getGradleRunnerWithBuildAndConfigCache(IS_GROOVY_DSL, [jarPrepare]).build()

        then:
        result1.task(":${jarPrepare}").outcome == SUCCESS
        result1.task(":${gemPrepare}").outcome == SUCCESS

        when:
        final result2 = getGradleRunnerWithBuildAndConfigCache(IS_GROOVY_DSL, [jarPrepare]).build()

        then:
        result2.task(":${jarPrepare}").outcome == UP_TO_DATE
        result2.task(":${gemPrepare}").outcome == UP_TO_DATE

        when:
        final result3 = getGradleRunnerWithBuildAndConfigCache(IS_GROOVY_DSL, [jarPrepare, '-i'])
            .withProjectDir(alternateProjectDir).build()

        then:
        result3.task(":${jarPrepare}").outcome == SUCCESS
        result3.task(":${gemPrepare}").outcome == FROM_CACHE

        when:
        final result4 = getGradleRunnerWithBuildAndConfigCache(IS_GROOVY_DSL, [jarPrepare, '-i'])
            .withProjectDir(alternateProjectDir).build()

        then:
        result4.task(":${jarPrepare}").outcome == UP_TO_DATE
        result4.task(":${gemPrepare}").outcome == UP_TO_DATE
    }

    void 'Prepare tasks are not cached when GEMs change'() {
        setup:
        final gemPrepare = GemUtils.nameForGemPrepareTask(DEFAULT_TOOLCHAIN)
        final jarPrepare = GemUtils.nameForJarPrepareTask(DEFAULT_TOOLCHAIN)
        final propName = 'krokiVersion'

        writeGemUsingBuildFile([
            'org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjKrokiExtension'
        ])
        writeUseKrokiVersionFromProperty(propName)
        copyTestProject('ditaa')
        addBuildCacheAndCopyProjectToAlternateArea()

        when:
        final result1 = getGradleRunnerWithBuildAndConfigCache(IS_GROOVY_DSL, [
            jarPrepare,
            "-P${propName}=0.9.1"
        ]*.toString()).build()

        then:
        result1.task(":${jarPrepare}").outcome == SUCCESS
        result1.task(":${gemPrepare}").outcome == SUCCESS

        when:
        final result3 = getGradleRunnerWithBuildAndConfigCache(IS_GROOVY_DSL, [
            jarPrepare,
            "-P${propName}=0.9.1"
        ]*.toString()).withProjectDir(alternateProjectDir).build()

        then:
        result3.task(":${jarPrepare}").outcome == SUCCESS
        result3.task(":${gemPrepare}").outcome == FROM_CACHE

        when:
        final result2 = getGradleRunnerWithBuildAndConfigCache(IS_GROOVY_DSL, [
            jarPrepare,
            "-P${propName}=0.10.0"
        ]*.toString()).build()

        then:
        result2.task(":${jarPrepare}").outcome == SUCCESS
        result2.task(":${gemPrepare}").outcome == SUCCESS

        when:
        final result4 = getGradleRunnerWithBuildAndConfigCache(IS_GROOVY_DSL, [
            jarPrepare,
            "-P${propName}=0.10.0"
        ]*.toString()).withProjectDir(alternateProjectDir).build()

        then:
        result4.task(":${jarPrepare}").outcome == SUCCESS
        result4.task(":${gemPrepare}").outcome == FROM_CACHE
    }

    void writeUseKrokiVersionFromProperty(String propName) {
        buildFile << """
        asciidoc.toolchains.asciidoctorj.asciidocExtensions {
            kroki(AsciidoctorjKrokiExtension) {
                useVersion(grolifantOps.resolveProperty('${propName}'))
            }
        }
        """.stripIndent()
    }

    void writeGemUsingBuildFile(Iterable<String> imports = []) {
        writeHtmlBasedBuildFileWithImports(
            ['org.asciidoctor.jvm', 'org.asciidoctor.jvm.gems', 'org.asciidoctor.jvm.kroki'],
            imports
        )
        buildFile << '''
        repositories {
            ruby.gems()
        }
        '''.stripIndent()
    }
}
