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
package org.asciidoctor.gradle.remote

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.internal.ExecutorLogLevel
@java.lang.SuppressWarnings('NoWildcardImports')
import org.asciidoctor.log.Severity

@java.lang.SuppressWarnings('NoWildcardImports')
import static org.asciidoctor.log.Severity.*

/** Maps from Asciidoctor severities to {@link ExecutorLogLevel} levels.
 *
 * @since 2.0
 */
@CompileStatic
class LogSeverityMapper {

    static ExecutorLogLevel translateAsciidoctorLogLevel(Severity severity) {
        switch (severity) {
            case DEBUG:
                ExecutorLogLevel.DEBUG
                break
            case INFO:
                ExecutorLogLevel.INFO
                break
            case WARN:
                ExecutorLogLevel.WARN
                break
            case FATAL:
                ExecutorLogLevel.QUIET
                break
            case ERROR:
            default:
                ExecutorLogLevel.ERROR
        }
    }

    static Severity getSeverityOf(int severityLevel) {
        switch (severityLevel) {
            case 0:
                return DEBUG
            case 1:
                return INFO
            case 2:
                return WARN
            case 3:
                return ERROR
            case 4:
                return FATAL
            default:
                return ERROR
        }
    }
}
