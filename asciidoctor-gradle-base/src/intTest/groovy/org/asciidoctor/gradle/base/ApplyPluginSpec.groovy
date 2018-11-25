package org.asciidoctor.gradle.base

import org.asciidoctor.gradle.base.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult

class ApplyPluginSpec extends FunctionalSpecification {

    void 'Apply the base plugin in a Kotlin DSL'() {

        given:
        getKotlinBuildFile('')

        when:
        BuildResult result = getGradleRunnerForKotlin(['tasks','-s']).build()

        then:
        noExceptionThrown()
    }

    void 'Apply the base plugin in a Groovy DSL'() {

        given:
        getGroovyBuildFile('', 'base')

        when:
        BuildResult result = getGradleRunner(['tasks']).build()

        then:
        noExceptionThrown()
    }
}