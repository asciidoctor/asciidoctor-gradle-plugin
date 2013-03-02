/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidoctor.gradle

import spock.lang.Specification

/**
 * Asciidoctor worker specification.
 */
class JRubyAsciidoctorWorkerSpec extends Specification {
    File srcDir = new File('build/resources/test/src/asciidoc')
    File outputDir = new File('build/asciidoc-output')

    def setup() {
        if(!outputDir.exists()) {
            outputDir.mkdir()
        }
    }

    def "Renders sample docbook"() {
        when:
            AsciidoctorWorker worker = new JRubyAsciidoctorWorker()
            worker.execute(srcDir, outputDir, AsciidoctorBackend.DOCBOOK.id)
        then:
            !outputDir.list().toList().isEmpty()
            outputDir.list().toList().contains('sample.xml')

            File sampleOutput = new File(outputDir, 'sample.xml')
            sampleOutput.exists()
            sampleOutput.length() > 0
    }

    def "Renders sample HTML"() {
        when:
            AsciidoctorWorker worker = new JRubyAsciidoctorWorker()
            worker.execute(srcDir, outputDir, AsciidoctorBackend.HTML5.id)
        then:
            !outputDir.list().toList().isEmpty()
            outputDir.list().toList().contains('sample.html')

            File sampleOutput = new File(outputDir, 'sample.html')
            sampleOutput.exists()
            sampleOutput.length() > 0
    }
}
