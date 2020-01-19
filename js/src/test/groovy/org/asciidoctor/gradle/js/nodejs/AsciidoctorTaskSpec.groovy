/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.js.nodejs

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Asciidoctor task specification
 *
 * @author Schalk W. Cronjé
 */
class AsciidoctorTaskSpec extends Specification {
    private static final String ASCIIDOCTOR = 'asciidoctor'
    Project project = ProjectBuilder.builder().withName('test').build()

    void setup() {
        project.allprojects {
            apply plugin: 'org.asciidoctor.js.base'
        }
    }

    void 'Can log documents when processed'() {
        when:
        AsciidoctorTask asciidoctor = asciidoctorTask {
            logDocuments = true
        }

        then:
        asciidoctor.logDocuments == true
    }

    void "Allow setting of attributes via method (Map variant)"() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            asciidoctorjs {
                attributes 'source-highlighter': 'foo'
                attributes 'source-highlighter': 'coderay'
                attributes idprefix: '$', idseparator: '-'
            }
        }

        then:
        verifyAll {
            task.attributes['source-highlighter'] == 'coderay'
            task.attributes['idprefix'] == '$'
            task.attributes['idseparator'] == '-'
        }
    }

    void "Allow setting of attributes via assignment"() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            attributes = ['source-highlighter': 'foo', idprefix: '$']
            attributes = ['source-highlighter': 'coderay', idseparator: '-']
        }

        then:
        task.attributes['source-highlighter'] == 'coderay'
        task.attributes['idseparator'] == '-'
        !task.attributes.containsKey('idprefix')
    }

    void "Allow setting of backends via method"() {
        given:
        Set<String> testBackends

        when:
        asciidoctorTask {
            outputOptions {
                backends 'foo', 'bar'
                backends 'pdf'
            }

            outputOptions {
                testBackends = backends
            }
        }

        then:
        verifyAll {
            testBackends.contains('pdf')
            testBackends.contains('foo')
            testBackends.contains('bar')
        }
    }

    void "Allow setting of sourceDir via method"() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            sourceDir project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
    }

    void "When setting sourceDir via assignment"() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            sourceDir = project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
    }

    void "When setting sourceDir via setSourceDir"() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            sourceDir = project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
    }

    void 'Can configure secondary sources'() {
        given:
        final File srcDir = project.file('src/docs/asciidoc')
        final String secSrc = 'secondary.txt'

        srcDir.mkdirs()
        new File(srcDir, secSrc).text = 'foo'

        when: 'Secondary sources are specified'
        AsciidoctorTask task = asciidoctorTask {
            sourceDir srcDir
            secondarySources {
                include secSrc
            }
        }
        FileCollection fileCollection = task.secondarySourceFileTree

        then: 'Default patterns are ignored'
        fileCollection.contains(new File(srcDir, secSrc).canonicalFile)
    }

    void 'When attribute providers are registered on the task, then global ones will not be used.'() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            asciidoctorjs {
                attributeProvider {
                    [:]
                }
            }
        }

        then:
        task.attributeProviders != project.extensions.getByType(AsciidoctorJSExtension).attributeProviders
    }

    AsciidoctorTask asciidoctorTask(Closure cfg) {
        project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask).configure cfg
    }
}
