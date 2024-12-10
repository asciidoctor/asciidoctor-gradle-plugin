package org.asciidoctor.gradle.model5.core

import org.asciidoctor.gradle.testfixtures.IntegrationSpecification
import org.gradle.testkit.runner.TaskOutcome

import static org.asciidoctor.gradle.model5.core.AsciidoctorCorePlugin.TOOLCHAIN_DISPLAY_TASK

class ExampleToolchainSpec extends IntegrationSpecification {

    void 'Can execute an asciidoctor task from a custom toolchain'() {
        setup:
        writeBuildFile()
        final taskName = 'myexampleAsciidoctorText'

        when:
        final result = getGradleRunner(IS_GROOVY_DSL,[taskName, '-s']).build()

        then:
        result.task(":${taskName}").outcome == TaskOutcome.SUCCESS
    }

    void writeBuildFile() {
        buildFile.text = '''
        plugins {
            id 'example.asciidoctor.toolchain'
        }
        
        asciidoc {
            publications {
                main {
                    outputFormats ('myexample.text')
                }
            }
        }
        '''
    }
}