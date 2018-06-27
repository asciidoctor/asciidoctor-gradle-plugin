/*
 * Copyright 2013-2018 the original author or authors.
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
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Issue
import spock.lang.Unroll

@java.lang.SuppressWarnings('NoWildcardImports')
import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.*

@SuppressWarnings(['MethodName', 'DuplicateStringLiteral'])
class ExtensionsFunctionalSpec extends FunctionalSpecification {

    static final List DEFAULT_ARGS = ['asciidoctor', '-s']
    static final String ASCIIDOC_INLINE_EXTENSIONS_FILE = 'inlineextensions.asciidoc'

    static final String GLOBAL = 'project'
    static final String LOCAL = 'task'

    void setup() {
        createTestProject('extensions')
    }

    @Unroll
    void 'Extension can be applied via closure (Asciidoctorj=#version,mode=#processMode)'() {
        given:
        getBuildFile(
            processMode, version, """
asciidoctor {
    asciidoctorj {
         extensions {
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
        version   | processMode
        SERIES_15 | 'JAVA_EXEC'
        SERIES_16 | 'JAVA_EXEC'
        SERIES_15 | 'IN_PROCESS'
        SERIES_16 | 'IN_PROCESS'
        SERIES_15 | 'OUT_OF_PROCESS'
        SERIES_16 | 'OUT_OF_PROCESS'
    }

    @Unroll
    void 'Extension can be applied from a string (Asciidoctorj=#version)'() {
        given:
        getBuildFile(
            'IN_PROCESS', version, """
asciidoctor {

    asciidoctorj {
        extensions '''
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

        when:
        runner.build()
        File resultFile = new File(testProjectDir.root, "build/docs/asciidoc/${ASCIIDOC_INLINE_EXTENSIONS_FILE.replaceFirst('asciidoc', 'html')}")

        then: 'content is generated as HTML and XML'
        resultFile.exists()
        resultFile.text.contains('WRITE THIS IN UPPERCASE')
        resultFile.text.contains('and write this in lowercase')

        where:
        version << [SERIES_15, SERIES_16]
    }

    @Unroll
    void 'Extension can be applied from file (Asciidoctorj=#version)'() {
        given:
        getBuildFile(
            'OUT_OF_PROCESS', version, """
asciidoctor {
    asciidoctorj {
        extensions file('src/docs/asciidoc/blockMacro.groovy')
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
        version << [SERIES_15, SERIES_16]
    }

    @Unroll
    void 'Fail build if extension fails to compile (Asciidoctorj=#version)'() {
        given:
        getBuildFile(
            'IN_PROCESS', version, """
asciidoctor {
    asciidoctorj {
        extensions '''
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
        version << [SERIES_15, SERIES_16]
    }

    @Unroll
    void 'Asciidoctor extension is defined in #extScope, version config is on #verScope'() {
        given:
        String extDSL = '''asciidoctorj.extensions file('src/docs/asciidoc/blockMacro.groovy')'''
        getBuildFile(
            'IN_PROCESS', version, """
${extScope == GLOBAL ? extDSL : ''}

asciidoctor {
    ${extScope == LOCAL ? extDSL : ''}
}
""", verScope == GLOBAL)
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)

        when:
        runner.build()
        File resultFile = new File(testProjectDir.root, "build/docs/asciidoc/${ASCIIDOC_INLINE_EXTENSIONS_FILE.replaceFirst('asciidoc', 'html')}")

        then: 'content is generated as HTML and XML'
        resultFile.exists()
        resultFile.text.contains('WRITE THIS IN UPPERCASE')
        resultFile.text.contains('and write this in lowercase')

        where:
        version = SERIES_16

        and:
        extScope | verScope
        LOCAL    | LOCAL
        LOCAL    | GLOBAL
        GLOBAL   | LOCAL
        GLOBAL   | GLOBAL
    }

    @Unroll
    @Issue('This test was forward-ported from https://github.com/asciidoctor/asciidoctor-gradle-plugin/pull/238 - a PR by Rene Groeschke')
    def "Postprocessor extensions are registered and preserved across multiple builds (Asciidoctorj=#version)"() {
        given: 'A build file that declares extensions'

        getBuildFile('IN_PROCESS', version, """
        asciidoctorj {
            extensions {
                postprocessor { document, output ->
                    return "Hi, Mom" + output 
                }
            }
        }
        """)

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
        version << [SERIES_15, SERIES_16]
    }

    File getBuildFile(
        final String processMode, final String version, final String extraContent, boolean configureGlobally = false) {

        String versionConfig = """
asciidoctorj {
    version = '${version}'
    groovyDslVersion = '${version == SERIES_15 ? GROOVYDSL_SERIES_15 : GROOVYDSL_SERIES_16}'
}
"""

        getJvmConvertBuildFile("""

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
