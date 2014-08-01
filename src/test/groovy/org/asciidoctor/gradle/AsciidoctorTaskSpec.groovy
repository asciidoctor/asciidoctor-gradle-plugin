/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.gradle

import org.asciidoctor.SafeMode
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Asciidoctor task specification
 *
 * @author Benjamin Muschko
 * @author Stephan Classen
 * @author Marcus Fihlon
 */
class AsciidoctorTaskSpec extends Specification {
    private static final String ASCIIDOCTOR = 'asciidoctor'
    private static final String ASCIIDOC_RESOURCES_DIR = 'build/resources/test/src/asciidoc'
    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidoc'
    private static final String ASCIIDOC_SAMPLE_FILE = 'sample.asciidoc'
    private static final String ASCIIDOC_SAMPLE2_FILE = 'subdir/sample2.ad'
    private static final DOCINFO_FILE_PATTERN = ~/^(.+\-)?docinfo(-footer)?\.[^.]+$/

    Project project
    AsciidoctorProxy mockAsciidoctor
    File testRootDir
    File srcDir
    File outDir
    ByteArrayOutputStream systemOut

    def setup() {
        project = ProjectBuilder.builder().withName('test').build()
        mockAsciidoctor = Mock(AsciidoctorProxy)
        testRootDir = new File('.')
        srcDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
        outDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
        systemOut = new ByteArrayOutputStream()
        System.out = new PrintStream(systemOut)
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateNumberLiteral')
    def "Adds asciidoctor task with multiple backends"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                backends = [AsciidoctorBackend.DOCBOOK.id, AsciidoctorBackend.HTML5.id]
            }

            task.processAsciidocSources()
        then:
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.DOCBOOK.id})
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.HTML5.id})
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateNumberLiteral')
    def "Adds asciidoctor task with multiple backends and single backend"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                backends = [AsciidoctorBackend.DOCBOOK.id, AsciidoctorBackend.HTML5.id]
                backend = AsciidoctorBackend.DOCBOOK5.id
            }

            task.processAsciidocSources()
        then:
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.DOCBOOK.id})
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.HTML5.id})
            systemOut.toString().contains('Both backend and backends were specified. backend will be ignored.')
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateNumberLiteral')
    def "Adds asciidoctor task with supported backend"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
            }

            task.processAsciidocSources()
        then:
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.HTML5.id})
            ! systemOut.toString().contains('deprecated')
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateNumberLiteral')
    def "Using setBackend should output a warning"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                backend = AsciidoctorBackend.DOCBOOK.id
            }

            task.processAsciidocSources()
        then:
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.DOCBOOK.id})
            systemOut.toString().contains('backend is deprecated and may not be supported in future versions. Please use backends instead.')
    }

    @SuppressWarnings('MethodName')
    def "Adds asciidoctor task throws exception"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
            }

            task.processAsciidocSources()
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
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
            }

            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Output warning when a sourceDocumentName was given"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
            }
        when:
            task.processAsciidocSources()
        then:
            systemOut.toString().contains('sourceDocumentName is deprecated and may not be supported in future versions. Please use sourceDocumentNames instead.')
    }

    @SuppressWarnings('MethodName')
    def "Output error when sourceDocumentName and sourceDocumentNames are given"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
                sourceDocumentNames = new SimpleFileCollection(new File(testRootDir, ASCIIDOC_SAMPLE_FILE))
            }
        when:
            task.processAsciidocSources()
        then:
            systemOut.toString().contains('Both sourceDocumentName and sourceDocumentNames were specified. sourceDocumentName will be ignored.')
    }

    @SuppressWarnings('MethodName')
    def "Source documents in directories end up in the corresponding output directory"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE2_FILE), { it.to_dir == new File(task.outputDir, 'subdir').absolutePath })
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), { it.to_dir == task.outputDir.absolutePath })
            0 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Should support String value for attributes option"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
                options = [
                  attributes: 'toc=right source-highlighter=coderay'
                ]
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    def "Should support GString value for attributes option"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
                def attrs = 'toc=right source-highlighter=coderay'
                options = [
                  attributes: "$attrs"
                ]
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    def "Should support List value for attributes option"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
                def highlighter = 'coderay'
                options = [
                  attributes: ['toc=right', "source-highlighter=$highlighter"]
                ]
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(_, _)
    }

    @SuppressWarnings('MethodName')
    def "Throws exception when attributes option value is an unsupported type"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
                options = [
                  attributes: 23
                ]
            }
        when:
            task.processAsciidocSources()
        then:
            thrown(Exception)
    }

    @SuppressWarnings('MethodName')
    def "Setting baseDir results in the correct value being sent to Asciidoctor"() {
        given:
            File basedir = new File(testRootDir, 'my_base_dir')
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                baseDir = basedir
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), { it.base_dir == basedir.absolutePath })
    }

    @SuppressWarnings('MethodName')
    def "Omitting a value for baseDir results in sending the dir of the processed file"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE),
                    { it.base_dir == new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE).getParentFile().absolutePath })
    }

    @SuppressWarnings('MethodName')
    def "Setting baseDir to null results in no value being sent to Asciidoctor"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                baseDir = null
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), { !it.base_dir })
    }

    @SuppressWarnings('MethodName')
    def "Safe mode option is equal to level of SafeMode.UNSAFE by default"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.safe == SafeMode.UNSAFE.level
            })
    }

    @SuppressWarnings('MethodName')
    def "Safe mode configuration option as integer is honored"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                options = [
                    safe: SafeMode.SERVER.level
                ]
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.safe == SafeMode.SERVER.level
            })
    }

    @SuppressWarnings('MethodName')
    def "Safe mode configuration option as string is honored"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                options = [
                    safe: 'server'
                ]
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.safe == SafeMode.SERVER.level
            })
    }

    @SuppressWarnings('MethodName')
    def "Safe mode configuration option as enum is honored"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                options = [
                    safe: SafeMode.SERVER
                ]
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.safe == SafeMode.SERVER.level
            })
    }

    @SuppressWarnings('MethodName')
    def "Attributes projectdir and rootdir are always set to relative dirs of the processed file"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.attributes.projectdir == AsciidoctorUtils.getRelativePath(project.projectDir, new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE).getParentFile())
                it.attributes.rootdir == AsciidoctorUtils.getRelativePath(project.rootDir, new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE).getParentFile())
            })
    }

    @SuppressWarnings('MethodName')
    def "Docinfo files are not copied to target directory"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), _)
            !outDir.listFiles({ !it.directory && !(it.name =~ DOCINFO_FILE_PATTERN) } as FileFilter)
    }

    @SuppressWarnings('MethodName')
    def "Project coordinates are set automatically as attributes"() {
        given:
            project.version = '1.0.0-SNAPSHOT'
            project.group = 'com.acme'
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.attributes.'project-name' == 'test' &&
                it.attributes.'project-group' == 'com.acme' &&
                it.attributes.'project-version' == '1.0.0-SNAPSHOT'
            })
    }

    @SuppressWarnings('MethodName')
    def "Override project coordinates with explicit attributes"() {
        given:
            project.version = '1.0.0-SNAPSHOT'
            project.group = 'com.acme'
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                options = [
                   attributes: [
                        'project-name': 'awesome',
                        'project-group': 'unicorns',
                        'project-version': '1.0.0.Final'
                    ]
                ]
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.attributes.'project-name' == 'awesome' &&
                    it.attributes.'project-group' == 'unicorns' &&
                    it.attributes.'project-version' == '1.0.0.Final'
            })
    }

    @SuppressWarnings('MethodName')
    def "Should support a single source document if a name is given"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(ASCIIDOC_RESOURCES_DIR, ASCIIDOC_SAMPLE_FILE)
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE),_ )
    }

    @SuppressWarnings('MethodName')
    def "Should support multiple source documents if names are given"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentNames = project.files(
                    "${testRootDir}/${ASCIIDOC_SAMPLE_FILE}",
                    "${testRootDir}/${ASCIIDOC_SAMPLE2_FILE}",)
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE),_ )
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE2_FILE),_ )
    }
}
