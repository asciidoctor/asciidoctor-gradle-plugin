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
import org.gradle.api.InvalidUserDataException
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
 * @author Schalk W. Cronjé
 */
class AsciidoctorTaskSpec extends Specification {
    private static final String ASCIIDOCTOR = 'asciidoctor'
    private static final String ASCIIDOC_RESOURCES_DIR = 'build/resources/test/src/asciidoc'
    private static final String ASCIIDOC_RESOURCES_SUB_DIR = 'build/resources/test/src/asciidoc/subdir'
    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidoc'
    private static final String ASCIIDOC_SAMPLE_FILE = 'sample.asciidoc'
    private static final String ASCIIDOC_SAMPLE2_FILE = 'subdir/sample2.ad'
    private static final String ASCIIDOC_INVALID_FILE = 'subdir/_include.adoc'
    private static final DOCINFO_FILE_PATTERN = ~/^(.+\-)?docinfo(-footer)?\.[^.]+$/

    Project project
    AsciidoctorProxy mockAsciidoctor
    ResourceCopyProxy mockCopyProxy
    File testRootDir
    File srcDir
    File outDir
    ByteArrayOutputStream systemOut

    def setup() {
        project = ProjectBuilder.builder().withName('test').build()
        project.configurations.create(ASCIIDOCTOR)
        mockAsciidoctor = Mock(AsciidoctorProxy)
        mockCopyProxy = Mock(ResourceCopyProxy)
        testRootDir = new File('.')
        srcDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR).absoluteFile
        outDir = new File(project.projectDir, ASCIIDOC_BUILD_DIR)
        systemOut = new ByteArrayOutputStream()
        System.out = new PrintStream(systemOut)
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of options via method"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                options eruby : 'erb'
                options eruby : 'erubis'
                options doctype : 'book', toc : 'right'
            }

        then:
            ! systemOut.toString().contains('deprecated')
            task.options['eruby'] == 'erubis'
            task.options['doctype'] == 'book'
            task.options['toc'] == 'right'
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of options via assignment"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                options = [eruby : 'erb', toc : 'right']
                options = [eruby : 'erubis', doctype : 'book']
            }

        then:
            ! systemOut.toString().contains('deprecated')
            task.options['eruby'] == 'erubis'
            task.options['doctype'] == 'book'
            !task.options.containsKey('toc')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of attributes via method"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                attributes 'source-highlighter': 'foo'
                attributes 'source-highlighter': 'coderay'
                attributes idprefix : '$', idseparator : '-'
            }

        then:
            ! systemOut.toString().contains('deprecated')
            task.attributes['source-highlighter'] == 'coderay'
            task.attributes['idprefix'] == '$'
            task.attributes['idseparator'] == '-'
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of attributes via assignment"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                attributes = ['source-highlighter': 'foo',idprefix : '$']
                attributes = ['source-highlighter': 'coderay', idseparator : '-']
            }

        then:
            ! systemOut.toString().contains('deprecated')
            task.attributes['source-highlighter'] == 'coderay'
            task.attributes['idseparator'] == '-'
            !task.attributes.containsKey('idprefix')
    }

    @SuppressWarnings('MethodName')
    def "Mixing attributes with options, should produce a warning, but updates should be appended"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                options eruby : 'erubis', attributes : ['source-highlighter': 'foo',idprefix : '$']
                options doctype: 'book', attributes : [idseparator : '-' ]
            }

        then:
            !task.attributes.containsKey('attributes')
            task.attributes['source-highlighter'] == 'foo'
            task.attributes['idseparator'] == '-'
            task.attributes['idprefix'] == '$'
            task.options['eruby'] == 'erubis'
            task.options['doctype'] == 'book'
            systemOut.toString().contains('Attributes found in options.')
    }

    @SuppressWarnings('MethodName')
    def "Mixing attributes with options with assignment, should produce a warning, and attributes will be replaced"() {
        when:
            Map tmpStore = [ eruby : 'erubis', attributes : ['source-highlighter': 'foo',idprefix : '$'] ]
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                options = tmpStore
                options = [ doctype: 'book', attributes : [idseparator : '-' ] ]
            }

        then:
            !task.attributes.containsKey('attributes')
            task.attributes['idseparator'] == '-'
            !task.attributes.containsKey('source-highlighter')
            !task.attributes.containsKey('idprefix')
            !task.options.containsKey('eruby')
            task.options['doctype'] == 'book'
            systemOut.toString().contains('Attributes found in options.')
    }

    @SuppressWarnings('MethodName')
    def "Mixing string legacy form of attributes with options with assignment, should produce a warning, and attributes will be replaced"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                options = [ doctype: 'book', attributes : 'toc=right source-highlighter=coderay toc-title=Table\\ of\\ Contents' ]
            }

        then:
            task.options['doctype'] == 'book'
            !task.attributes.containsKey('attributes')
            task.attributes['toc'] == 'right'
            task.attributes['source-highlighter'] == 'coderay'
            task.attributes['toc-title'] == 'Table of Contents'
            systemOut.toString().contains('Attributes found in options.')
    }

    @SuppressWarnings('MethodName')
    def "Mixing list legacy form of attributes with options with assignment, should produce a warning, and attributes will be replaced"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                options = [ doctype: 'book', attributes : [
                            'toc=right',
                            'source-highlighter=coderay',
                            'toc-title=Table of Contents'
                            ]]
            }

        then:
            task.options['doctype'] == 'book'
            !task.attributes.containsKey('attributes')
            task.attributes['toc'] == 'right'
            task.attributes['source-highlighter'] == 'coderay'
            task.attributes['toc-title'] == 'Table of Contents'
            systemOut.toString().contains('Attributes found in options.')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of backends via method"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                backends 'foo','bar'
                backends 'pdf'
            }

        then:
            ! systemOut.toString().contains('deprecated')
            task.backends.contains('pdf')
            task.backends.contains('foo')
            task.backends.contains('bar')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of backends via assignment"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                backends = ['pdf']
                backends = ['foo','bar']
            }

        then:
            ! systemOut.toString().contains('deprecated')
            !task.backends.contains('pdf')
            task.backends.contains('foo')
            task.backends.contains('bar')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of requires via method"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                requires 'slim','tilt'
                requires 'asciidoctor-pdf'
            }

        then:
            ! systemOut.toString().contains('deprecated')
            task.requires.contains('asciidoctor-pdf')
            task.requires.contains('tilt')
            task.requires.contains('slim')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of requires via assignment"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                requires = ['asciidoctor-pdf']
                requires = ['slim','tilt']
            }

        then:
            ! systemOut.toString().contains('deprecated')
            !task.requires.contains('asciidoctor-pdf')
            task.requires.contains('tilt')
            task.requires.contains('slim')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of sourceDir via method"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                sourceDir project.projectDir
            }

        then:
            ! systemOut.toString().contains('deprecated')
            task.getSourceDir().absolutePath == project.projectDir.absolutePath
            task.sourceDir.absolutePath == project.projectDir.absolutePath
    }


    @SuppressWarnings('MethodName')
    def "When setting sourceDir via assignment"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                sourceDir = project.projectDir
            }

        then:
            task.getSourceDir().absolutePath == project.projectDir.absolutePath
            task.sourceDir.absolutePath == project.projectDir.absolutePath

    }

    @SuppressWarnings('MethodName')
    def "When setting sourceDir via setSourceDir"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                setSourceDir project.projectDir
            }

        then:
            task.getSourceDir().absolutePath == project.projectDir.absolutePath
            task.sourceDir.absolutePath == project.projectDir.absolutePath
            !systemOut.toString().contains('deprecated')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of gemPath via method"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                gemPath project.projectDir
            }

        then:
            ! systemOut.toString().contains('deprecated')
            task.asGemPath() == project.projectDir.absolutePath
    }

    @SuppressWarnings('MethodName')
    def "When setting gemPath via assignment"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                gemPath = project.projectDir
            }

        then:
            task.asGemPath() == project.projectDir.absolutePath
            ! systemOut.toString().contains('deprecated')
    }

    @SuppressWarnings('MethodName')
    def "When setting gemPath via setGemPath"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                setGemPath project.projectDir
            }

        then:
            task.asGemPath() == project.projectDir.absolutePath
            ! systemOut.toString().contains('deprecated')
    }

    @SuppressWarnings('MethodName')
    def "sourceDocumentNames should resolve descendant files of sourceDir if supplied as relatives"() {
        when: "I specify two files relative to sourceDir,including one in a subfoler"
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                sourceDir srcDir
                sourceDocumentNames = [ASCIIDOC_SAMPLE_FILE, ASCIIDOC_SAMPLE2_FILE]
            }
            def fileCollection = task.sourceDocumentNames

        then: "both files should be in collection, but any other files found in folder should be excluded"
            fileCollection.contains(new File(srcDir,ASCIIDOC_SAMPLE_FILE).canonicalFile)
            fileCollection.contains(new File(srcDir,ASCIIDOC_SAMPLE2_FILE).canonicalFile)
            !fileCollection.contains(new File(srcDir,'sample-docinfo.xml').canonicalFile)
            fileCollection.files.size() == 2
    }

//    @SuppressWarnings('MethodName')
//    def "sourceDocumentNames should resolve descendant files of sourceDir even if given as absolute files"() {
//        given:
//            File sample1 = new File(srcDir,ASCIIDOC_SAMPLE_FILE).absoluteFile
//            File sample2 = new File(srcDir,ASCIIDOC_SAMPLE2_FILE).absoluteFile
//
//        when: "I specify two absolute path files, that are descendents of sourceDit"
//            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
//                sourceDir srcDir
//                sourceDocumentNames  sample1
//                sourceDocumentNames  sample2
//            }
//            def fileCollection = task.sourceDocumentNames
//
//        then: "both files should be in collection, but any other files found in folder should be excluded"
//            fileCollection.contains(new File(srcDir,ASCIIDOC_SAMPLE_FILE).canonicalFile)
//            fileCollection.contains(new File(srcDir,ASCIIDOC_SAMPLE2_FILE).canonicalFile)
//            !fileCollection.contains(new File(srcDir,'sample-docinfo.xml').canonicalFile)
//            fileCollection.files.size() == 2
//    }

    @SuppressWarnings('MethodName')
    def "sourceDocumentNames should not resolve files that are not descendants of sourceDir"() {
        given:
            File sample1 = new File(project.projectDir,ASCIIDOC_SAMPLE_FILE).absoluteFile

        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                sourceDir srcDir
                sourceDocumentNames = [sample1]
            }
            def fileCollection = task.sourceDocumentNames

        then:
            fileCollection.files.size() == 0
    }

//    @SuppressWarnings('MethodName')
//    def "sourceDocumentNames should resolve descendant files of sourceDir even if passed as a FileCollection"() {
//        given:
//            File sample1 = new File(srcDir,ASCIIDOC_SAMPLE_FILE).absoluteFile
//            File sample2 = new File(srcDir,ASCIIDOC_SAMPLE2_FILE).absoluteFile
//
//        when: "I specify two files in a FileCollection, that are descendents of sourceDit"
//            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
//                sourceDir srcDir
//                sourceDocumentNames  new SimpleFileCollection(sample1,sample2)
//            }
//            def fileCollection = task.sourceDocumentNames
//
//        then: "both files should be in collection, but any other files found in folder should be excluded"
//            fileCollection.contains(new File(srcDir,ASCIIDOC_SAMPLE_FILE).canonicalFile)
//            fileCollection.contains(new File(srcDir,ASCIIDOC_SAMPLE2_FILE).canonicalFile)
//            !fileCollection.contains(new File(srcDir,'sample-docinfo.xml').canonicalFile)
//            fileCollection.files.size() == 2
//    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateNumberLiteral')
    def "Add asciidoctor task with multiple backends"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                backends AsciidoctorBackend.DOCBOOK.id, AsciidoctorBackend.HTML5.id
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
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                backends = [AsciidoctorBackend.DOCBOOK.id, AsciidoctorBackend.HTML5.id]
                backend = AsciidoctorBackend.DOCBOOK5.id
            }

            task.processAsciidocSources()
        then:
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.DOCBOOK.id})
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.HTML5.id})
            systemOut.toString().contains('Using `backend` and `backends` together will result in `backend` being ignored.')
    }

    @SuppressWarnings('MethodName')
    @SuppressWarnings('DuplicateNumberLiteral')
    def "Adds asciidoctor task with supported backend"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
                sourceDir srcDir
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
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                backend = AsciidoctorBackend.DOCBOOK.id
            }

            task.processAsciidocSources()
        then:
            2 * mockAsciidoctor.renderFile(_, { Map map -> map.backend == AsciidoctorBackend.DOCBOOK.id})
            systemOut.toString().contains('Using `backend` and `backends` together will result in `backend` being ignored.')
    }

    @SuppressWarnings('MethodName')
    def "Adds asciidoctor task throws exception"() {
        expect:
            project.tasks.findByName(ASCIIDOCTOR) == null
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(srcDir, ASCIIDOC_SAMPLE_FILE)
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
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(srcDir, ASCIIDOC_SAMPLE_FILE)
            }
        when:
            task.processAsciidocSources()
        then:
            systemOut.toString().contains('setSourceDocumentName is deprecated')
    }

    @SuppressWarnings('MethodName')
    def "Output error when sourceDocumentName and sourceDocumentNames are given"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(srcDir, ASCIIDOC_SAMPLE_FILE)
                sourceDocumentNames = new SimpleFileCollection(new File(srcDir, ASCIIDOC_SAMPLE_FILE))
            }
        when:
            task.processAsciidocSources()
        then:
            systemOut.toString().contains('setSourceDocumentNames is deprecated')
    }

    @SuppressWarnings('MethodName')
    def "Source documents in directories end up in the corresponding output directory"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                separateOutputDirs = false
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
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(srcDir, ASCIIDOC_SAMPLE_FILE)
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
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(srcDir, ASCIIDOC_SAMPLE_FILE)
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
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(srcDir, ASCIIDOC_SAMPLE_FILE)
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
    def "Throws exception when attributes embedded in options is an unsupported type"() {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(srcDir, ASCIIDOC_SAMPLE_FILE)
                options = [
                  attributes: 23
                ]
            }
            task.processAsciidocSources()
        then:
            thrown(InvalidUserDataException)
    }

    @SuppressWarnings('MethodName')
    def "Setting baseDir results in the correct value being sent to Asciidoctor"() {
        given:
            File basedir = new File(testRootDir, 'my_base_dir')
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
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
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sources {
                  include ASCIIDOC_SAMPLE_FILE,ASCIIDOC_SAMPLE2_FILE
                }
            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE_FILE),_ )
            1 * mockAsciidoctor.renderFile(new File(task.sourceDir, ASCIIDOC_SAMPLE2_FILE),_ )
    }

//    @SuppressWarnings('MethodName')
//    def "Should throw exception if the file sourceDocumentName is not reachable from sourceDir"() {
//        given:
//            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
//                asciidoctor = mockAsciidoctor
//                sourceDir new File(testRootDir, ASCIIDOC_RESOURCES_SUB_DIR).absoluteFile
//                outputDir = outDir
//                sourceDocumentNames new File(srcDir, ASCIIDOC_SAMPLE_FILE).absoluteFile
//            }
//        when:
//            task.processAsciidocSources()
//        then:
//            0 * mockAsciidoctor.renderFile(_, _)
//            thrown(GradleException)
//    }

//    @SuppressWarnings('MethodName')
//    def "Should throw exception if a file in sourceDocumentNames is not reachable from sourceDir"() {
//        given:
//            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
//                asciidoctor = mockAsciidoctor
//                sourceDir new File(testRootDir, ASCIIDOC_RESOURCES_SUB_DIR).absoluteFile
//                outputDir = outDir
//                sourceDocumentNames new SimpleFileCollection(new File(srcDir, ASCIIDOC_SAMPLE_FILE).absoluteFile)
//            }
//        when:
//            task.processAsciidocSources()
//        then:
//            0 * mockAsciidoctor.renderFile(_, _)
//            thrown(GradleException)
//    }

    @SuppressWarnings('MethodName')
    def "Should throw exception if the file sourceDocumentName starts with underscore"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
                sourceDir = srcDir
                outputDir = outDir
                sourceDocumentName = new File(srcDir, ASCIIDOC_INVALID_FILE)
            }
        when:
            task.processAsciidocSources()
        then:
            0 * mockAsciidoctor.renderFile(_, _)
            thrown(GradleException)
    }

    @SuppressWarnings('MethodName')
    def "Should throw exception if a file in sourceDocumentNames starts with underscore"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy
                sourceDir srcDir
                outputDir outDir
                setSourceDocumentNames ASCIIDOC_INVALID_FILE
            }
        when:
            task.processAsciidocSources()
        then:
            0 * mockAsciidoctor.renderFile(_, _)
            thrown(GradleException)
    }

//    @SuppressWarnings('MethodName')
//    def "Should not emit warning about absolute path in sourceDocumentNames"() {
//        expect:
//            project.tasks.findByName(ASCIIDOCTOR) == null
//        when:
//            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
//                asciidoctor = mockAsciidoctor
//                sourceDir = srcDir
//                outputDir = outDir
//                sourceDocumentNames = new SimpleFileCollection(new File(srcDir, ASCIIDOC_SAMPLE_FILE).absoluteFile)
//            }
//
//            task.processAsciidocSources()
//        then:
//            1 * mockAsciidoctor.renderFile(_, _)
//    }

    def "When 'resources' not specified, then copy all images to backend"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy

                sourceDir srcDir
                outputDir outDir
                backends AsciidoctorBackend.HTML5.id

                sources {
                    include ASCIIDOC_SAMPLE_FILE
                }

            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockCopyProxy.copy(_, _)
    }

    def "When 'resources' not specified and more than one backend, then copy all images to every backend"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy

                sourceDir srcDir
                outputDir outDir
                backends AsciidoctorBackend.HTML5.id,AsciidoctorBackend.DOCBOOK.id

                sources {
                    include ASCIIDOC_SAMPLE_FILE
                }

            }
        when:
            task.processAsciidocSources()
        then:
            1 * mockCopyProxy.copy( new File(outDir,AsciidoctorBackend.HTML5.id) , _)
            1 * mockCopyProxy.copy( new File(outDir,AsciidoctorBackend.DOCBOOK.id) , _)
    }

    def "When 'resources' are specified, then copy according to all patterns"() {
        given:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
                asciidoctor = mockAsciidoctor
                resourceCopyProxy = mockCopyProxy

                sourceDir srcDir
                outputDir outDir
                backends AsciidoctorBackend.HTML5.id,AsciidoctorBackend.DOCBOOK.id

                sources {
                    include ASCIIDOC_SAMPLE_FILE
                }

                resources {
                    from (sourceDir) {
                        include 'images/**'
                    }
                }
            }

        when:
            task.processAsciidocSources()
        then:
            1 * mockCopyProxy.copy( new File(outDir,AsciidoctorBackend.HTML5.id) , _)
            1 * mockCopyProxy.copy( new File(outDir,AsciidoctorBackend.DOCBOOK.id) , _)

    }

    def "sanity test for default configuration" () {
        when:
            Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            }

        then:
            task.sourceDir.absolutePath.endsWith("src/docs/asciidoc")

    }

}
