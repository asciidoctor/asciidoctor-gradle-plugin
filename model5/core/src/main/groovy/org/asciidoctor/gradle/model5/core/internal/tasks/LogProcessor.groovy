/*
 * Copyright 2013 - 2025 the original author or authors.
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
import org.ysb33r.grolifant5.api.core.StringTools

import java.util.regex.Pattern

import static org.ysb33r.grolifant5.api.core.StringTools.SLASH

/**
 * Post process logs from Asciidoctor engines.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class LogProcessor {
    public final static String LOG_EVENTS_FILE_PREFIX = 'log-events'
    public static final String LOG_SUBPATH = 'reports/asciidoc/logs'
    public final static String OPEN_RECORDS = '['
    public final static String CLOSE_RECORDS = ']'

    /**
     * Parses JSON logs from Asciidoctor executions.
     *
     * @param stringTools StringTools to aid in some string processing.
     * @param dir Log directory
     * @param patterns Patterns to match against.
     * @param maxIndex Maximum number of JSON files.
     */
    static void parseLogs(
        StringTools stringTools,
        Directory dir,
        Set<Pattern> patterns,
        int maxIndex
    ) {
        final slurper = new JsonSlurper()
        final logFile = dir.file('log.json')
        final errorFile = dir.file('errors.json')
        int matched = 0
        logFile.asFile.withWriter { log ->
            errorFile.asFile.withWriter { errors ->
                log.println(OPEN_RECORDS)
                errors.println(OPEN_RECORDS)
                (1..maxIndex).each {
                    final eventFile = dir.file("${LOG_EVENTS_FILE_PREFIX}.${it}").asFile
                    if (eventFile.exists()) {
                        final json = slurper.parse(eventFile)
                        log.println(JsonOutput.prettyPrint(JsonOutput.toJson(json)))
                        final matches = findMatches(json, patterns)
                        if (matches > 0) {
                            errors.println(JsonOutput.prettyPrint(JsonOutput.toJson(json)))
                            matched += matches
                        }
                        eventFile.delete()
                    }
                }
                log.println(CLOSE_RECORDS)
                errors.println(CLOSE_RECORDS)
            }
        }

        if (matched) {
            final initMsg = stringTools.stringize(stringTools.urize(errorFile.asFile))
            final finalMsg = initMsg.startsWith('file://') ? initMsg : initMsg.replaceFirst(SLASH, SLASH * 2)
            throw new ConversionWarningException(
                "${matched} fatal issues where discovered.\nSee ${finalMsg}."
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
                    map['matches'] = mapping
                    matches += mapping.size()
                }
            }
        }
        matches
    }

}
