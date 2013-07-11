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
    private static final String ASCIIDOCTOR = 'asciidoctor'
    private static final String ASCIIDOC_RESOURCES_DIR = 'build/resources/test/src/asciidoc'
    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidoc'

    Project project
    Asciidoctor mockAsciidoctor
    File rootDir

    def setup() {
        project = ProjectBuilder.builder().build()
        mockAsciidoctor = Mock(Asciidoctor)
        rootDir = new File('.')
    }

    @SuppressWarnings('MethodName')
    def "Adds asciidoctor task with unsupported backend"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.add(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                backend = 'unknown'
            }

            task.gititdone()
        then:
            thrown(InvalidUserDataException)
    }

    @SuppressWarnings('MethodName')
    def "Adds asciidoctor task with supported backend"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
            }

            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Adds asciidoctor task throws exception"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
            }

            task.gititdone()
        then:
           mockAsciidoctor.renderFile(_, _) >> { throw new IllegalArgumentException() }
           thrown(GradleException)
    }

    @SuppressWarnings('MethodName')
    def "Processes a single document given a value for sourceDocumentName"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(rootDir, 'sample.asciidoc')
            }

            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }
}
