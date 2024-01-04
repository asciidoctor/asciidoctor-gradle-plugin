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
package org.asciidoctor.gradle.base.process

import groovy.transform.CompileStatic
import org.ysb33r.grolifant.api.core.jvm.ExecutionMode

/** Ways of executing Java processes.
 *
 * @since 3.0 (Relocated from {@code org.asciidoctor.gradle.jvm.ProcessMode} which existed since 2.0.0)
 * @author Schalk W. Cronjé
 *
 * @deprecated Since 4.0. Use {@link ExecutionMode} instead
 */
@CompileStatic
@Deprecated
enum ProcessMode {
    /** Use Gradle worker in-process.
     *
     */
    IN_PROCESS(ExecutionMode.CLASSPATH),

    /** Use out-of-process Gradle worker.
     *
     */
    OUT_OF_PROCESS(ExecutionMode.OUT_OF_PROCESS),

    /** Use a classic out-of-process Java execution.
     *
     */
    JAVA_EXEC(ExecutionMode.JAVA_EXEC)

    final ExecutionMode executionMode

    static ProcessMode fromExecutionMode(ExecutionMode mode) {
        ProcessMode.values().find { it.executionMode == mode } ?: JAVA_EXEC
    }

    private ProcessMode(ExecutionMode em) {
        this.executionMode = em
    }
}