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
package org.asciidoctor.gradle.model5.js.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.toolchains.DefaultProcessingOptions
import org.asciidoctor.gradle.model5.core.toolchains.AbstractAsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.asciidoctor.gradle.model5.js.JsModel
import org.asciidoctor.gradle.model5.js.engines.AsciidoctorjsNodeEngine
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project

import javax.inject.Inject

@CompileStatic
class DefaultAsciidoctorjsToolchain extends AbstractAsciidoctorToolchain implements AsciidoctorjsToolchain {

    @Delegate
    private final AsciidoctorjsNodeEngine engine

    @Delegate
    private final ProcessingOptions processingOptions

    @Inject
    DefaultAsciidoctorjsToolchain(String name, Project project) {
        super(name, project)
        final objectFactory = project.objects

        this.engine = objectFactory.newInstance(AsciidoctorjsNodeEngine, name)
        this.processingOptions = objectFactory.newInstance(DefaultProcessingOptions)
    }

    /**
     * A list of tasks that will perform toolchain-related preparation before conversion using the toolchain can start.
     *
     * @return List of task names. Can be empty, but never {@code null}
     */
    @Override
    Iterable<String> getToolchainPreparationTaskNames() {
        [JsModel.toolchainPrepareTaskName(name)]
    }
}
