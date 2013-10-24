package org.asciidoctor.gradle

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.asciidoctor.Asciidoctor
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 *
 * @author Rob Winch
 */
class AsciidoctorTaskISpec extends Specification {

    private static final String ASCIIDOCTOR = 'asciidoctor'

    Project project

    def 'fopdf backend renders pdf successfully'() {
        setup:
            project = ProjectBuilder.builder().withProjectDir(new File('samples/fopdf')).build()
            project.buildDir = new File(project.buildDir, 'fopdf-' + System.currentTimeMillis())
        when:
            Task task = project.tasks.add(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                backends = ['fopdf','html5']
                options = [
                        attributes: [
                                icons: 'font',
                                'source-highlighter': 'prettify',
                                experimental: true,
                                copycss: true
                        ]
                ]
            }

            task.gititdone()
            File pdfFile = new File(project.buildDir,'asciidoc/index.pdf')
        then: 'verify the pdf is rendered correctly'
            // NOTE the output varies by OS
            pdfFile.length() > 100L
        cleanup:
            FileUtils.deleteDirectory(project.buildDir)
    }
}
