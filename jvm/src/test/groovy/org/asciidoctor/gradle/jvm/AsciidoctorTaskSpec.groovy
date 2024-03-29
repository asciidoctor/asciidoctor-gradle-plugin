/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.jvm

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.grolifant.api.core.ProjectOperations
import org.ysb33r.grolifant.api.core.StringTools
import spock.lang.Specification

import static org.asciidoctor.gradle.base.internal.AsciidoctorAttributes.resolveAsSerializable

/**
 * Asciidoctor task specification
 *
 * @author Benjamin Muschko
 * @author Stephan Classen
 * @author Marcus Fihlon
 * @author Schalk W. Cronjé
 * @author Lari Hotari
 */
class AsciidoctorTaskSpec extends Specification {
    private static final String ASCIIDOC_RESOURCES_DIR = 'asciidoctor-gradle-jvm/src/test/resources/src/asciidoc'
    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidoc'

    Project project = ProjectBuilder.builder().withName('test').build()
    StringTools strTools
    File testRootDir
    File srcDir
    File outDir
    ByteArrayOutputStream systemOut

    PrintStream originSystemOut

    void setup() {
        project.allprojects {
            apply plugin: 'org.asciidoctor.jvm.base'
        }

        testRootDir = new File(System.getProperty('ROOT_PROJECT_DIR') ?: '.')
        srcDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR).absoluteFile
        outDir = new File(project.projectDir, ASCIIDOC_BUILD_DIR)
        systemOut = new ByteArrayOutputStream()
        originSystemOut = System.out
        System.out = new PrintStream(systemOut)
        strTools = ProjectOperations.find(project).stringTools
    }

    void cleanup() {
        System.out = originSystemOut
    }

    void 'Base directory is project directory by default'() {
        when:
        final task = createTask {
        }

        then:
        task.baseDir == project.projectDir
    }

    void 'Base directory can be root project directory'() {
        when:
        final task = createTask {
            baseDirIsRootProjectDir()
        }

        then:
        task.baseDir == project.rootProject.projectDir
    }

    void 'Base directory can be project directory'() {
        when:
        final task = createTask {
            baseDirIsRootProjectDir()
            baseDirIsProjectDir()
        }

        then:
        task.baseDir == project.rootProject.projectDir
    }

    void 'Base directory can be a fixed directory'() {
        when:
        final task = createTask {
            baseDir 'foo'
        }

        then:
        task.baseDir == project.file('foo')
    }

    void 'Base directory can be source directory'() {
        when:
        final task = createTask {
            baseDirFollowsSourceDir()
        }

        then:
        task.baseDir == task.sourceDir
    }

    void 'Base directory can be source directory within a temporary working directory'() {
        when:
        final task = createTask {
            baseDirFollowsSourceDir()
            useIntermediateWorkDir()
        }

        then:
        task.baseDir == project.file("${project.buildDir}/tmp/${task.name}.intermediate")
    }

    void 'Base directory can be null to follow source file'() {
        when:
        final task = createTask {
            baseDirFollowsSourceFile()
        }

        then:
        task.baseDir == null
    }

    void 'Base directory can be set to null'() {
        when:
        final task = createTask {
            baseDir = null
        }

        then:
        task.baseDir == null
    }

    void "Allow setting of options via method"() {
        when:
        final task = createTask {
            options eruby: 'erb'
            options eruby: 'erubis'
            options doctype: 'book', toc: 'right'
        }
        final opts = resolveAsSerializable(task.options, strTools)

        then:
        !systemOut.toString().contains('deprecated')
        opts['eruby'] == 'erubis'
        opts['doctype'] == 'book'
        opts['toc'] == 'right'
    }

    void "Allow setting of options via assignment"() {
        when:
        final task = createTask {
            options = [eruby: 'erb', toc: 'right']
            options = [eruby: 'erubis', doctype: 'book']
        }
        final opts = resolveAsSerializable(task.options, strTools)

        then:
        !systemOut.toString().contains('deprecated')
        opts['eruby'] == 'erubis'
        opts['doctype'] == 'book'
        !opts.containsKey('toc')
    }

    void "Allow setting of attributes via method (Map variant)"() {
        when:
        final task = createTask {
            attributes 'source-highlighter': 'foo'
            attributes 'source-highlighter': 'coderay'
            attributes idprefix: '$', idseparator: '-'
        }
        final attrs = resolveAsSerializable(task.attributes, strTools)

        then:
        !systemOut.toString().contains('deprecated')
        attrs['source-highlighter'] == 'coderay'
        attrs['idprefix'] == '$'
        attrs['idseparator'] == '-'
    }

    void "Do not allow setting of attributes via legacy key=value list"() {
        when:
        createTask {
            attributes(['source-highlighter=foo', 'source-highlighter=coderay', 'idprefix=$', 'idseparator=-'])
        }

        then:
        thrown(MissingMethodException)
    }

    void "Do not allow setting of attributes via legacy key-value string"() {
        when:
        createTask {
            attributes 'source-highlighter=foo source-highlighter=coderay idprefix=$ idseparator=-'
        }

        then:
        thrown(MissingMethodException)
    }

    void "Allow setting of attributes via assignment"() {
        when:
        final task = createTask {
            attributes = ['source-highlighter': 'foo', idprefix: '$']
            attributes = ['source-highlighter': 'coderay', idseparator: '-']
        }
        final attrs = resolveAsSerializable(task.attributes, strTools)

        then:
        !systemOut.toString().contains('deprecated')
        attrs['source-highlighter'] == 'coderay'
        attrs['idseparator'] == '-'
        !attrs.containsKey('idprefix')
    }

    void "Mixing attributes with options, produces an exception"() {
        when:
        createTask {
            options eruby: 'erubis', attributes: ['source-highlighter': 'foo', idprefix: '$']
            options doctype: 'book', attributes: [idseparator: '-']
        }

        then:
        thrown(GradleException)
    }

    void "Mixing attributes with options (with assignment), produces an exception"() {
        when:
        Map tmpStore = [eruby: 'erubis', attributes: ['source-highlighter': 'foo', idprefix: '$']]
        createTask {
            options = tmpStore
            options = [doctype: 'book', attributes: [idseparator: '-']]
        }

        then:
        thrown(GradleException)
    }

    void "Mixing string legacy form of attributes with options with assignment, produces an exception"() {
        when:
        createTask {
            options = [
                    doctype   : 'book',
                    attributes: 'toc=right source-highlighter=coderay toc-title=Table\\ of\\ Contents'
            ]
        }

        then:
        thrown(GradleException)
    }

    void "Mixing list legacy form of attributes with options with assignment, produces an exception"() {
        when:
        createTask {
            options = [doctype: 'book', attributes: [
                    'toc=right',
                    'source-highlighter=coderay',
                    'toc-title=Table of Contents'
            ]]
        }

        then:
        thrown(GradleException)
    }

    void "Allow setting of backends via method"() {
        given:
        Set<String> testBackends

        when:
        createTask {
            outputOptions {
                backends 'foo', 'bar'
                backends 'pdf'
            }

            outputOptions {
                testBackends = backends
            }
        }

        then:
        !systemOut.toString().contains('deprecated')
        verifyAll {
            testBackends.contains('pdf')
            testBackends.contains('foo')
            testBackends.contains('bar')
        }
    }

    void "Allow setting of backends via assignment"() {
        given:
        Set<String> testBackends

        when:
        createTask {
            outputOptions {
                backends = ['pdf']
                backends = ['foo', 'bar']
            }

            outputOptions {
                testBackends = backends
            }
        }

        then:
        !systemOut.toString().contains('deprecated')

        verifyAll {
            !testBackends.contains('pdf')
            testBackends.contains('foo')
            testBackends.contains('bar')
        }
    }

    void "Allow setting of requires via method"() {
        when:
        project.allprojects {
            asciidoctorj {
                requires 'asciidoctor-pdf'
            }
        }
        final task = createTask {
            asciidoctorj {
                requires 'slim', 'tilt'
            }
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.asciidoctorj.requires[0] == 'asciidoctor-pdf'
        task.asciidoctorj.requires[1] == 'slim'
        task.asciidoctorj.requires[2] == 'tilt'
    }

    void "Allow setting of requires via assignment"() {
        when:
        project.allprojects {
            asciidoctorj {
                requires 'asciidoctor-pdf'
            }
        }
        final task = createTask {
            asciidoctorj {
                requires = ['slim', 'tilt']
            }
        }

        then:
        !systemOut.toString().contains('deprecated')
        !task.asciidoctorj.requires.contains('asciidoctor-pdf')
        task.asciidoctorj.requires.contains('tilt')
        task.asciidoctorj.requires.contains('slim')
    }

    void "Allow setting of sourceDir via method"() {
        when:
        final task = createTask {
            sourceDir project.projectDir
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        task.sourceDir.absolutePath == project.projectDir.absolutePath
    }

    void "When setting sourceDir via assignment"() {
        when:
        final task = createTask {
            sourceDir = project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        task.sourceDir.absolutePath == project.projectDir.absolutePath
    }

    void "When setting sourceDir via setSourceDir"() {
        when:
        final task = createTask {
            sourceDir = project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        !systemOut.toString().contains('deprecated')
    }

    void 'When attribute providers are registered on the task, then global ones will not be used.'() {
        when:
        final task = createTask {
            asciidoctorj {
                attributeProvider {
                    [:]
                }
            }
        }

        then:
        task.attributeProviders != project.extensions.getByType(AsciidoctorJExtension).attributeProviders
    }

    void 'Asciidoctor task with non-default name has different source directory'() {
        when:
        AsciidoctorTask task = project.tasks.create(name: 'kilowatt', type: AsciidoctorTask)

        then:
        task.sourceDir == project.file('src/docs/asciidocKilowatt')
    }

    private AsciidoctorTask createTask(@DelegatesTo(AsciidoctorTask) Closure configurator) {
        final task = project.tasks.create('asciidoctorTask', AsciidoctorTask)
        task.configure(configurator)
        task
    }

//    void 'Configure fork options via closure'() {
//        when:
//        AsciidoctorTask task = asciidoctorTask {
//            forkOptions {
//                debug = true
//            }
//        }
//
//        then:
//        task.javaForkOptions.debug == true
//    }

//    void 'Configure fork options via an Action'() {
//        when:
//        def withOptions = new Action<JavaForkOptions>() {
//            void execute(JavaForkOptions javaForkOptions) {
//                javaForkOptions.minHeapSize = '123'
//            }
//        }
//
//        AsciidoctorTask task = asciidoctorTask {
//            forkOptions withOptions
//        }
//
//        then:
//        task.javaForkOptions.minHeapSize == '123'
//    }
//
//    AsciidoctorTask asciidoctorTask(Closure cfg) {
//        project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask).configure cfg
//    }
}

//class ExecutorConfigurationInspectingAsciidoctorTask extends AsciidoctorTask {
//    @Inject
//    ExecutorConfigurationInspectingAsciidoctorTask(WorkerExecutor we) {
//        super(we)
//    }
//
//    // method for unit testing what attributes get passed to execution
//    ExecutorConfiguration getSampleExecutorConfiguration(String backendName = 'html',
//                                                         File workingSourceDir = new File('.'),
//                                                         Set<File> sourceFiles = [] as Set,
//                                                         Optional<String> lang = Optional.empty()) {
//        super.getExecutorConfigurationFor(backendName, workingSourceDir, sourceFiles, lang)
//    }
//}
