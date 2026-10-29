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
package org.asciidoctor.gradle.adocfmt

import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class AdocFmtPluginSpec extends IntegrationSpecification {

    public static final String TEST_SUBDIR = 'src/docs/adoc'
    public static final String ANTORA_TEST_SUBDIR = 'docs/antora'
    public static final String FMT_TASK = 'adocFmt'
    public static final String CHECK_TASK = 'adocFmtCheck'

    void 'Can check and format'() {
        setup:
        writeBuildFile()
        writeSource(TEST_SUBDIR)

        when:
        final result1 = getGradleRunner(true, ['check']).buildAndFail()

        then:
        result1.task(":${CHECK_TASK}").outcome == FAILED

        when:
        final result2 = getGradleRunner(true, [FMT_TASK]).build()

        then:
        result2.task(":${FMT_TASK}").outcome == SUCCESS

        when:
        final result3 = getGradleRunner(true, ['check']).build()

        then:
        result3.task(":${CHECK_TASK}").outcome == SUCCESS

        when:
        getGradleRunner(true, [FMT_TASK]).build()
        final result4 = getGradleRunner(true, [FMT_TASK]).build()

        then:
        result4.task(":${FMT_TASK}").outcome == UP_TO_DATE

        when:
        final result5 = getGradleRunner(true, [CHECK_TASK]).build()

        then:
        result5.task(":${CHECK_TASK}").outcome == UP_TO_DATE
    }

    void 'Can check and format Antora layouts'() {
        setup:
        writeBuildFile(true)
        writeSource("${ANTORA_TEST_SUBDIR}/modules/ROOT/pages")
        writeSource("${ANTORA_TEST_SUBDIR}/modules/anotherModule/pages")

        when:
        final result1 = getGradleRunner(true, ['check']).buildAndFail()

        then:
        result1.task(":${CHECK_TASK}").outcome == FAILED

        when:
        final result2 = getGradleRunner(true, [FMT_TASK]).build()

        then:
        result2.task(":${FMT_TASK}").outcome == SUCCESS

        when:
        final result3 = getGradleRunner(true, ['check']).build()

        then:
        result3.task(":${CHECK_TASK}").outcome == SUCCESS

        when:
        getGradleRunner(true, [FMT_TASK]).build()
        final result4 = getGradleRunner(true, [FMT_TASK]).build()

        then:
        result4.task(":${FMT_TASK}").outcome == UP_TO_DATE

        when:
        final result5 = getGradleRunner(true, [CHECK_TASK]).build()

        then:
        result5.task(":${CHECK_TASK}").outcome == UP_TO_DATE
    }

    private void writeBuildFile(boolean antoraMode = false) {
        buildFile.text = """
        import org.asciidoctor.gradle.adocfmt.tasks.*
        
        plugins {
            id 'org.asciidoctor.format'
        }
        
        tasks.register('${FMT_TASK}', AdocFmtTask ) {
            sourceDir = '${antoraMode ? ANTORA_TEST_SUBDIR : TEST_SUBDIR}'
            antoraMode = ${antoraMode}
        }
        
        tasks.register('${CHECK_TASK}', AdocFmtCheckTask ) {
            sourceDir = '${antoraMode ? ANTORA_TEST_SUBDIR : TEST_SUBDIR}'
            antoraMode = ${antoraMode}
        }
        """.stripIndent()
    }

    private void writeSource(String dir) {
        final srcDir = new File(projectDir, dir)
        srcDir.mkdirs()

        // Example is from https://ypfaff.github.io/adocfmt/getting-started.html
        new File(srcDir, 'test.adoc').text = '''
        = Tea guide

        ##  Brewing
        Green tea turns bitter in boiling water. Heat the water
        to 80 degrees.
        
        - Use one teaspoon per cup.
        
        - Steep for two minutes.
        
        
        
        ## Serving ##
        1. Pour the tea.
        2. Serve it hot.
        '''
    }
}
