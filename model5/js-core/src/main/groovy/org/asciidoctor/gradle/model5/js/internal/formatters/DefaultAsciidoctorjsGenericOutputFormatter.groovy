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
import org.asciidoctor.gradle.model5.core.DocType
import org.asciidoctor.gradle.model5.core.errors.IncorrectOutputFormatException
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsGenericOutputFormatter
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * A generic output formatter for {@code asciidoctor.js}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjsGenericOutputFormatter implements AsciidoctorjsGenericOutputFormatter {

    final String name
    final FileCollection classpath = null
    private final ConfigCacheSafeOperations ccso
    private final ObjectFactory objectFactory
    private final Property<AsciidoctorNamedBackend> backend
    private final MapProperty<String, Object> attributes
    private final SetProperty<String> requires
    private final AsciidoctorjsToolchain toolchain
    private Boolean copyResources
    private Optional<DocType> docType

    @Inject
    DefaultAsciidoctorjsGenericOutputFormatter(String name, AsciidoctorjsToolchain tc, Project project) {
        this.name = name
        this.toolchain = tc
        this.objectFactory = project.objects
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.attributes = objectFactory.mapProperty(String, Object)
        this.requires = objectFactory.setProperty(String)
        this.copyResources = false
        this.docType = Optional.empty()
        this.backend = objectFactory.property(AsciidoctorNamedBackend).convention(project.provider { ->
            throw new IncorrectOutputFormatException("A backend must be defined for ${tc.name}:${name}")
        })
    }

    @Override
    void usePackage(String scope, String moduleName, Object version) {
        toolchain.usePackage(scope, moduleName, ccso.stringTools().provideString(version))
    }

    @Override
    void requires(String... reqs) {
        this.requires.addAll(reqs)
    }

    @Override
    void attributes(Map<String, ?> attrs) {
        this.attributes.putAll(attrs)
    }

    @Override
    void setBackend(String backendName) {
        this.backend.set(AsciidoctorNamedBackend.of(name, backendName))
    }

    @Override
    void setCopyResources(boolean flag) {
        this.copyResources = flag
    }

    @Override
    void setEnforcedDocType(String doctype) {
        this.docType = Optional.of(DocType.valueOf(doctype.toUpperCase(Locale.US)))
    }

    @Override
    Provider<AsciidoctorNamedBackend> getBackend() {
        this.backend
    }

    @Override
    boolean getCopyResources() {
        this.copyResources
    }

    @Override
    Provider<Set<String>> getRequires() {
        this.requires
    }

    @Override
    Provider<Map<String, Object>> getAttributeProvider() {
        this.attributes
    }

    @Override
    String getDisplayType() {
        AsciidoctorjsGenericOutputFormatter.canonicalName
    }
}
