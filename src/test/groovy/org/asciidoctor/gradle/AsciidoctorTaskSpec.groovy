package org.asciidoctor.gradle

import org.asciidoctor.Asciidoctor
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Asciidoctor task specification
 *
 * @author Benjamin Muschko
 */
class AsciidoctorTaskSpec extends Specification {
    Project project
    Asciidoctor mockAsciidoctor
    File rootDir

    def setup() {
        project = ProjectBuilder.builder().build()
        mockAsciidoctor = Mock(Asciidoctor)
        rootDir = new File('.')
    }

    def "Adds asciidoctor task with unsupported backend"() {
        expect:
            project.tasks.findByName('asciidoctor') == null
        when:
            Task task = project.tasks.add(name: 'asciidoctor', type: AsciidoctorTask) {
                backend = 'unknown'
            }

            task.gititdone()
        then:
            thrown(InvalidUserDataException)
    }

    def "Adds asciidoctor task with supported backend"() {
        expect:
            project.tasks.findByName('asciidoctor') == null
        when:
                println new File('.').absolutePath
            Task task = project.tasks.add(name: 'asciidoctor', type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, 'build/resources/test/src/asciidoc')
                outputDir = new File(rootDir, 'build/asciidoc')
            }

            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    def "Adds asciidoctor task throws exception"() {
        expect:
            project.tasks.findByName('asciidoctor') == null
        when:
            Task task = project.tasks.add(name: 'asciidoctor', type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, 'build/resources/test/src/asciidoc')
                outputDir = new File(rootDir, 'build/asciidoc')
            }

            task.gititdone()
        then:
           mockAsciidoctor.renderFile(_, _) >> { throw new RuntimeException() }
           thrown(GradleException)
    }
}
