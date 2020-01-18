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
package org.asciidoctor.gradle.jvm

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.generators.AsciidoctorjVersionProcessModeGenerator
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Issue
import spock.lang.Timeout
import spock.lang.Unroll

@java.lang.SuppressWarnings('NoWildcardImports')
import static org.asciidoctor.gradle.testfixtures.AsciidoctorjTestVersions.*

@SuppressWarnings(['LineLength'])
class ExtensionsFunctionalSpec extends FunctionalSpecification {

    static final List DEFAULT_ARGS = ['asciidoctor', '-s', '-i']
    static final String ASCIIDOC_INLINE_EXTENSIONS_FILE = 'inlineextensions.asciidoc'

    static final String GLOBAL = 'project'
    static final String LOCAL = 'task'

    void setup() {
        createTestProject('extensions')
    }

    @Timeout(value = 90)
    @Unroll
    @SuppressWarnings('GStringExpressionWithinString')
    void 'Extension can be applied via closure (#model)'() {
        given:
        getBuildFile(
            model.processMode, model.version, """
asciidoctor {
    asciidoctorj {
         docExtensions {
            block(name: "BIG", contexts: [":paragraph"]) {
                parent, reader, attributes ->
                def upperLines = reader.readLines()*.toUpperCase()
                .inject("") {a, b -> a + '\\\\n' + b}

                createBlock(parent, "paragraph", [upperLines], attributes, [:])
            }
            block("small") {
                parent, reader, attributes ->
                def lowerLines = reader.readLines()*.toLowerCase()
                .inject("") {a, b -> a + '\\\\n' + b}

                createBlock(parent, "paragraph", [lowerLines], attributes, [:])
            }
         }
    }
}
""")

        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)

        when:
        runner.build()
        File resultFile = new File(testProjectDir.root, "build/docs/asciidoc/${ASCIIDOC_INLINE_EXTENSIONS_FILE.replaceFirst('asciidoc', 'html')}")

        then: 'content is generated as HTML and XML'
        resultFile.exists()
        resultFile.text.contains('WRITE THIS IN UPPERCASE')
        resultFile.text.contains('and write this in lowercase')

        where:
        model << AsciidoctorjVersionProcessModeGenerator.get()
    }

    @Unroll
    @Timeout(value = 90)
    void 'Extension can be applied from a string (#model)'() {
        given:
        getBuildFile(
            model.processMode, model.version, """
asciidoctor {

    asciidoctorj {
        docExtensions '''
block(name: 'BIG', contexts: [':paragraph']) {
        parent, reader, attributes ->
        def upperLines = reader.readLines()
        .collect {it.toUpperCase()}
        .inject('') {a, b -> "\${a}\\\n\${b}"}

        createBlock(parent, "paragraph", [upperLines], attributes, [:])
}
block('small') {
        parent, reader, attributes ->
        def lowerLines = reader.readLines()
        .collect {it.toLowerCase()}
        .inject('') {a, b -> "\${a}\\\n\${b}"}

        createBlock(parent, 'paragraph', [lowerLines], attributes, [:])
}
'''
    }
}
""")
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)
        if (model.processMode != 'JAVA_EXEC') {
            runner.withDebug(false)
        }

        when:
        runner.build()
        File resultFile = new File(testProjectDir.root, "build/docs/asciidoc/${ASCIIDOC_INLINE_EXTENSIONS_FILE.replaceFirst('asciidoc', 'html')}")

        then: 'content is generated as HTML and XML'
        resultFile.exists()
        resultFile.text.contains('WRITE THIS IN UPPERCASE')
        resultFile.text.contains('and write this in lowercase')

        where:
        model << AsciidoctorjVersionProcessModeGenerator.get()
    }

    @Timeout(value = 90)
    @Unroll
    void 'Extension can be applied from file (#model)'() {
        given:
        getBuildFile(
            model.processMode, model.version, """
asciidoctor {
    asciidoctorj {
        docExtensions file('src/docs/asciidoc/blockMacro.groovy')
    }
}
""")
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)

        if (model.processMode != 'JAVA_EXEC') {
            runner.withDebug(false)
        }

        when:
        runner.build()
        File resultFile = new File(testProjectDir.root, "build/docs/asciidoc/${ASCIIDOC_INLINE_EXTENSIONS_FILE.replaceFirst('asciidoc', 'html')}")

        then: 'content is generated as HTML and XML'
        resultFile.exists()
        resultFile.text.contains('WRITE THIS IN UPPERCASE')
        resultFile.text.contains('and write this in lowercase')

        where:
        model << AsciidoctorjVersionProcessModeGenerator.get()
    }

    @Timeout(value = 90)
    @Unroll
    @Issue('This test was forward-ported from https://github.com/asciidoctor/asciidoctor-gradle-plugin/pull/238 - a PR by Rene Groeschke')
    void "Extensions are preserved across multiple builds (#model)"() {
        given: 'A build file that declares extensions'

        getBuildFile(
            model.processMode, model.version, '''
        asciidoctorj {
            docExtensions {
                postprocessor { document, output ->
                    return "Hi, Mom" + output
                }
            }
        }
        ''')

        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)
        File outputFile = new File(testProjectDir.root, 'build/docs/asciidoc/inlineextensions.html')

        when:
        BuildResult firstInvocationResult = runner.build()

        then:
        firstInvocationResult.task(':asciidoctor').outcome == TaskOutcome.SUCCESS
        outputFile.exists()
        outputFile.text.startsWith('Hi, Mom')

        when:
        new File(testProjectDir.root, 'src/docs/asciidoc/inlineextensions.asciidoc') << 'changes'
        final BuildResult secondInvocationResult = getGradleRunner(DEFAULT_ARGS).build()

        then:
        secondInvocationResult.task(':asciidoctor').outcome == TaskOutcome.SUCCESS
        outputFile.text.startsWith('Hi, Mom')

        where:
        model << AsciidoctorjVersionProcessModeGenerator.get()
    }

    @Timeout(value = 90)
    @Unroll
    void 'Fail build if extension fails to compile (#model)'() {
        given:
        getBuildFile(
            model.processMode, model.version, """
asciidoctor {
    asciidoctorj {
        docExtensions '''
            postprocessor {
                document, output ->
                    if (output.contains("blacklisted")) {
                        throw new IllegalArgumentException("Document contains a blacklisted word")
                    }
                }
            }
'''
    }
}
""")
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)

        when:
        BuildResult result = runner.buildAndFail()

        then:
        result.output.contains('org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed')

        where:
        model << AsciidoctorjVersionProcessModeGenerator.get()
    }

    @Timeout(value = 90)
    @Unroll
    void 'Asciidoctor extension is defined in #extScope, version config is on #verScope'() {
        given:
        String extDSL = '''asciidoctorj.docExtensions file('src/docs/asciidoc/blockMacro.groovy')'''
        getBuildFile(
            processMode, version, """
                ${extScope == GLOBAL ? extDSL : ''}

                asciidoctor {
                    ${extScope == LOCAL ? extDSL : ''}
                }
            """,
            verScope == GLOBAL
        )
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)

        when:
        runner.build()
        File resultFile = new File(
            testProjectDir.root,
            'build/docs/asciidoc/' + ASCIIDOC_INLINE_EXTENSIONS_FILE.replaceFirst('asciidoc', 'html')
        )

        then: 'content is generated as HTML and XML'
        resultFile.exists()
        resultFile.text.contains('WRITE THIS IN UPPERCASE')
        resultFile.text.contains('and write this in lowercase')

        where:
        version = SERIES_20
        processMode = 'JAVA_EXEC'

        and:
        extScope | verScope
        LOCAL    | LOCAL
        LOCAL    | GLOBAL
        GLOBAL   | LOCAL
        GLOBAL   | GLOBAL
    }

    File getBuildFile(
        final String processMode, final String version, final String extraContent, boolean configureGlobally = false) {

        String versionConfig = """
            asciidoctorj {
                version = '${version}'
                modules.groovyDsl.version = '${GROOVYDSL_SERIES_20}'
            }
        """

        getJvmConvertGroovyBuildFile("""
            ${configureGlobally ? versionConfig : ''}

            asciidoctor {
                inProcess ${processMode}
                sourceDir 'src/docs/asciidoc'
                sources {
                    include '${ASCIIDOC_INLINE_EXTENSIONS_FILE}'
                }

            ${configureGlobally ? '' : versionConfig}

            }

            ${extraContent}
        """
        )
    }
}
