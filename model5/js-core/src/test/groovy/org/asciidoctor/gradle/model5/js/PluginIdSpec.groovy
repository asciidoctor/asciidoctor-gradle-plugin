package org.asciidoctor.gradle.model5.js

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
            'org.asciidoctor.js.base',
            'org.asciidoctor.js',
            'org.asciidoctor.js.docbook',
            'org.asciidoctor.js.revealjs',
            'org.asciidoctor.js.kroki'
        ]
    }
}
