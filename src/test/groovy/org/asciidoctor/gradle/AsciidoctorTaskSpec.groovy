package org.asciidoctor.gradle

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
    AsciidoctorWorker mockWorker

    def setup() {
        project = ProjectBuilder.builder().build()
        mockWorker = Mock(AsciidoctorWorker)
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
            Task task = project.tasks.add(name: 'asciidoctor', type: AsciidoctorTask) {
                worker = mockWorker
            }

            task.gititdone()
        then:
            1 * mockWorker.execute(project.file('src/asciidoc'), new File(project.buildDir, 'asciidoc'), AsciidoctorBackend.HTML5.id)
    }

    def "Adds asciidoctor task throws exception"() {
        expect:
            project.tasks.findByName('asciidoctor') == null
        when:
            Task task = project.tasks.add(name: 'asciidoctor', type: AsciidoctorTask) {
                worker = mockWorker
            }

            task.gititdone()
        then:
            mockWorker.execute(project.file('src/asciidoc'), new File(project.buildDir, 'asciidoc'), AsciidoctorBackend.HTML5.id) >> { throw new RuntimeException() }
            thrown(GradleException)
    }
}
