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
// tag::hacking-asciidoctorjs-output-formatter[]
package org.asciidoctor.gradle.model5.js.internal.formatters

import groovy.transform.CompileStatic

// end::hacking-asciidoctorjs-output-formatter[]

import org.asciidoctor.gradle.model5.core.revealjs.RevealjsOptions
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsRevealjs
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project
import org.gradle.api.tasks.TaskInputs

import javax.inject.Inject
import java.util.function.Consumer

import static org.asciidoctor.gradle.model5.js.internal.PluginUtils.loadDefaultVersion

/**
 * Reveal.js backend.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
// tag::hacking-asciidoctorjs-output-formatter[]
@CompileStatic
class DefaultAsciidoctorjsRevealjs extends AbstractAsciidoctorjsFormatterVersioned // <.>
    implements AsciidoctorjsRevealjs { // <.>

    public static final String DEFAULT_NAME = 'revealjs' // <.>
    public static final String BACKEND_NAME = 'revealjs' // <.>

    final boolean copyResources = true // <.>
    final RevealjsOptions revealjsOptions // <.>

    @Delegate
    private final DefaultAsciidoctorjsTemplates templates // <.>

    @Inject
    DefaultAsciidoctorjsRevealjs(String name, AsciidoctorjsToolchain tc, Project project) {
        super(
            name,
            BACKEND_NAME, // <.>
            'asciidoctor', // <.>
            'reveal.js', // <.>
            loadDefaultVersion('asciidoctorjs.revealjs', project, tc.class.classLoader), // <.>
            tc,
            project
        )
        this.revealjsOptions = project.objects.newInstance(RevealjsOptions)
        attributes.putAll(revealjsOptions.attributeProvider)

        this.templates = project.objects.newInstance(DefaultAsciidoctorjsTemplates, tc, { String r -> // <.>
            packageRequires.add(r)
        } as Consumer<String>)
    }

    @Override
    void configureTaskInputs(TaskInputs taskInputs) { // <.>
        revealjsOptions.configureTaskInputs(taskInputs)
    }

    @Override
    protected Class<?> getDslType() {
        AsciidoctorjsRevealjs // <.>
    }
}
// end::hacking-asciidoctorjs-output-formatter[]
