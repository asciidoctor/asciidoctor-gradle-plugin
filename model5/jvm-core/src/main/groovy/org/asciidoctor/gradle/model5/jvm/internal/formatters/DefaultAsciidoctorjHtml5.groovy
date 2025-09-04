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
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjDocbook
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 * HTML5 backend.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjHtml5 extends AbstractAsciidoctorjFormatter implements AsciidoctorjHtml5 {
    public static final String DEFAULT_NAME = 'html'
    public static final String BACKEND_NAME = 'html5'

    final boolean copyResources = true
    private final Property<Boolean> embedded

    @Inject
    DefaultAsciidoctorjHtml5(String name, AsciidoctorjToolchain tc, Project project) {
        super(name,BACKEND_NAME, tc,project)
        this.embedded = project.objects.property(Boolean).convention(false)
    }

    @Override
    void setEmbedded(boolean flag) {
        this.embedded.set(flag)
    }

    @Override
    Provider<Boolean> getEmbedded() {
        this.embedded
    }

    protected final Class<?> getDslType() {
        AsciidoctorjHtml5
    }
}
