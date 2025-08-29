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
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjPdf
import org.asciidoctor.gradle.model5.jvm.internal.PluginUtils
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Implementation of {@code asciidoctorj-pdf} output formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjPdf extends AbstractAsciidoctorJFormatterVersioned implements AsciidoctorjPdf {
    public static final String DEFAULT_NAME = 'pdf'
    public static final String BACKEND_NAME = DEFAULT_NAME
    final boolean copyResources = false

    @Inject
    DefaultAsciidoctorjPdf(String name, AsciidoctorjToolchain tc, Project project) {
        super(
                name,
                BACKEND_NAME,
                JvmModel.ASCIIDOCTORJ_PDF_DEPENDENCY,
                PluginUtils.loadDefaultVersion('asciidoctorj.pdf', project, tc.class.classLoader),
                tc,
                project
        )
    }
}
