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
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.errors.ConversionWarningException
import org.gradle.api.file.Directory

import java.util.regex.Pattern

/**
 * Post process logs from Asciidoctor engines.
 *
 * @author Schalk W. Cronjé
 * @author Mattias Reichel
 *
 * @since 5.0
 */
@CompileStatic
class LogProcessor {
    public final static String LOG_EVENTS_FILE_PREFIX = 'log-events'
    public static final String LOG_SUBPATH = 'reports/asciidoc/logs'
    public final static String OPEN_RECORDS = '['
    public final static String CLOSE_RECORDS = ']'

    private final static String MATCHES = 'matches'

    /**
     * Parses JSON logs from Asciidoctor executions.
     *
     * @param dir Log directory
     * @param patterns Patterns to match against.
     * @param maxIndex Maximum number of JSON files.
     */
    static void parseLogs(
        Directory dir,
        Set<Pattern> patterns,
        int maxIndex
    ) {
        final slurper = new JsonSlurper()
        final logFile = dir.file('log.json')
        final errorFile = dir.file('errors.json')
        final records = []
        (1..maxIndex).each {
            final eventFile = dir.file("${LOG_EVENTS_FILE_PREFIX}.${it}").asFile
            if (eventFile.exists()) {
                records.addAll((List) slurper.parse(eventFile))
                eventFile.delete()
            }
        }

        logFile.asFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(records))
        final matched = findMatches(records, patterns)
        final errors = records.findAll { ((Map) it).containsKey(MATCHES) }
        errorFile.asFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(errors))

        if (matched) {
            throw new ConversionWarningException(
                "${matched} fatal issues were discovered.\nSee ${errorFile.asFile.toPath().toUri()}."
            )
        }
    }

    static private int findMatches(Object json, Set<Pattern> patterns) {
        int matches = 0
        ((Iterable) json).each {
            final map = (Map) it
            final msg = map['message']?.toString()
            if (msg) {
                final mapping = patterns.findAll { pat ->
                    msg.find(pat)
                }*.toString()
                if (!mapping.empty) {
                    map[MATCHES] = mapping
                    matches += mapping.size()
                }
            }
        }
        matches
    }

}
