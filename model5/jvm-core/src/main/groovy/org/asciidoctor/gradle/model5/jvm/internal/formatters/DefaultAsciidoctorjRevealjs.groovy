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
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.revealjs.RevealjsOptions
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjRevealjs
import org.asciidoctor.gradle.model5.jvm.internal.PluginUtils
import org.asciidoctor.gradle.model5.jvm.internal.gems.GemUtils
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.tasks.TaskInputs

import javax.inject.Inject

/**
 * Implementation of the {@code asciidoctorj} {@code reveal.js} output formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjRevealjs extends AbstractAsciidoctorjFormatterVersioned implements AsciidoctorjRevealjs {
    public static final String DEFAULT_NAME = 'revealjs'
    public static final String BACKEND_NAME = DEFAULT_NAME

    final boolean copyResources = true
    final RevealjsOptions revealjsOptions

    @Delegate
    private final DefaultAsciidoctorjTemplates templates

    @Inject
    DefaultAsciidoctorjRevealjs(String name, AsciidoctorjToolchain tc, Project project) {
        super(
                name,
                BACKEND_NAME,
                JvmModel.ASCIIDOCTORJ_REVEALJS_DEPENDENCY,
                PluginUtils.loadDefaultVersion('asciidoctorj.revealjs', project, tc.class.classLoader),
                tc,
                project
        )

        this.revealjsOptions = project.objects.newInstance(RevealjsOptions)
        attributes.putAll(revealjsOptions.attributeProvider)

        this.templates = project.objects.newInstance(
            DefaultAsciidoctorjTemplates,
            GemUtils.nameForToolchainConfiguration(tc.name)
        )
    }

    @Override
    void configureTaskInputs(TaskInputs taskInputs) {
        revealjsOptions.configureTaskInputs(taskInputs)
    }

    @Override
    protected Class<?> getDslType() {
        AsciidoctorjRevealjs
    }
}
