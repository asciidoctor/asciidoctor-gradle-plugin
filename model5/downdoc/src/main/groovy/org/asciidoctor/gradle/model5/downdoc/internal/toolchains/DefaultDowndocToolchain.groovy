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
package org.asciidoctor.gradle.model5.downdoc.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.toolchains.DefaultProcessingOptions
import org.asciidoctor.gradle.model5.core.toolchains.AbstractAsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.asciidoctor.gradle.model5.downdoc.DowndocModel
import org.asciidoctor.gradle.model5.downdoc.engines.DowndocNodeEngine
import org.asciidoctor.gradle.model5.downdoc.toolchains.DowndocToolchain
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Default implementation of {@link DowndocToolchain} that provides support for converting
 * AsciiDoc content using {@code downdoc}.
 * <p>
 * This implementation delegates Node.js engine capabilities to {@link DowndocNodeEngine}
 * and processing options to {@link ProcessingOptions}.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class DefaultDowndocToolchain extends AbstractAsciidoctorToolchain implements DowndocToolchain {

    @Delegate
    private final DowndocNodeEngine engine

    @Delegate
    private final ProcessingOptions processingOptions

    /**
     * Creates a new toolchain instance.
     *
     * @param name The name of this toolchain instance
     * @param project The Gradle project this toolchain belongs to
     */
    @Inject
    DefaultDowndocToolchain(String name, Project project) {
        super(name, project)
        final objectFactory = project.objects

        this.engine = objectFactory.newInstance(DowndocNodeEngine, name)
        this.processingOptions = objectFactory.newInstance(DefaultProcessingOptions)
    }

    /**
     * A list of tasks that will perform toolchain-related preparation before conversion using the toolchain can start.
     *
     * @return List of task names. Can be empty, but never {@code null}
     */
    @Override
    Iterable<String> getToolchainPreparationTaskNames() {
        [DowndocModel.toolchainPrepareTaskName(name)]
    }

    /**
     * A string representing the class name as it should be used in the DSL.
     *
     * @return Display type for report. Can be {code null}.
     */
    @Override
    String getDisplayType() {
        DowndocToolchain.canonicalName
    }
}
