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
package org.asciidoctor.gradle.jvm.epub

import org.asciidoctor.gradle.jvm.epub.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.BuildScanFixture
import org.asciidoctor.gradle.testfixtures.CachingTestFixture

import static org.asciidoctor.gradle.testfixtures.JRubyTestVersions.AJ20_SAFE_MAXIMUM

class AsciidoctorEpubTaskCachingFunctionalSpec extends FunctionalSpecification
        implements CachingTestFixture, BuildScanFixture {
    private static final String DEFAULT_TASK = 'asciidoctorEpub'
    private static final String DEFAULT_OUTPUT_FILE = 'build/docs/asciidocEpub/epub3.epub'
    private static final String JRUBY_TEST_VERSION = AJ20_SAFE_MAXIMUM

    void setup() {
        setupCache()
        createTestProject()
    }

    void "Epub task is cacheable and relocatable"() {
        given:
        getBuildFile(singleFormatConfiguration('EPUB3'))

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        assertDefaultTaskIsCachedAndRelocatable()

        then:
        outputFile.exists()
        outputFileInRelocatedDirectory.exists()
    }

    @Override
    File getOutputFile() {
        file(DEFAULT_OUTPUT_FILE)
    }

    @Override
    String getDefaultTask() {
        ":${DEFAULT_TASK}"
    }

    File getBuildFile(String extraContent) {
        writeGroovyBuildFile('org.asciidoctor.jvm.epub', extraContent).withWriterAppend { w ->
            if (performBuildScan) {
                w.println buildScanConfiguration
            }
        }
        buildFile
    }

    String singleFormatConfiguration(String format) {
        """
            asciidoctorEpub {
                sourceDir 'src/docs/asciidoc'
                ebookFormats ${format}

                asciidoctorj {
                    jrubyVersion = '${JRUBY_TEST_VERSION}'
                }
    
                sources {
                    include 'epub3.adoc'
                }
            }
        """
    }
}
