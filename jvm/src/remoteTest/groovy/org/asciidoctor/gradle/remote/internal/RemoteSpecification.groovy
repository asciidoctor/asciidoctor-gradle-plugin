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
package org.asciidoctor.gradle.remote.internal

import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.internal.ExecutorConfigurationContainer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.asciidoctor.gradle.internal.ExecutorLogLevel.DEBUG

class RemoteSpecification extends Specification {

    static final String INPUT_DOC = 'index.adoc'
    static final String INPUT_DOC2 = 'index2.adoc'
    static final String OUTPUT_HTML = 'index.html'
    static final String OUTPUT_HTML2 = 'subdir/index2.html'
    static final String OUTPUT_DOCBOOK = 'index.xml'
    static final String HTML = 'html5'
    static final String DOCBOOK = 'docbook'
    static final String INVALID_1 = 'abc'
    static final String INVALID_2 = 'def'

    @Rule
    TemporaryFolder testProjectDir

    Map getProject(File base) {
        File src = new File(base, 'src')
        File output = new File(base, 'out')
        File gemDir = new File(base, 'gems')
        File src2 = new File(src, 'subdir')

        src2.mkdirs()
        output.mkdirs()
        gemDir.mkdirs()
        new File(src, INPUT_DOC) << '''= A document

with text

include::a-missing-include-file[]
'''
        new File(src2, INPUT_DOC2) << '''= A document

in a subdirectory
'''
        new File(gemDir, 'verbose.rb').text = '$VERBOSE = true'

        [src: new File(src, INPUT_DOC), outputDir: output, gemPath: gemDir]
    }

    ExecutorConfigurationContainer getContainerSingleEntryWithFailureLevel(File srcFile, File outputDir) {
        new ExecutorConfigurationContainer(
                getExecutorConfiguration(HTML, srcFile, new File(outputDir, OUTPUT_HTML), null)
        )
    }

    ExecutorConfigurationContainer getContainerSingleEntry(File srcFile, File outputDir) {
        new ExecutorConfigurationContainer(
            getExecutorConfiguration(HTML, srcFile, new File(outputDir, OUTPUT_HTML), null)
        )
    }

    ExecutorConfigurationContainer getContainerMultipleEntries(File srcFile, File outputDir, File gemDir) {
        new ExecutorConfigurationContainer([
            getExecutorConfiguration(HTML, srcFile, new File(outputDir, OUTPUT_HTML), null),
            getExecutorConfiguration(DOCBOOK, srcFile, new File(outputDir, OUTPUT_DOCBOOK), gemDir)
        ])
    }

    @SuppressWarnings('Println')
    ExecutorConfiguration getExecutorConfiguration(
        final String backend, File srcFile, File outputFile, File gemDir, int failureLevel = 4 // FATAL
    ) {
        boolean altOptions = gemDir != null
        List<String> requires = []
        List<Object> exts = altOptions ? [{ println 'fake extension' }.dehydrate()] : []

        new ExecutorConfiguration(
            options: [:],
            attributes: [:],
            asciidoctorExtensions: exts,
            copyResources: false,
            safeModeLevel: 0,
            backendName: backend,
            fatalMessagePatterns: [],
            sourceDir: srcFile.parentFile,
            outputDir: outputFile.parentFile,
            projectDir: testProjectDir.root,
            rootDir: testProjectDir.root,
            baseDir: testProjectDir.root,
            sourceTree: [srcFile, new File(srcFile.parentFile, "subdir/${INPUT_DOC2}")],
            logDocuments: altOptions,
            executorLogLevel: DEBUG,
            failureLevel: failureLevel,
            requires: requires,
            gemPath: (altOptions ? gemDir.absolutePath : '')
        )
    }
}
