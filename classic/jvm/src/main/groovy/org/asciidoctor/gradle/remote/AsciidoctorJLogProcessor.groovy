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
package org.asciidoctor.gradle.remote

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.asciidoctor.ast.Cursor
import org.asciidoctor.gradle.internal.ExecutorLogLevel
import org.asciidoctor.log.LogHandler
import org.asciidoctor.log.LogRecord
import org.asciidoctor.log.Severity

import java.util.regex.Pattern

/**
 * How to deal with failures coming out of AsciidoctorJ.
 *
 * @author Schalk W. Cronjé
 *
 * @since 4.0
 */
@CompileStatic
@Slf4j
class AsciidoctorJLogProcessor implements Serializable {

    private int maxSeverityLevel
    private final int failureLevel
    private final List<String> warningMessages = []
    private final List<Pattern> messagePatterns = []

    AsciidoctorJLogProcessor(
            int maxSeverityLevel,
            int failureLevel
    ) {
        this.maxSeverityLevel = maxSeverityLevel
        this.failureLevel = failureLevel
    }

    /**
     * If any warning message was set, fail with an exception.
     */
    void failOnWarnings() {
        if (!warningMessages.empty) {
            final String msg = 'ERROR: The following messages from AsciidoctorJ are treated as errors:\n' +
                    warningMessages.join('\n- ')
            throw new AsciidoctorRemoteExecutionException(msg)
        }
    }

    /** If failure level is reached or exceed, fail with an exception.
     *
     */
    void failOnFailureLevelReachedOrExceeded() {
        if (maxSeverityLevel >= failureLevel) {
            Severity maxSeverity = LogSeverityMapper.getSeverityOf(maxSeverityLevel)
            Severity failureSeverity = LogSeverityMapper.getSeverityOf(failureLevel)
            throw new AsciidoctorRemoteExecutionException('ERROR: Failure level reached or exceeded: ' +
                    "${maxSeverity} >= $failureSeverity")
        }
    }

    /**
     * Creates a log handler for Asciidoctor
     *
     * @param required The required level of logging
     * @return A log handler instance suitable for registering with AsciidoctorJ.
     */
    LogHandler getLogHandler(ExecutorLogLevel required) {
        int requiredLevel = required.level
        new LogHandler() {
            @Override
            void log(LogRecord logRecord) {
                ExecutorLogLevel logLevel = LogSeverityMapper.translateAsciidoctorLogLevel(logRecord.severity)
                if (logLevel.level > maxSeverityLevel) {
                    maxSeverityLevel = logLevel.level
                }
                if (logLevel.level >= requiredLevel) {
                    String msg = logRecord.message
                    Cursor cursor = logRecord.cursor
                    if (cursor) {
                        final String cPath = cursor.path ?: ''
                        final String cDir = cursor.dir ?: ''
                        final String cFile = cursor.file ?: ''
                        final String cLine = cursor.lineNumber >= 0 ? cursor.lineNumber.toString() : ''
                        msg = "${msg} :: ${cPath} :: ${cDir}/${cFile}:${cLine}"
                    }
                    if (logRecord.sourceFileName) {
                        final String subMsg = logRecord.sourceMethodName ? (':' + logRecord.sourceMethodName) : ''
                        msg = "${msg} (${logRecord.sourceFileName}${subMsg})"
                    }
                    logMessage(logLevel, msg)
                }

                addMatchingMessage(logRecord.message)
            }
        }
    }

    /**
     * The list of warning messages that was recorded during the conversion.
     *
     * @return List of recorded warning messages
     */
    List<String> getWarningMessages() {
        this.warningMessages
    }

    /**
     * Forwards the message to Slf4j.
     *
     * @param logLevel The level of the message
     * @param msg Message to be logged
     */
    void logMessage(ExecutorLogLevel level, String msg) {
        switch (level) {
            case ExecutorLogLevel.DEBUG:
                log.debug(msg)
                break
            case ExecutorLogLevel.INFO:
                log.info(msg)
                break
            case ExecutorLogLevel.WARN:
                log.warn(msg)
                break
            case ExecutorLogLevel.ERROR:
                log.error(msg)
                break
            case ExecutorLogLevel.QUIET:
                log.error(msg)
                break
        }
    }

    /** Patterns for matching log messages as errors
     *
     * @param patterns List of patterns. Can be empty.
     */
    void resetMessagePatternsTo(final List<Pattern> patterns) {
        this.messagePatterns.clear()
        this.messagePatterns.addAll(patterns)
    }

    /** Adds a warning message that fits a pattern.
     *
     * @param msg
     */
    private void addMatchingMessage(final String msg) {
        if (!this.messagePatterns.empty) {
            if (this.messagePatterns.any { msg =~ it }) {
                this.warningMessages.add msg
            }
        }
    }
}
