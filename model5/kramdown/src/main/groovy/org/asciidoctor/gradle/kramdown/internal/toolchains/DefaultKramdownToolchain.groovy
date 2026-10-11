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
package org.asciidoctor.gradle.kramdown.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.kramdown.KramdownLauncher
import org.asciidoctor.gradle.kramdown.MarkdownFormat
import org.asciidoctor.gradle.kramdown.engines.DefaultKramdownEngine
import org.asciidoctor.gradle.kramdown.formatters.KramdownOutputFormatter
import org.asciidoctor.gradle.kramdown.internal.formatters.DefaultKramdownOutputFormatter
import org.asciidoctor.gradle.kramdown.toolchains.KramdownToolchain
import org.asciidoctor.gradle.model5.core.HasDisplayType
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.asciidoctor.gradle.model5.jrubytools.GemUtils
import org.asciidoctor.gradle.model5.reducer.engines.AsciidoctorReducerEngine
import org.asciidoctor.gradle.model5.reducer.engines.EngineOptions
import org.asciidoctor.gradle.model5.reducer.toolchains.AsciidoctorReducerToolchain
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
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
class DefaultKramdownToolchain implements KramdownToolchain, HasDisplayType {

    static class Factory implements NamedDomainObjectFactory<KramdownToolchain> {

        private final ObjectFactory objectFactory

        @Inject
        Factory(Project project) {
            this.objectFactory = project.objects
        }

        @Override
        KramdownToolchain create(String name) {
            objectFactory.newInstance(DefaultKramdownToolchain, name)
        }
    }

    final String name
    final NamedDomainObjectContainer<KramdownOutputFormatter> registeredOutputFormatters
    private final DefaultKramdownEngine engine
    private final Property<MarkdownFormat> format

    /**
     * Creates a new toolchain instance.
     *
     * @param name The name of this toolchain instance
     * @param project The Gradle project this toolchain belongs to
     */
    @Inject
    DefaultKramdownToolchain(String name, Project project) {
        this.name = name
        final objectFactory = project.objects
        this.engine = objectFactory.newInstance(DefaultKramdownEngine, name)
        this.format = objectFactory.property(MarkdownFormat).convention(MarkdownFormat.GFM)
        this.registeredOutputFormatters = objectFactory.domainObjectContainer(
            KramdownOutputFormatter,
            objectFactory.newInstance(DefaultKramdownOutputFormatter.Factory)
        )
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
        KramdownToolchain.canonicalName
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
    Provider<? extends KramdownLauncher> getLauncher() {
        engine.launcher
    }

    @Override
    void setForkEvery(int count) {
        engine.forkEvery = count
    }

    @Override
    void setFormat(MarkdownFormat fmt) {
        this.format.set(fmt)
    }

    @Override
    Provider<MarkdownFormat> getFormat() {
        this.format
    }

    @Override
    void useKramdownAsciidoc(Object ver) {
        engine.useKramdownAsciidoc(ver)
    }
}
