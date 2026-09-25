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
package org.asciidoctor.gradle.model5.reducer.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.internal.toolchains.DefaultProcessingOptions
import org.asciidoctor.gradle.model5.core.toolchains.AbstractAsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.asciidoctor.gradle.model5.jrubytools.GemUtils
import org.asciidoctor.gradle.model5.reducer.engines.AsciidoctorReducerEngine
import org.asciidoctor.gradle.model5.reducer.engines.EngineOptions
import org.asciidoctor.gradle.model5.reducer.toolchains.AsciidoctorReducerToolchain
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleSetJavaForkOptions

import javax.inject.Inject

/**
 * Default implementation of {@link AsciidoctorReducerToolchain} that provides support for converting
 * AsciiDoc content using {@code Reducer}.
 * <p>
 * This implementation delegates Node.js engine capabilities to {@link AsciidoctorReducerEngine}
 * and processing options to {@link ProcessingOptions}.
 *
 * @since 5.1
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class DefaultReducerToolchain extends AbstractAsciidoctorToolchain implements AsciidoctorReducerToolchain {

    private final AsciidoctorReducerEngine engine

    @Delegate
    private final ProcessingOptions processingOptions

    /**
     * Creates a new toolchain instance.
     *
     * @param name The name of this toolchain instance
     * @param project The Gradle project this toolchain belongs to
     */
    @Inject
    DefaultReducerToolchain(String name, Project project) {
        super(name, project)
        final objectFactory = project.objects

        this.engine = objectFactory.newInstance(AsciidoctorReducerEngine, name)
        this.processingOptions = objectFactory.newInstance(DefaultProcessingOptions)
        this
    }

    /**
     * A list of tasks that will perform toolchain-related preparation before conversion using the toolchain can start.
     *
     * @return List of task names. Can be empty, but never {@code null}
     */
    @Override
    Iterable<String> getToolchainPreparationTaskNames() {
        [GemUtils.toolchainPrepareTaskName(name)]
    }

    /**
     * A string representing the class name as it should be used in the DSL.
     *
     * @return Display type for report. Can be {code null}.
     */
    @Override
    String getDisplayType() {
        AsciidoctorReducerToolchain.canonicalName
    }

    @Override
    void useReducer(Object ver) {
        engine.useReducer(ver)
    }

    @Override
    GrolifantSimpleSetJavaForkOptions getForkOptions() {
        engine.forkOptions
    }

    @Override
    EngineOptions getEngineOptions() {
        engine.engineOptions
    }

    @Override
    void useJRuby(Object v) {
        engine.useJRuby(v)
    }

    @Override
    Provider<? extends AsciidoctorLauncher> getLauncher() {
        engine.launcher
    }

    @Override
    void setForkEvery(int count) {
        engine.forkEvery = count
    }
}
