package org.asciidoctor.gradle.compat

import org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AsciidoctorExtensionSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    void 'Default version should be latest of 1.5.x'() {
        given:
        project.allprojects {
            apply plugin : 'org.asciidoctor.convert'
        }

        expect:
        project.extensions.asciidoctorj.version == AsciidoctorjTestVersions.SERIES_15
    }

    void 'Default GroovyDSL version should be latest version'() {
        given:
        project.allprojects {
            apply plugin : 'org.asciidoctor.convert'
        }

        expect:
        project.extensions.asciidoctorj.groovyDslVersion == AsciidoctorjTestVersions.GROOVYDSL_SERIES_15
    }

}