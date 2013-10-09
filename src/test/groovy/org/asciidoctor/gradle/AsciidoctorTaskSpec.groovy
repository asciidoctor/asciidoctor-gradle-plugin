package org.asciidoctor.gradle

import org.asciidoctor.Asciidoctor
import org.gradle.api.GradleException
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
    private static final String ASCIIDOC_SAMPLE_FILE = 'sample.asciidoc'

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
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
                backend = 'unknown'
            }

            task.gititdone()
        then:
            org.asciidoctor.gradle.AsciidoctorBackend.isBuiltIn('unknown') == false
            2 * mockAsciidoctor.renderFile(_, _)
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
            2 * mockAsciidoctor.renderFile(_, _)
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
                sourceDocumentName = new File(rootDir, ASCIIDOC_SAMPLE_FILE)
            }

            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Source documents in directories end up in the corresponding output directory"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
            }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, 'subdir/sample2.ad'), { it.to_dir == new File(task.outputDir, 'subdir').absolutePath })
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), { it.to_dir == task.outputDir.absolutePath })
            0 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Should support String value for attributes option"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(rootDir, ASCIIDOC_SAMPLE_FILE)
                options = [
                  attributes: 'toc=right source-highlighter=coderay'
                ]
            }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Should support GString value for attributes option"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(rootDir, ASCIIDOC_SAMPLE_FILE)
                def attrs = 'toc=right source-highlighter=coderay'
                options = [
                  attributes: "$attrs"
                ]
            }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Should support List value for attributes option"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(rootDir, ASCIIDOC_SAMPLE_FILE)
                def highlighter = 'coderay'
                options = [
                  attributes: ['toc=right', "source-highlighter=$highlighter"]
                ]
            }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Throws exception when attributes option value is an unsupported type"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(rootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(rootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(rootDir, ASCIIDOC_SAMPLE_FILE)
                options = [
                  attributes: 23
                ]
            }
        when:
            task.gititdone()
        then:
            thrown(Exception)
    }
}
