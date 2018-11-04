/*
 * Copyright 2013-2018 the original author or authors.
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

import org.gradle.api.Project
import org.gradle.api.Task
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
@SuppressWarnings(['DuplicateStringLiteral', 'DuplicateNumberLiteral', 'MethodName', 'ClassSize', 'DuplicateMapLiteral'])
class AsciidoctorTaskSpec extends Specification {
    private static final String ASCIIDOCTOR = 'asciidoctor'
    private static final String ASCIIDOC_RESOURCES_DIR = 'build/resources/test/src/asciidoc'
    private static final String ASCIIDOC_BUILD_DIR = 'build/asciidoc'
    private static final String ASCIIDOC_SAMPLE_FILE = 'sample.asciidoc'
    private static final String ASCIIDOC_SAMPLE2_FILE = 'subdir/sample2.ad'
    private static final String HTML5 = 'html5'

    Project project
    File testRootDir
    File srcDir
    File outDir
    ByteArrayOutputStream systemOut

    PrintStream originSystemOut

    def setup() {
        project = ProjectBuilder.builder().withName('test').build()
        project.configurations.create(ASCIIDOCTOR)
        testRootDir = new File('.')
        srcDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR).absoluteFile
        outDir = new File(project.projectDir, ASCIIDOC_BUILD_DIR)
        systemOut = new ByteArrayOutputStream()
        originSystemOut = System.out
        System.out = new PrintStream(systemOut)
    }

    def cleanup() {
        System.out = originSystemOut
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of options via method"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            options eruby: 'erb'
            options eruby: 'erubis'
            options doctype: 'book', toc: 'right'
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.options['eruby'] == 'erubis'
        task.options['doctype'] == 'book'
        task.options['toc'] == 'right'
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of options via assignment"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            options = [eruby: 'erb', toc: 'right']
            options = [eruby: 'erubis', doctype: 'book']
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.options['eruby'] == 'erubis'
        task.options['doctype'] == 'book'
        !task.options.containsKey('toc')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of attributes via method (Map variant)"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            attributes 'source-highlighter': 'foo'
            attributes 'source-highlighter': 'coderay'
            attributes idprefix: '$', idseparator: '-'
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.attributes['source-highlighter'] == 'coderay'
        task.attributes['idprefix'] == '$'
        task.attributes['idseparator'] == '-'
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of attributes via method (List variant)"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            attributes(['source-highlighter=foo', 'source-highlighter=coderay', 'idprefix=$', 'idseparator=-'])
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.attributes['source-highlighter'] == 'coderay'
        task.attributes['idprefix'] == '$'
        task.attributes['idseparator'] == '-'
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of attributes via method (String variant)"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            attributes 'source-highlighter=foo source-highlighter=coderay idprefix=$ idseparator=-'
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.attributes['source-highlighter'] == 'coderay'
        task.attributes['idprefix'] == '$'
        task.attributes['idseparator'] == '-'
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of attributes via assignment"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            attributes = ['source-highlighter': 'foo', idprefix: '$']
            attributes = ['source-highlighter': 'coderay', idseparator: '-']
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.attributes['source-highlighter'] == 'coderay'
        task.attributes['idseparator'] == '-'
        !task.attributes.containsKey('idprefix')
    }

    @SuppressWarnings('MethodName')
    def "Mixing attributes with options, should produce a warning, but updates should be appended"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            options eruby: 'erubis', attributes: ['source-highlighter': 'foo', idprefix: '$']
            options doctype: 'book', attributes: [idseparator: '-']
        }

        then:
        !task.attributes.containsKey('attributes')
        task.attributes['source-highlighter'] == 'foo'
        task.attributes['idseparator'] == '-'
        task.attributes['idprefix'] == '$'
        task.options['eruby'] == 'erubis'
        task.options['doctype'] == 'book'
        // @Ignore('Wrong sysout capture')
        // systemOut.toString().contains('Attributes found in options.')
    }

    @SuppressWarnings('MethodName')
    def "Mixing attributes with options with assignment, should produce a warning, and attributes will be replaced"() {
        when:
        Map tmpStore = [eruby: 'erubis', attributes: ['source-highlighter': 'foo', idprefix: '$']]
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            options = tmpStore
            options = [doctype: 'book', attributes: [idseparator: '-']]
        }

        then:
        !task.attributes.containsKey('attributes')
        task.attributes['idseparator'] == '-'
        !task.attributes.containsKey('source-highlighter')
        !task.attributes.containsKey('idprefix')
        !task.options.containsKey('eruby')
        task.options['doctype'] == 'book'
        // @Ignore('Wrong sysout capture')
        // systemOut.toString().contains('Attributes found in options.')
    }

    @SuppressWarnings('MethodName')
    def "Mixing string legacy form of attributes with options with assignment, should produce a warning, and attributes will be replaced"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            options = [doctype: 'book', attributes: 'toc=right source-highlighter=coderay toc-title=Table\\ of\\ Contents']
        }

        then:
        task.options['doctype'] == 'book'
        !task.attributes.containsKey('attributes')
        task.attributes['toc'] == 'right'
        task.attributes['source-highlighter'] == 'coderay'
        task.attributes['toc-title'] == 'Table of Contents'
        // @Ignore('Wrong sysout capture')
        // systemOut.toString().contains('Attributes found in options.')
    }

    @SuppressWarnings('MethodName')
    def "Mixing list legacy form of attributes with options with assignment, should produce a warning, and attributes will be replaced"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            options = [doctype: 'book', attributes: [
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
        // @Ignore('Wrong sysout capture')
        // systemOut.toString().contains('Attributes found in options.')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of backends via method"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            backends 'foo', 'bar'
            backends 'pdf'
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.backends.contains('pdf')
        task.backends.contains('foo')
        task.backends.contains('bar')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of backends via assignment"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            backends = ['pdf']
            backends = ['foo', 'bar']
        }

        then:
        !systemOut.toString().contains('deprecated')
        !task.backends.contains('pdf')
        task.backends.contains('foo')
        task.backends.contains('bar')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of requires via method"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            requires 'slim', 'tilt'
            requires 'asciidoctor-pdf'
        }

        then:
        !systemOut.toString().contains('deprecated')
        task.requires.contains('asciidoctor-pdf')
        task.requires.contains('tilt')
        task.requires.contains('slim')
    }

    @SuppressWarnings('MethodName')
    def "Allow setting of requires via assignment"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            requires = ['asciidoctor-pdf']
            requires = ['slim', 'tilt']
        }

        then:
        !systemOut.toString().contains('deprecated')
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
        !systemOut.toString().contains('deprecated')
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        task.sourceDir.absolutePath == project.projectDir.absolutePath
    }


    @SuppressWarnings('MethodName')
    def "When setting sourceDir via assignment"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir = project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
        task.sourceDir.absolutePath == project.projectDir.absolutePath

    }

    @SuppressWarnings('MethodName')
    def "When setting sourceDir via setSourceDir"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir = project.projectDir
        }

        then:
        task.sourceDir.absolutePath == project.projectDir.absolutePath
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
        !systemOut.toString().contains('deprecated')
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
        !systemOut.toString().contains('deprecated')
    }

    @SuppressWarnings('MethodName')
    def "When setting gemPath via setGemPath"() {
        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            gemPath = project.projectDir
        }

        then:
        task.asGemPath() == project.projectDir.absolutePath
        !systemOut.toString().contains('deprecated')
    }

    @SuppressWarnings('MethodName')
    def "sourceDocumentNames should resolve descendant files of sourceDir if supplied as relatives"() {
        when: 'I specify two files relative to sourceDir,including one in a subfolder'
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir srcDir
            sourceDocumentNames = [ASCIIDOC_SAMPLE_FILE, ASCIIDOC_SAMPLE2_FILE]
        }
        def fileCollection = task.sourceDocumentNames

        then: 'both files should be in collection, but any other files found in folder should be excluded'
        fileCollection.contains(new File(srcDir, ASCIIDOC_SAMPLE_FILE).canonicalFile)
        fileCollection.contains(new File(srcDir, ASCIIDOC_SAMPLE2_FILE).canonicalFile)
        !fileCollection.contains(new File(srcDir, 'sample-docinfo.xml').canonicalFile)
        fileCollection.files.size() == 2
    }

    @SuppressWarnings('MethodName')
    def 'sourceDocumentNames should resolve descendant files of sourceDir even if given as absolute files'() {
        given:
        File sample1 = new File(srcDir, ASCIIDOC_SAMPLE_FILE).absoluteFile
        File sample2 = new File(srcDir, ASCIIDOC_SAMPLE2_FILE).absoluteFile

        when: 'I specify two absolute path files, that are descendents of sourceDir'
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir srcDir
            sourceDocumentNames = [sample1, sample2]
        }
        def fileCollection = task.sourceDocumentNames

        then: 'both files should be in collection, but any other files found in folder should be excluded'
        fileCollection.contains(new File(srcDir, ASCIIDOC_SAMPLE_FILE).canonicalFile)
        fileCollection.contains(new File(srcDir, ASCIIDOC_SAMPLE2_FILE).canonicalFile)
        !fileCollection.contains(new File(srcDir, 'sample-docinfo.xml').canonicalFile)
        fileCollection.files.size() == 2
    }

    @SuppressWarnings('MethodName')
    def "sourceDocumentNames should not resolve files that are not descendants of sourceDir"() {
        given:
        File sample1 = new File(project.projectDir, ASCIIDOC_SAMPLE_FILE).absoluteFile

        when:
        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
            sourceDir srcDir
            sourceDocumentNames = [sample1]
        }
        def fileCollection = task.sourceDocumentNames

        then:
        fileCollection.files.size() == 0
    }

    def "Files in the resources copyspec should be recognised as input files"() {
        given:
        File imagesDir = new File(outDir, 'images')
        File imageFile = new File(imagesDir, 'fake.txt')
        imagesDir.mkdirs()
        imageFile.text = 'foo'

        Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {

            sourceDir srcDir
            outputDir "${outDir}/foo"
            backends HTML5

            sources {
                include ASCIIDOC_SAMPLE_FILE
            }

            resources {
                from(outDir) {
                    include 'images/**'
                }
            }
        }

        when:
        project.evaluate()

        then:
        task.inputs.files.contains(project.file("${srcDir}/sample.asciidoc"))
        task.inputs.files.contains(project.file("${imagesDir}/fake.txt"))
    }
}
