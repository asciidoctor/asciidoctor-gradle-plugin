/**
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
package org.asciidoctor.gradle.model5.reducer.toolchains;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.asciidoctor.gradle.model5.core.toolchains.AsciidoctorToolchain;
import org.asciidoctor.gradle.model5.reducer.engines.EngineOptions;
import org.gradle.api.Action;
import org.ysb33r.grolifant5.api.core.ClosureUtils;
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleSetJavaForkOptions;

/**
 * Toolchains running the {@code Reducer} engine.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface AsciidoctorReducerToolchain extends AsciidoctorToolchain {
    /**
     * Overrides the default version of the {@code Reducer} engine.
     *
     * @param ver Lazy-evaluated version
     */
    void useReducer(Object ver);

    /**
     * Overrides the default version of JRuby.
     *
     * @param v JRuby version
     */
    void useJRuby(Object v);

    /**
     * Configures additional engine options.
     *
     * @param configurator Configurator which is passed an instance of {@link EngineOptions}
     */
    default void engineOptions(Action<EngineOptions> configurator) {
        configurator.execute(getEngineOptions());
    }

    /**
     * Configures additional engine options.
     *
     * @param configurator Configurator which is passed an instance of {@link EngineOptions}
     */
    default void engineOptions(@DelegatesTo(EngineOptions.class) Closure<?> configurator) {
        ClosureUtils.configureItem(getEngineOptions(), configurator);
    }

    /**
     * Direct access to engine options.
     *
     * @return Instance of something that implements {@link EngineOptions}
     */
    EngineOptions getEngineOptions();

    /**
     * Configures fork options for the conversion process.
     *
     * @param configurator Configurating {@link Action}
     */
    default void forkOptions(Action<GrolifantSimpleSetJavaForkOptions> configurator) {
        configurator.execute(getForkOptions());
    }

    /**
     * Configures fork options for the conversion process.
     *
     * @param configurator Configurating Closure
     */
    default void forkOptions(@DelegatesTo(GrolifantSimpleSetJavaForkOptions.class) Closure<?> configurator) {
        ClosureUtils.configureItem(getForkOptions(), configurator);
    }

    /**
     * Access to the fork options
     * @return An instance that implements {@link GrolifantSimpleSetJavaForkOptions}
     */
    GrolifantSimpleSetJavaForkOptions getForkOptions();


    /**
     * Fork after processing N documents.
     * If not set, or at a value of 0 or less, no forking will happen.
     * This is primarily intended for processing many documents where out-od-memory issues might occur.
     *
     * @param forkEvery NUmber of documents to process before forking.
     */
    void setForkEvery(int forkEvery);
}
