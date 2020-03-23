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
package org.asciidoctor.gradle.base.process

import groovy.transform.CompileStatic

/**
 * Logging severity (Ruby).
 */
@CompileStatic
@SuppressWarnings('DuplicateNumberLiteral')
enum LoggerSeverity implements Serializable {
    DEBUG(0),
    ERROR(3),
    FATAL(4), // also know as QUIET in ExecutorLogLevel
    INFO(1),
    WARN(2),

    final int level

    private LoggerSeverity(int level) {
        this.level = level
    }

    static LoggerSeverity of(int level) {
        for (value in values()) {
            if (value.level == level) {
                return value
            }
        }
        null
    }
}