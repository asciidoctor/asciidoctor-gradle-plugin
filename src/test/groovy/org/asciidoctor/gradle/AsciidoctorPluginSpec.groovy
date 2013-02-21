package org.asciidoctor.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Plugin specification.
 *
 * @author Benjamin Muschko
 */
class AsciidoctorPluginSpec extends Specification {
    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def "Applies plugin and checks default setup"() {
        expect:
            project.tasks.findByName('asciidoctor') == null
        when:
            project.apply plugin: AsciidoctorPlugin
        then:
            Task asciidoctorTask = project.tasks.findByName('asciidoctor')
            asciidoctorTask != null
            asciidoctorTask.group == 'Documentation'
            asciidoctorTask.sourceDir == project.file('src/asciidoc')
            asciidoctorTask.outputDir == new File(project.buildDir, 'asciidoc')
            asciidoctorTask.backend == AsciidoctorBackend.HTML5.id
    }
}
