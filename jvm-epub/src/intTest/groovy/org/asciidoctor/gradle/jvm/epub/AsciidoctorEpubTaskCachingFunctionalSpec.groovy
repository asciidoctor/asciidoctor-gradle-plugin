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
package org.asciidoctor.gradle.jvm.epub

import org.asciidoctor.gradle.jvm.epub.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.CachingTest
import spock.lang.IgnoreIf

import static org.asciidoctor.gradle.testfixtures.JRubyTestVersions.AJ20_SAFE_MAXIMUM

class AsciidoctorEpubTaskCachingFunctionalSpec extends FunctionalSpecification implements CachingTest {
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

    // kindlegen is only available as a 32-bit executable and won't run on MacOS Catalina
    @IgnoreIf({ isWindowsOr64bitOnlyMacOS() })
    void "Epub task is not cached when format changes"() {
        given:
        getBuildFile(singleFormatConfiguration('EPUB3'))

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo(singleFormatConfiguration('KF8'))
        assertDefaultTaskExecutes()

        then:
        file('build/docs/asciidocEpub/epub3.mobi').exists()

        and:
        assertDefaultTaskIsCachedAndRelocatable()
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
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'org.asciidoctor.jvm.epub'
            }
            
            ${scan ? buildScanConfiguration : ''}
            ${offlineRepositories}
            
            ${extraContent}
        """
        buildFile
    }

    String singleFormatConfiguration(String format) {
        """
            asciidoctorEpub {
                sourceDir 'src/docs/asciidoc'
                ebookFormats ${format}
    
                kindlegen {
                    agreeToTermsOfUse = true
                }
    
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
