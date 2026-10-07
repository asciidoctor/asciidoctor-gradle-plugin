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

import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.jvm.internal.PluginUtils
import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.testfixtures.AsciidoctorjHtmlIntegrationSpecification
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.TaskOutcome
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin
import spock.lang.Issue

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN
import static org.gradle.testkit.runner.TaskOutcome.FAILED
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

    void 'Changes to classpath will cause rebuild'() {
        setup:
        writeHtmlBasedBuildFileWithImports(['org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjGenericExtension'])
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
        // Add diagram to the classpath even if we are not going to use it.
        buildFile << """
        asciidoc.toolchains.asciidoctorj.asciidocExtensions {
            foo(AsciidoctorjGenericExtension) {
                useModule('${JvmModel.ASCIIDOCTORJ_DIAGRAM_DEPENDENCY}','${diagramVersion}')
            }
        }
        """.stripIndent()
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

    void 'Changes to #setting will cause rebuild'() {
        setup:
        copyTestProject('normal')
        buildFile << "\n${initial}\n"

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result1.task(":${taskName}").outcome == SUCCESS

        when:
        final result2 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result2.task(":${taskName}").outcome == UP_TO_DATE

        when:
        buildFile << "\n${change}\n"
        final result3 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result3.task(":${taskName}").outcome == SUCCESS

        when:
        final result4 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result4.task(":${taskName}").outcome == UP_TO_DATE

        where:
        setting                     | initial             | change
        'attributes'                | ''                  | "${SOURCE_SET}.attributes.add('product', 'Beta')"
        'embedded'                  | ''                  | "${HTML_FORMATTER}.embedded = true"
        // In safe mode the output directory has to be below the base directory
        'the safe mode'             | PROJECT_DIR_AS_BASE | "${TOOLCHAIN}.safeMode = 'SAFE'"
        'the base directory'        | ''                  | PROJECT_DIR_AS_BASE
        'per-file base directories' | ''                  | "${SOURCE_SET}.baseDir.baseDirFollowsSourceFiles()"
    }

    void 'Changes to attributes will update the output'() {
        setup:
        copyTestProject('normal')
        new File(projectDir, 'src/docs/asciidoc/sample.asciidoc') << '\nThe product is {product}.\n'
        buildFile << "\n${SOURCE_SET}.attributes.add('product', 'Alpha')\n"
        final outputFile = new File(outputDir, 'sample.html')

        when:
        getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        outputFile.text.contains('The product is Alpha.')

        when:
        buildFile << "\n${SOURCE_SET}.attributes.add('product', 'Beta')\n"
        getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        outputFile.text.contains('The product is Beta.')
    }

    void 'Adding a fatal warning pattern will cause rebuild'() {
        setup:
        copyTestProject('multiple-warnings')

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result1.task(":${taskName}").outcome == SUCCESS

        when:
        buildFile << "\n${SOURCE_SET}.fatalWarnings(~/section title out of sequence/)\n"
        final result2 = getGradleRunner(IS_GROOVY_DSL, [taskName]).buildAndFail()

        then:
        result2.task(":${taskName}").outcome == FAILED
    }

    void 'Changes to the backend of a generic output formatter will cause rebuild'() {
        setup:
        writeHtmlBasedBuildFileWithImports([GENERIC_OUTPUT_FORMATTER])
        copyTestProject('normal')
        buildFile << """
        ${TOOLCHAIN}.registeredOutputFormatters {
            custom(AsciidoctorjGenericOutputFormatter) {
                backend = 'html5'
            }
        }
        """.stripIndent()
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, 'custom', DEFAULT_PUBLICATION)
        final customTaskName = 'asciidoctorCustom'

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [customTaskName]).build()

        then:
        result1.task(":${customTaskName}").outcome == SUCCESS

        when:
        final result2 = getGradleRunner(IS_GROOVY_DSL, [customTaskName]).build()

        then:
        result2.task(":${customTaskName}").outcome == UP_TO_DATE

        when:
        buildFile << "\n${TOOLCHAIN}.registeredOutputFormatters.custom.backend = 'docbook5'\n"
        final result3 = getGradleRunner(IS_GROOVY_DSL, [customTaskName]).build()

        then:
        result3.task(":${customTaskName}").outcome == SUCCESS
    }

    void 'Changes to a scripted extension will cause rebuild'() {
        setup:
        writeHtmlBasedBuildFileWithImports([GROOVY_DSL_EXTENSION])
        copyTestProject('normal')
        buildFile << """
        ${TOOLCHAIN}.asciidocExtensions {
            groovydsl(AsciidoctorjGroovyDslExtension) {
                fromString('''${scriptedExtension('first')}''')
            }
        }
        """.stripIndent()

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result1.task(":${taskName}").outcome == SUCCESS

        when:
        final result2 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result2.task(":${taskName}").outcome == UP_TO_DATE

        when:
        buildFile << "\n${TOOLCHAIN}.asciidocExtensions.groovydsl.fromString('''${scriptedExtension('second')}''')\n"
        final result3 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result3.task(":${taskName}").outcome == SUCCESS
    }

    void 'Changes to a scripted extension file outside the source directory will cause rebuild'() {
        setup:
        writeHtmlBasedBuildFileWithImports([GROOVY_DSL_EXTENSION])
        copyTestProject('normal')
        final scriptFile = new File(projectDir, 'extensions/extension.groovy')
        scriptFile.parentFile.mkdirs()
        scriptFile.text = scriptedExtension('first')
        buildFile << """
        ${TOOLCHAIN}.asciidocExtensions {
            groovydsl(AsciidoctorjGroovyDslExtension) {
                fromFile('extensions/extension.groovy')
            }
        }
        """.stripIndent()

        when:
        final result1 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result1.task(":${taskName}").outcome == SUCCESS

        when:
        final result2 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result2.task(":${taskName}").outcome == UP_TO_DATE

        when:
        scriptFile.text = scriptedExtension('second')
        final result3 = getGradleRunner(IS_GROOVY_DSL, [taskName]).build()

        then:
        result3.task(":${taskName}").outcome == SUCCESS
    }

    private static String scriptedExtension(String blockName) {
        "block('${blockName}') { parent, reader, attributes -> " +
            "createBlock(parent, 'paragraph', reader.readLines(), attributes, [:]) }"
    }

    private void writeHtmlBasedBuildFileWithImports(List<String> imports) {
        writeBasicBuildFileGroovy(
            ['org.asciidoctor.jvm'],
            imports
        )
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjHtml5.DEFAULT_NAME, DEFAULT_PUBLICATION)
    }

    private String getDiagramVersion() {
        final props = loadPropertiesFile('asciidoctor5-jvm-core-plugin')
        props['asciidoctorj.diagram']
    }

    private static final String SOURCE_SET = 'asciidoc.publications.main.sourceSet'
    private static final String TOOLCHAIN = 'asciidoc.toolchains.asciidoctorj'
    private static final String HTML_FORMATTER = "${TOOLCHAIN}.registeredOutputFormatters.html"
    private static final String PROJECT_DIR_AS_BASE = "${SOURCE_SET}.baseDir.baseDirIsProjectDir()"
    private static final String GROOVY_DSL_EXTENSION =
        'org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjGroovyDslExtension'
    private static final String GENERIC_OUTPUT_FORMATTER =
        'org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjGenericOutputFormatter'
}