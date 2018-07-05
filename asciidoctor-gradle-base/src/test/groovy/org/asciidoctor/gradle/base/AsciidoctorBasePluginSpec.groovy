package org.asciidoctor.gradle.base

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AsciidoctorBasePluginSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    void 'Can apply the base plugin'() {
        when:
        project.allprojects {
            apply plugin: 'org.asciidoctor.base'
        }

        then:
        project.tasks.find { it.name == 'assemble' } != null
        noExceptionThrown()
    }
}