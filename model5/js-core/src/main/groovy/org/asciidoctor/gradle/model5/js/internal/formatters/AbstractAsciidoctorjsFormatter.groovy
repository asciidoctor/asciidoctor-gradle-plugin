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
package org.asciidoctor.gradle.model5.js.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsOutputFormatter
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

/**
 * Base class for implementing an output formatter for {@code asciidoctor.js}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
abstract class AbstractAsciidoctorjsFormatter implements AsciidoctorjsOutputFormatter {
    final String name
    final Provider<AsciidoctorNamedBackend> backend
    final FileCollection classpath = null

    protected final ConfigCacheSafeOperations ccso
    protected final AsciidoctorjsToolchain toolchain

    /**
     * Can be modified by derived classes when additional requires are needed.
     */
    protected final SetProperty<String> packageRequires

    /**
     * Can be modified by derived classes when attributes need to be made available.
     */
    protected final MapProperty<String, Object> attributes

    /**
     * A list of {@code requires} that a component places on the associated toolchain.
     *
     * @return List of {@code requires}. Can be empty, but never {@code null}.
     */
    @Override
    Provider<Set<String>> getRequires() {
        this.packageRequires
    }

    /**
     * Indicates that something can provide unresolved attributes.
     *
     * @return Provider to a map of unresolved attributes.
     */
    @Override
    Provider<Map<String, Object>> getAttributeProvider() {
        this.attributes
    }

    /**
     * A string representing the class name as it should be used in the DSL.
     *
     * @return Display type for report.
     */
    @Override
    String getDisplayType() {
        dslType.canonicalName
    }

    protected AbstractAsciidoctorjsFormatter(
        String name,
        String backendName,
        AsciidoctorjsToolchain tc,
        Project project
    ) {
        this.name = name
        this.toolchain = tc
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.backend = ccso.providerTools().provider { -> AsciidoctorNamedBackend.of(name, backendName) }
        this.packageRequires = project.objects.setProperty(String)
        this.attributes = project.objects.mapProperty(String, Object)
    }

    /**
     * The type that this implements and which should be displayed.
     *
     * @return A type that needs to be displayed.
     */
    abstract protected Class<?> getDslType()
}
