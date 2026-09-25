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
package org.asciidoctor.gradle.model5.reducer.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.reducer.formatters.AsciidoctorReducerOutputFormatter
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Implementation of {@link AsciidoctorReducerOutputFormatter}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultReducerAsciidocOutputFormatter implements AsciidoctorReducerOutputFormatter {

    static class Factory implements NamedDomainObjectFactory<AsciidoctorReducerOutputFormatter> {
        private final ObjectFactory objectFactory

        @Inject
        Factory(Project project) {
            this.objectFactory = project.objects
        }

        AsciidoctorReducerOutputFormatter create(String name) {
            objectFactory.newInstance(DefaultReducerAsciidocOutputFormatter, name)
        }
    }

    public static final String DEFAULT_NAME = 'reducedAdoc'
    public static final String BACKEND_NAME = 'reduced-adoc'
    public static final String KEY_PRESERVE_CONDITIONALS = 'preserve-conditionals'

    final String name
    final boolean copyResources = true
    final Provider<AsciidoctorNamedBackend> backend
    final Provider<Map<String, String>> customProperties
    final FileCollection classpath = null
    private final ConfigCacheSafeOperations ccso
    private final MapProperty<String, Object> attributes
    private final Provider<Set<String>> emptyRequires
    private final Property<Boolean> preserveConditionals

    @Inject
    DefaultReducerAsciidocOutputFormatter(String name, Project project) {
        this.name = name
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.backend = project.provider { -> AsciidoctorNamedBackend.of(owner.name, BACKEND_NAME) }
        this.emptyRequires = ccso.providerTools().provider { -> Collections.EMPTY_SET }
        this.attributes = project.objects.mapProperty(String, Object).value([:])
        this.preserveConditionals = project.objects.property(Boolean).value(false)
        this.customProperties = this.preserveConditionals.map {
            (it ? [(KEY_PRESERVE_CONDITIONALS): EMPTY] : [:]) as Map<String,String>
        }
    }

    @Override
    Provider<Set<String>> getRequires() {
        this.emptyRequires
    }

    @Override
    void setPreserveConditionals(boolean flag) {
        this.preserveConditionals.set(flag)
    }

    @Override
    Provider<Map<String, Object>> getAttributeProvider() {
        this.attributes
    }
}
