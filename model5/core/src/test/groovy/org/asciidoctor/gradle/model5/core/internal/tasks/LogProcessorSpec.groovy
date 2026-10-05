/*
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.model5.core.internal.tasks

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.asciidoctor.gradle.model5.core.errors.ConversionWarningException
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification
import org.gradle.api.file.Directory
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.StringTools

import static org.asciidoctor.gradle.model5.core.internal.tasks.LogProcessor.LOG_EVENTS_FILE_PREFIX

class LogProcessorSpec extends UnitTestSpecification {

    StringTools stringTools
    Directory logDir

    void setup() {
        stringTools = ConfigCacheSafeOperations.from(project).stringTools()
        logDir = project.layout.projectDirectory.dir('logs')
        logDir.asFile.mkdirs()

        writeEvents(1, [
            [severity: 'WARN', message: 'section title out of sequence', path: 'a.adoc', line: '3']
        ])
        writeEvents(2, [
            [severity: 'WARN', message: 'section title out of sequence', path: 'b.adoc', line: '5'],
            [severity: 'ERROR', message: 'include file not found: missing.adoc', path: 'b.adoc', line: '7']
        ])
    }

    void 'Records from all log event files are written to a single array'() {
        when:
        LogProcessor.parseLogs(stringTools, logDir, [] as Set, 3)
        final records = (List<Map>) new JsonSlurper().parse(logDir.file('log.json').asFile)

        then:
        records*.path == ['a.adoc', 'b.adoc', 'b.adoc']
        records*.line == ['3', '5', '7']
        new JsonSlurper().parse(logDir.file('errors.json').asFile) == []
        !logDir.file("${LOG_EVENTS_FILE_PREFIX}.1").asFile.exists()
        !logDir.file("${LOG_EVENTS_FILE_PREFIX}.2").asFile.exists()
    }

    void 'Only matching records are written to errors.json'() {
        when:
        LogProcessor.parseLogs(stringTools, logDir, [~/include file not found/] as Set, 3)

        then:
        final e = thrown(ConversionWarningException)
        e.message.startsWith('1 fatal issues where discovered.')

        when:
        final errors = (List<Map>) new JsonSlurper().parse(logDir.file('errors.json').asFile)

        then:
        errors.size() == 1
        errors[0].message == 'include file not found: missing.adoc'
        errors[0].matches == ['include file not found']
    }

    private void writeEvents(int index, List<Map> records) {
        logDir.file("${LOG_EVENTS_FILE_PREFIX}.${index}").asFile.text = JsonOutput.toJson(records)
    }
}
