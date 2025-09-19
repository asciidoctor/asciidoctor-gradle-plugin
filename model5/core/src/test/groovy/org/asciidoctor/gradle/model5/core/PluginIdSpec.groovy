package org.asciidoctor.gradle.model5.core

import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification
import spock.lang.Unroll

class PluginIdSpec extends UnitTestSpecification {

    @Unroll
    void 'Can apply #pluginId'() {
        when:
        project.pluginManager.apply(pluginId)

        then:
        noExceptionThrown()

        where:
        pluginId << [
            'org.asciidoctor.core.base',
            'org.asciidoctor.core',
            'org.asciidoctor.themes'
        ]
    }
}
