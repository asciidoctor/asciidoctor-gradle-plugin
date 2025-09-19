package org.asciidoctor.gradle.model5.jvm

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
            'org.asciidoctor.jvm.base',
            'org.asciidoctor.jvm',
            'org.asciidoctor.jvm.pdf',
            'org.asciidoctor.jvm.revealjs',
            'org.asciidoctor.jvm.gems',
            'org.asciidoctor.jvm.diagram',
            'org.asciidoctor.jvm.kroki'
        ]
    }
}
