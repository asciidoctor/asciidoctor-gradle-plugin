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
package org.asciidoctor.gradle.model5.reducer.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.engines.AsciidoctorEngine
import org.asciidoctor.gradle.model5.jrubytools.engines.BaseJRubyEngine
import org.asciidoctor.gradle.model5.reducer.ReducerModel
import org.asciidoctor.gradle.model5.reducer.internal.engines.DefaultLauncher
import org.asciidoctor.gradle.model5.reducer.internal.utils.PluginUtils
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 * The core engine for running {@code Reducer}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class AsciidoctorReducerEngine extends BaseJRubyEngine implements AsciidoctorEngine {

    public static final String REDUCER_PROP = 'reducer'
    public static final String REDUCER_PACKAGE = 'asciidoctor-reducer'

    private final Property<String> reducerVersion
    private final Property<DefaultLauncher> jvmLauncher

    @Inject
    AsciidoctorReducerEngine(String name, Project tempProjectReference) {
        super(
            name,
            ReducerModel.nameForEngineConfiguration(name),
            ReducerModel.nameForGemsConfiguration(name),
            tempProjectReference
        )

        this.reducerVersion = ccso.providerTools().property(String).convention(
            PluginUtils.loadDefaultVersion(REDUCER_PROP, tempProjectReference, this.class.classLoader)
        )
        tempProjectReference.dependencies.addProvider(gemsConfigurationName, reducerVersion.map {
            "rubygems:${REDUCER_PACKAGE}:${it}"
        })

        this.jvmLauncher = ccso.providerTools().property(DefaultLauncher)
            .value(createLauncher(tempProjectReference))
        this.jvmLauncher.finalizeValue()
    }

    /**
     * Overrides the default version of the {@code Reducer} engine.
     *
     * @param ver Lazy-evaluated version
     */
    void useReducer(Object ver) {
        ccso.stringTools().updateStringProperty(this.reducerVersion, ver)
    }

    @Override
    Provider<? extends AsciidoctorLauncher> getLauncher() {
        this.jvmLauncher
    }

    private DefaultLauncher createLauncher(Project tempProjectReference) {
        final cp = this.classpath
        tempProjectReference.objects.newInstance(
            DefaultLauncher,
            this.forkOptions,
            this.configuredEngineOptions
        ).tap {
            classpath(cp)
            forkEvery = forkAfterDocCount
        }
    }
}
