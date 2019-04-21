/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.js


import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Asciidoctor task specification
 *
 * @author Schalk W. Cronjé
 */
class AsciidoctorTaskSpec extends Specification {
    private static final String ASCIIDOCTOR = 'asciidoctor'
//    private static final String ASCIIDOC_RESOURCES_DIR = 'asciidoctor-gradle-jvm/src/test/resources/src/asciidoc'
//    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidoc'
//    private static final String ASCIIDOC_SAMPLE_FILE = 'sample.asciidoc'
//    private static final String ASCIIDOC_SAMPLE2_FILE = 'subdir/sample2.ad'

    Project project = ProjectBuilder.builder().withName('test').build()

//    File testRootDir
//    File srcDir
//    File outDir
//    ByteArrayOutputStream systemOut
//
//    PrintStream originSystemOut

    def setup() {

        project.allprojects {
            apply plugin: 'org.asciidoctor.js.base'
        }

//        testRootDir = new File(System.getProperty('ROOT_PROJECT_DIR') ?: '.')
//        srcDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR).absoluteFile
//        outDir = new File(project.projectDir, ASCIIDOC_BUILD_DIR)
//        systemOut = new ByteArrayOutputStream()
//        originSystemOut = System.out
//        System.out = new PrintStream(systemOut)
    }

//    void cleanup() {
//        System.out = originSystemOut
//    }

    void 'Can log documents when processed'() {
        when:
        AsciidoctorTask asciidoctor = asciidoctorTask {
            logDocuments = true
        }

        then:
        asciidoctor.logDocuments == true
    }
//    void "Allow setting of options via method"() {
//        when:
//        AsciidoctorTask task = asciidoctorTask {
//            options eruby: 'erb'
//            options eruby: 'erubis'
//            options doctype: 'book', toc: 'right'
//        }
//
//        then:
//        !systemOut.toString().contains('deprecated')
//        task.options['eruby'] == 'erubis'
//        task.options['doctype'] == 'book'
//        task.options['toc'] == 'right'
//    }
//
//    void "Allow setting of options via assignment"() {
//        when:
//        AsciidoctorTask task = asciidoctorTask {
//            options = [eruby: 'erb', toc: 'right']
//            options = [eruby: 'erubis', doctype: 'book']
//        }
//
//        then:
//        !systemOut.toString().contains('deprecated')
//        task.options['eruby'] == 'erubis'
//        task.options['doctype'] == 'book'
//        !task.options.containsKey('toc')
//    }
//
//    void "Allow setting of attributes via method (Map variant)"() {
//        when:
//        AsciidoctorTask task = asciidoctorTask {
//            attributes 'source-highlighter': 'foo'
//            attributes 'source-highlighter': 'coderay'
//            attributes idprefix: '$', idseparator: '-'
//        }
//
//        then:
//        !systemOut.toString().contains('deprecated')
//        task.attributes['source-highlighter'] == 'coderay'
//        task.attributes['idprefix'] == '$'
//        task.attributes['idseparator'] == '-'
//    }
//
//    void "Allow setting of attributes via assignment"() {
//        when:
//        AsciidoctorTask task = asciidoctorTask {
//            attributes = ['source-highlighter': 'foo', idprefix: '$']
//            attributes = ['source-highlighter': 'coderay', idseparator: '-']
//        }
//
//        then:
//        !systemOut.toString().contains('deprecated')
//        task.attributes['source-highlighter'] == 'coderay'
//        task.attributes['idseparator'] == '-'
//        !task.attributes.containsKey('idprefix')
//    }

//    void "Allow setting of backends via method"() {
//        given:
//        Set<String> testBackends
//
//        when:
//        asciidoctorTask {
//            outputOptions {
//                backends 'foo', 'bar'
//                backends 'pdf'
//            }
//
//            outputOptions {
//                testBackends = backends
//            }
//        }
//
//        then:
//        !systemOut.toString().contains('deprecated')
//        verifyAll {
//            testBackends.contains('pdf')
//            testBackends.contains('foo')
//            testBackends.contains('bar')
//        }
//    }
//
//    void "Allow setting of backends via assignment"() {
//        given:
//        Set<String> testBackends
//
//        when:
//        asciidoctorTask {
//            outputOptions {
//                backends = ['pdf']
//                backends = ['foo', 'bar']
//            }
//
//            outputOptions {
//                testBackends = backends
//            }
//        }
//
//        then:
//        !systemOut.toString().contains('deprecated')
//
//        verifyAll {
//            !testBackends.contains('pdf')
//            testBackends.contains('foo')
//            testBackends.contains('bar')
//        }
//    }


    void "Allow setting of sourceDir via method"() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            sourceDir project.projectDir
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        task.sourceDir.absolutePath == project.projectDir.absolutePath
    }


    void "When setting sourceDir via assignment"() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            sourceDir = project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        task.sourceDir.absolutePath == project.projectDir.absolutePath

    }

    void "When setting sourceDir via setSourceDir"() {
        when:
        AsciidoctorTask task = asciidoctorTask {
            sourceDir = project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        !systemOut.toString().contains('deprecated')
    }

//    void 'Can configure secondary sources'() {
//        final String secSrc = 'secondary.txt'
//        when: 'Secondary sources are specified'
//        AsciidoctorTask task = asciidoctorTask {
//            sourceDir srcDir
//            secondarySources {
//                include secSrc
//            }
//        }
//        FileCollection fileCollection = task.secondarySourceFileTree
//
//        then: 'Default patterns are ignored'
//        fileCollection.contains(new File(srcDir, secSrc).canonicalFile)
//    }
//
//
//    void 'When attribute providers are registered on the task, then global ones will not be used.'() {
//        when:
//        AsciidoctorTask task = asciidoctorTask {
//            asciidoctorj {
//                attributeProvider {
//                    [:]
//                }
//            }
//        }
//
//        then:
//        task.attributeProviders != project.extensions.getByType(AsciidoctorJExtension).attributeProviders
//    }

    AsciidoctorTask asciidoctorTask(Closure cfg) {
        project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask).configure cfg
    }
}
