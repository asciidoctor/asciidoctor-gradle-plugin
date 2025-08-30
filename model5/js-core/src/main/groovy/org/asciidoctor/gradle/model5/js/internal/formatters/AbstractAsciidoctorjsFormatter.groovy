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
package org.asciidoctor.gradle.model5.js.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsOutputFormatter
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

@CompileStatic
abstract class AbstractAsciidoctorjsFormatter implements AsciidoctorjsOutputFormatter {
    final String name
    final Provider<AsciidoctorNamedBackend> backend

    protected final ConfigCacheSafeOperations ccso
    protected final AsciidoctorjsToolchain toolchain

    protected AbstractAsciidoctorjsFormatter(String name, String backendName, AsciidoctorjsToolchain tc, Project project) {
        this.name = name
        this.toolchain = tc
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.backend = ccso.providerTools().provider { -> AsciidoctorNamedBackend.of(name, backendName) }
    }
}
