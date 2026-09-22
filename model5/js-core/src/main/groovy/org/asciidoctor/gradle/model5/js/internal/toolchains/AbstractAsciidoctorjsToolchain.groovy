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
package org.asciidoctor.gradle.model5.js.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.toolchains.DefaultProcessingOptions
import org.asciidoctor.gradle.model5.core.toolchains.AbstractAsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.asciidoctor.gradle.model5.js.JsModel
import org.asciidoctor.gradle.model5.js.engines.AsciidoctorjsNodeEngine
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project

import static org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreBasePlugin.INTERMEDIATE_RESOURCE_PATH

/**
 * Default implementation of {@link AsciidoctorjsToolchain} that provides support for processing
 * AsciiDoc content using Asciidoctor.js.
 * <p>
 * This implementation delegates Node.js engine capabilities to {@link AsciidoctorjsNodeEngine}
 * and processing options to {@link ProcessingOptions}.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
abstract class AbstractAsciidoctorjsToolchain extends AbstractAsciidoctorToolchain implements AsciidoctorjsToolchain {

    public static final String PROPS_RESOURCE = "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-js-core-plugin.properties"

    @Delegate
    private final AsciidoctorjsNodeEngine engine

    @Delegate
    private final ProcessingOptions processingOptions

    /**
     * A list of tasks that will perform toolchain-related preparation before conversion using the toolchain can start.
     *
     * @return List of task names. Can be empty, but never {@code null}
     */
    @Override
    Iterable<String> getToolchainPreparationTaskNames() {
        [JsModel.toolchainPrepareTaskName(name)]
    }

    /**
     * A string representing the class name as it should be used in the DSL.
     *
     * @return Display type for report. Can be {code null}.
     */
    @Override
    String getDisplayType() {
        AsciidoctorjsToolchain.canonicalName
    }

    /**
     * Creates a new toolchain instance.
     *
     * @param name The name of this toolchain instance
     * @parmn propAsciidoctorVer Property for reading the version of the Asciidoctor package.
     * @parmn propAsciidoctorCliVer Property for reading the version of the Asciidoctor Cli package.
     * @param project The Gradle project this toolchain belongs to
     */
    protected AbstractAsciidoctorjsToolchain(
        String name,
        String propAsciidoctorVer,
        String propAsciidoctorCliVer,
        Project project
    ) {
        super(name, project)
        final objectFactory = project.objects

        this.engine = objectFactory.newInstance(
            AsciidoctorjsNodeEngine,
            name,
            propAsciidoctorVer,
            propAsciidoctorCliVer
        )
        this.processingOptions = objectFactory.newInstance(DefaultProcessingOptions)
    }
}
