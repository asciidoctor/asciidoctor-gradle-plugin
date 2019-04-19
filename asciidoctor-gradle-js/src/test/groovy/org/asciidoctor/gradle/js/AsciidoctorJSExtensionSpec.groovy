package org.asciidoctor.gradle.js

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AsciidoctorJSExtensionSpec extends Specification {
    Project project = ProjectBuilder.builder().build()
    AsciidoctorJSExtension asciidoctorjs

    void setup() {
        project.apply plugin : 'org.asciidoctor.js.base'
        asciidoctorjs = project.extensions.getByType(AsciidoctorJSExtension)
    }

    void 'Can set a default diagram version'() {
        when:
        asciidoctorjs.useDocbook()

        then:
        asciidoctorjs.getDocbookVersion() == AsciidoctorJSExtension.DEFAULT_DOCBOOK_VERSION
        asciidoctorjs.requires.find { it.contains('docbook-converter') }
    }
}