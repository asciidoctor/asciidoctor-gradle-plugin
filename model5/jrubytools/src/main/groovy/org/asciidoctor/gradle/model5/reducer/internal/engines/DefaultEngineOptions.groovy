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
import org.asciidoctor.gradle.model5.reducer.engines.EngineOptions
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.gradle.jruby.api.core.JRubyEnvironmentInheritance

import javax.inject.Inject

import static org.ysb33r.gradle.jruby.api.core.JRubyEnvironmentInheritance.ESSENTIAL_ONLY

/**
 * Options for running the {@code asciidoctor-reducer} engine.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultEngineOptions implements EngineOptions {

    @Inject
    DefaultEngineOptions(ObjectFactory objectFactory) {
        this.traceFlag = objectFactory.property(Boolean).convention(false)
        this.systemPath = objectFactory.property(Boolean).convention(false)
        this.openModules = objectFactory.property(Boolean).convention(false)
        this.inheritEnv = objectFactory.property(JRubyEnvironmentInheritance).convention(ESSENTIAL_ONLY)
    }

    @Override
    void setTrace(boolean flag) {
        this.traceFlag.set(flag)
    }

    Provider<Boolean> getTrace() {
        this.traceFlag
    }

    @Override
    void setInheritRubyEnv(String mode) {
        this.inheritEnv.set(JRubyEnvironmentInheritance.valueOf(mode.toUpperCase(Locale.US)))
    }

    @Override
    void setInheritRubyEnv(JRubyEnvironmentInheritance mode) {
        this.inheritEnv.set(mode)
    }

    Provider<JRubyEnvironmentInheritance> getInheritRubyEnv() {
        this.inheritEnv
    }

    @Override
    void withSystemPath() {
        this.systemPath.set(true)
    }

    @Override
    void withModuleOpens() {
        this.openModules.set(true)
    }

    Provider<Boolean> getSystemPath() {
        this.systemPath
    }

    Provider<Boolean> getOpenModules() {
        this.openModules
    }

    private final Property<Boolean> traceFlag
    private final Property<Boolean> systemPath
    private final Property<Boolean> openModules
    private final Property<JRubyEnvironmentInheritance> inheritEnv
}
