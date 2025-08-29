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
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.jvm.ExecutionMode
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjOutputFormatter
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

@CompileStatic
abstract class AbstractAsciidoctorjFormatter implements AsciidoctorjOutputFormatter {
    final String name
    final Provider<AsciidoctorNamedBackend> backend

    protected final ConfigCacheSafeOperations ccso
    protected final AsciidoctorjToolchain toolchain

    private final Property<ExecutionMode> executionMode

    /**
     * Sets whether the workers should run in or out of the Gradle process.
     *
     * @param mode Execution mode.
     */
    @Override
    void setExecutionMode(ExecutionMode mode) {
        this.executionMode.set(mode)
    }

    /**
     * Get the execution mode for the formatter.
     *
     * @return Provider to execution mode.
     */
    @Override
    Provider<ExecutionMode> getExecutionMode() {
       this.executionMode
    }

    protected AbstractAsciidoctorjFormatter(String name, String backendName, AsciidoctorjToolchain tc, Project project) {
        this.name = name
        this.toolchain = tc
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.backend = ccso.providerTools().provider { -> AsciidoctorNamedBackend.of(name, backendName) }
        this.executionMode = ccso.providerTools().property(ExecutionMode).convention(ExecutionMode.IN_PROCESS)
    }
}
