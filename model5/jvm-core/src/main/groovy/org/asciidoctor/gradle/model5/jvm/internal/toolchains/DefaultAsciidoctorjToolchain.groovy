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
package org.asciidoctor.gradle.model5.jvm.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.toolchains.DefaultProcessingOptions
import org.asciidoctor.gradle.model5.core.toolchains.AbstractAsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.asciidoctor.gradle.model5.jvm.engines.AsciidoctorjEngine
import org.asciidoctor.gradle.model5.jvm.engines.EngineOptions
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Action
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.ClosureUtils

import javax.inject.Inject

@CompileStatic
class DefaultAsciidoctorjToolchain extends AbstractAsciidoctorToolchain implements AsciidoctorjToolchain {

    @Delegate
    private final AsciidoctorjEngine engine

    @Delegate
    private final ProcessingOptions processingOptions

    @Inject
    DefaultAsciidoctorjToolchain(String name, Project project) {
        super(name, project)
        final objectFactory = project.objects

        this.engine = objectFactory.newInstance(AsciidoctorjEngine, name)
        this.processingOptions = objectFactory.newInstance(DefaultProcessingOptions)
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
    void engineOptions(@DelegatesTo(EngineOptions.class) Closure<?> configurator) {
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

//    @Override
//    Provider<Map<String, String>> getOptions() {
//        return null
//    }
//
//    @Override
//    void setOptions(Map<String, ?> m) {
//
//    }
//
//    @Override
//    void options(Map<String, ?> m) {
//
//    }

}
