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
package org.asciidoctor.gradle.model5.downdoc.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.downdoc.formatters.DowndocMarkdownOutputFormatter
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider

import javax.inject.Inject

import static java.util.Collections.EMPTY_MAP
import static java.util.Collections.EMPTY_SET
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Implementation of {@link DowndocMarkdownOutputFormatter}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultDowndocMarkdownOutputFormatter implements DowndocMarkdownOutputFormatter {

    static class Factory implements NamedDomainObjectFactory<DowndocMarkdownOutputFormatter> {
        private final ObjectFactory objectFactory

        @Inject
        Factory(Project project) {
            this.objectFactory = project.objects
        }

        DowndocMarkdownOutputFormatter create(String name) {
            objectFactory.newInstance(DefaultDowndocMarkdownOutputFormatter, name)
        }
    }

    public static final String DEFAULT_NAME = 'markdown'
    public static final String BACKEND_NAME = EMPTY

    final String name
    final boolean copyResources = true
    final FileCollection classpath = null
    final Provider<Map<String, Object>> attributeProvider
    final Provider<Set<String>> requires
    final Provider<AsciidoctorNamedBackend> backend

    @Inject
    DefaultDowndocMarkdownOutputFormatter(String name, Project project) {
        this.name = name
        this.attributeProvider = project.provider { -> EMPTY_MAP }
        this.requires = project.provider { -> EMPTY_SET }
        this.backend = project.provider { -> AsciidoctorNamedBackend.of(owner.name, BACKEND_NAME) }
    }
}
