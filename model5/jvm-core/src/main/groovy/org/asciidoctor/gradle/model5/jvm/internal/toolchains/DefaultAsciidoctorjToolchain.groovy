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
package org.asciidoctor.gradle.model5.jvm.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.toolchains.DefaultProcessingOptions
import org.asciidoctor.gradle.model5.core.toolchains.AbstractAsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.asciidoctor.gradle.model5.jvm.engines.AsciidoctorjEngine
import org.asciidoctor.gradle.model5.jvm.engines.EngineOptions
import org.asciidoctor.gradle.model5.jvm.engines.ExecutionContext
import org.asciidoctor.gradle.model5.jvm.internal.gems.GemUtils
import org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjGemsPlugin
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ClosureUtils

import javax.inject.Inject

import static java.util.Collections.EMPTY_LIST

/**
 * Default implementation of the {@come asciidoctorj} toolchain.
 *
 * <p>
 * This class provides the core functionality for running Asciidoctor document processing using
 * the AsciidoctorJ implementation. It handles configuration of the engine options, processing
 * options, and preparation tasks required for document generation.
 * <p>
 *
 * When the {@link AsciidoctorjGemsPlugin} is applied, this implementation automatically adds
 * the necessary gem and jar preparation tasks to the toolchain.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class DefaultAsciidoctorjToolchain extends AbstractAsciidoctorToolchain implements AsciidoctorjToolchain {

    private final ListProperty<String> prepareTasks

    @Delegate
    private final AsciidoctorjEngine engine

    @Delegate
    private final ProcessingOptions processingOptions

    /**
     * Creates a new instance of the AsciidoctorJ toolchain.
     *
     * @param name The name of this toolchain instance
     * @param project The Gradle project this toolchain belongs to
     */
    @Inject
    DefaultAsciidoctorjToolchain(String name, Project project) {
        super(name, project)
        final objectFactory = project.objects

        this.engine = objectFactory.newInstance(AsciidoctorjEngine, name)
        this.processingOptions = objectFactory.newInstance(DefaultProcessingOptions)
        this.prepareTasks = objectFactory.listProperty(String).convention(EMPTY_LIST)

        project.pluginManager.withPlugin(AsciidoctorjGemsPlugin.PLUGIN_ID) {
            addPrepareTasks(
                GemUtils.nameForGemPrepareTask(name),
                GemUtils.nameForJarPrepareTask(name)
            )
        }
    }

    /**
     * Configures additional engine options.
     *
     * @param configurator Configurator which is passed an instance of {@link EngineOptions}
     */
    @Override
    void engineOptions(Action<EngineOptions> configurator) {
        configurator.execute(engine)
    }

    /**
     * Configures additional engine options.
     *
     * @param configurator Configurator which is passed an instance of {@link EngineOptions}
     */
    @Override
    void engineOptions(@DelegatesTo(EngineOptions) Closure<?> configurator) {
        ClosureUtils.configureItem(engine, configurator)
    }

    /**
     * Directo access to engine options.
     *
     * @return Instance of something that implements {@link EngineOptions}
     */
    @Override
    EngineOptions getEngineOptions() {
        engine
    }

    /**
     * An interface primarily used by output formatters to register context.
     *
     * @param formatter An active {@code asciidoctorj} formatter.
     * @param executionContext Valid execution context. The provider can be empty which will mean the same as no
     *                         execution  context.
     */
    @Override
    void registerExecutionContext(String formatter, Provider<ExecutionContext> executionContext) {
        engine.registerExecutionContext(name, formatter, executionContext)
    }

    /**
     * A string representing the class name as it should be used in the DSL.
     *
     * @return Display type for report. Can be {code null}.
     */
    @Override
    String getDisplayType() {
        AsciidoctorjToolchain.canonicalName
    }

    /**
     * By default this is empty, but if the GEM plugin is applied, two more tasks will appear in this list.
     *
     * @return List of preparation tasks.
     */
    @Override
    Iterable<String> getToolchainPreparationTaskNames() {
        prepareTasks.get()
    }

    /**
     * Add additional prepare tasks.
     *
     * @param tasks One of more tasks to add.
     */
    void addPrepareTasks(String... tasks) {
        this.prepareTasks.addAll(tasks)
    }
}
