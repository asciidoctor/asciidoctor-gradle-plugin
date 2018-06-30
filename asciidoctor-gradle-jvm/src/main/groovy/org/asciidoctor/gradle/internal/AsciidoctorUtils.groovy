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
package org.asciidoctor.gradle.internal

import groovy.transform.CompileStatic
import org.gradle.api.logging.LogLevel
import org.ysb33r.grolifant.api.OperatingSystem

@CompileStatic
class AsciidoctorUtils {

    static final OperatingSystem OS = OperatingSystem.current()

    private static final String DOUBLE_BACKLASH = '\\\\'
    private static final String BACKLASH = '\\'

    /** Normalises slashes in a path.
     *
     * @param path
     * @return Slashes chanegs to backslahes no Windows, unahcnges otherwise.
     */
    static String normalizePath(String path) {
        if (OS.windows) {
            path = path.replace(DOUBLE_BACKLASH, BACKLASH)
            path = path.replace(BACKLASH, DOUBLE_BACKLASH)
        }
        path
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    static String getRelativePath(File target, File base) throws IOException {
        base.toPath().relativize(target.toPath()).toFile().toString()
    }

    /** Determines an executor logging level from the current Gradle logging level
     *
     * @param level
     * @return
     */
    static ExecutorLogLevel getExecutorLogLevel(LogLevel level) {
        switch(level) {
            case LogLevel.DEBUG:
                return ExecutorLogLevel.DEBUG
            case LogLevel.LIFECYCLE:
            case LogLevel.WARN:
                return ExecutorLogLevel.WARN
            case LogLevel.INFO:
                return ExecutorLogLevel.INFO
            case LogLevel.QUIET:
                return ExecutorLogLevel.QUIET
            default:
                return ExecutorLogLevel.ERROR
        }
    }
}
