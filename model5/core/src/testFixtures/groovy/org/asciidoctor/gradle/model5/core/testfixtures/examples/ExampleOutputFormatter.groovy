/*
 * Copyright ${year} the original author or authors.
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
package org.asciidoctor.gradle.model5.core.testfixtures.examples

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.waitingroom.AbstractOutputFormatter
import org.asciidoctor.gradle.model5.core.waitingroom.AsciidoctorPublication
import org.asciidoctor.gradle.model5.toolchains.AsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.waitingroom.AsciidoctorOutputFormatter
import org.asciidoctor.gradle.model5.toolchains.ToolchainUtils
import org.gradle.api.Project

import javax.inject.Inject

@CompileStatic
class ExampleOutputFormatter extends AbstractOutputFormatter implements AsciidoctorOutputFormatter {

    private final ExampleToolchain toolchain

    @Inject
    ExampleOutputFormatter(String name, ExampleToolchain toolchain, Project tempProjectReference) {
        super(name, tempProjectReference)
        this.toolchain = toolchain
    }

    @Override
    void registerTasksIfAbsent(AsciidoctorPublication publication) {
        final taskName = getAsciidoctorTaskName(publication)
        ToolchainUtils.registerOnce(taskName, ExampleAsciidoctorTask, objectFactory) {
            it.group = 'Documentation'
            it.description = 'Example output generator'
            ToolchainUtils.configureFromStandardPublicationProviders(it, publication)
        }
    }

    @Override
    Class<?> getOutputFormatterClass() {
        ExampleOutputFormatter.class
    }

    @Override
    protected AsciidoctorToolchain getAsciidoctorToolchain() {
        this.toolchain
    }
}
