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

import org.asciidoctor.Asciidoctor
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
 */
class AsciidoctorTaskSpec extends Specification {
    private static final String ASCIIDOCTOR = 'asciidoctor'
    private static final String ASCIIDOC_RESOURCES_DIR = 'build/resources/test/src/asciidoc'
    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidoc'
    private static final String ASCIIDOC_SAMPLE_FILE = 'sample.asciidoc'
    private static final String ASCIIDOC_SAMPLE2_FILE = 'subdir/sample2.ad'
    private static final DOCINFO_FILE_PATTERN = ~/^(.+\-)?docinfo(-footer)?\.[^.]+$/

    Project project
    Asciidoctor mockAsciidoctor
    File testRootDir

    def setup() {
        project = ProjectBuilder.builder().withName('test').build()
        mockAsciidoctor = Mock(Asciidoctor)
        testRootDir = new File('.')
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateNumberLiteral')
    def "Adds asciidoctor task with multiple backends"() {
        when:
            Task task = project.tasks.add(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                backends = [AsciidoctorBackend.DOCBOOK.id, AsciidoctorBackend.HTML5.id]
            }

            task.gititdone()
        then:
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.DOCBOOK.id})
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.HTML5.id})
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateNumberLiteral')
    def "Adds asciidoctor task with supported backend"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
            }
        when:
            task.gititdone()
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
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
    @SuppressWarnings('DuplicateStringLiteral')
    def "Should support GString value for attributes option"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
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
    @SuppressWarnings('DuplicateStringLiteral')
    def "Should support List value for attributes option"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(testRootDir, ASCIIDOC_SAMPLE_FILE)
                options = [
                  attributes: 23
                ]
            }
        when:
            task.gititdone()
        then:
            thrown(Exception)
    }

    @SuppressWarnings('MethodName')
    def "Setting baseDir results in the correct value being sent to Asciidoctor"() {
        given:
            File basedir = new File(testRootDir, 'my_base_dir')
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                baseDir = basedir
            }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), { it.base_dir == basedir.absolutePath })
    }

    @SuppressWarnings('MethodName')
    def "Omitting a value for baseDir results in default value being sent to Asciidoctor"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
        }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), { it.base_dir == project.projectDir.absolutePath })
    }

    @SuppressWarnings('MethodName')
    def "Setting baseDir to null results in no value being sent to Asciidoctor"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                baseDir = null
            }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), { !it.base_dir })
    }

    @SuppressWarnings('MethodName')
    def "Safe mode option is equal to level of SafeMode.UNSAFE by default"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
        }
        when:
            task.gititdone()
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                options = [
                    safe: SafeMode.SERVER.level
                ]
        }
        when:
            task.gititdone()
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                options = [
                    safe: 'server'
                ]
        }
        when:
            task.gititdone()
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                options = [
                    safe: SafeMode.SERVER
                ]
        }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.safe == SafeMode.SERVER.level
            })
    }

    @SuppressWarnings('MethodName')
    def "Attributes projectdir and rootdir are always set"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
        }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), {
                it.attributes.projectdir == project.projectDir.absolutePath &&
                it.attributes.rootdir == project.rootDir.absolutePath
            })
    }

    @SuppressWarnings('MethodName')
    def "Docinfo files are not copied to target directory"() {
        given:
            File outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
        }
        when:
            task.gititdone()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE), _)
            !outputDir.listFiles({ !it.directory && !(it.name =~ DOCINFO_FILE_PATTERN) } as FileFilter)
    }

    @SuppressWarnings('MethodName')
    def "Project coordinates are set automatically as attributes"() {
        given:
        project.version = '1.0.0-SNAPSHOT'
        project.group = 'com.acme'
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            asciidoctor = mockAsciidoctor
            sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
            outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
        }
        when:
            task.gititdone()
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
            sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
            outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
            options = [
                attributes: [
                    'project-name': 'awesome',
                    'project-group': 'unicorns',
                    'project-version': '1.0.0.Final'
                ]
            ]
        }
        when:
        task.gititdone()
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
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentName = new File(ASCIIDOC_RESOURCES_DIR, ASCIIDOC_SAMPLE_FILE)
            }

        when:
            task.gititdone()

        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE),_ )
    }

    @SuppressWarnings('MethodName')
    def "Should support multiple source documents if names are given"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
                outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
                sourceDocumentNames = project.files(
                    "${testRootDir}/${ASCIIDOC_SAMPLE_FILE}",
                    "${testRootDir}/${ASCIIDOC_SAMPLE2_FILE}",)
            }

        when:
            task.gititdone()

        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE),_ )
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE2_FILE),_ )
    }
}
