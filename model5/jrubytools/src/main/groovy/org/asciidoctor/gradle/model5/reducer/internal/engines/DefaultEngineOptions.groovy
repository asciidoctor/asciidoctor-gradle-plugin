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
package org.asciidoctor.gradle.model5.reducer.internal.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jrubytools.internal.engines.DefaultJRubyEngineOptions
import org.asciidoctor.gradle.model5.reducer.engines.EngineOptions
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 * Options for running the {@code asciidoctor-reducer} engine.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultEngineOptions extends DefaultJRubyEngineOptions implements EngineOptions {

    @Inject
    DefaultEngineOptions(ObjectFactory objectFactory) {
        super(objectFactory)
        this.traceFlag = objectFactory.property(Boolean).convention(false)
    }

    @Override
    void setTrace(boolean flag) {
        this.traceFlag.set(flag)
    }

    Provider<Boolean> getTrace() {
        this.traceFlag
    }

    private final Property<Boolean> traceFlag
}
