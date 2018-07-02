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

    static ExecutorLogLevel translateAsciidoctorLogLevel(Severity sev) {
        switch (sev) {
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
}
