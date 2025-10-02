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
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsDocbook
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.js.internal.PluginUtils.loadDefaultVersion

/**
 * Docbook backend.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjsDocbook extends AbstractAsciidoctorjsFormatterVersioned implements AsciidoctorjsDocbook {
    public static final String DEFAULT_NAME = 'docbook'
    public static final String BACKEND_NAME = 'docbook'

    final boolean copyResources = true

    @Inject
    DefaultAsciidoctorjsDocbook(String name, AsciidoctorjsToolchain tc, Project project) {
        super(
            name,
            BACKEND_NAME,
            'asciidoctor',
            'docbook-converter',
            loadDefaultVersion('asciidoctorjs.docbook', project, tc.class.classLoader),
            tc,
            project
        )
    }

    /**
     * The type that this implements and which should be displayed.
     *
     * @return A type that needs to be displayed.
     */
    @Override
    protected Class<?> getDslType() {
        AsciidoctorjsDocbook
    }
}
