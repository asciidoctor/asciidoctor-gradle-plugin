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
package org.asciidoctor.gradle.internal

import spock.lang.Specification

import static org.asciidoctor.gradle.internal.ExecutorLogLevel.DEBUG

class ExecutorConfigurationSpec extends Specification {
    void 'toString provides report'() {
        given:
        File fake = new File('.')
        def config = new ExecutorConfiguration(
            sourceDir : fake,
            outputDir: fake,
            projectDir: fake,
            rootDir: fake,
            baseDir: fake,
            sourceTree: [fake] as Set,
            fatalMessagePatterns: [~/./],
            backendName: 'backend',
            gemPath : 'gem:path',
            logDocuments: true,
            copyResources: true,
            legacyAttributes: true,
            safeModeLevel: 0,
            requires: ['bibtex'],
            options:[fooOpt: 'barOpt'],
            attributes: [fooAttr: 'barAttr'],
            asciidoctorExtensions: [],
            executorLogLevel: DEBUG
        )

        when:
        def print = config.toString()

        then:
        print.contains('backend(s) = backend')
        print.contains('1 options')
        print.contains('1 attributes')
    }
}