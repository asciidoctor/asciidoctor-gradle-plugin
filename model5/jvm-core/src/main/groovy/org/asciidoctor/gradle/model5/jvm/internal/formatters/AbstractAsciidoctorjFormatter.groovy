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
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.jvm.engines.ExecutionContext
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjOutputFormatter
import org.asciidoctor.gradle.model5.jvm.internal.engines.DefaultExecutionContext
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleSetJavaForkOptions

@CompileStatic
abstract class AbstractAsciidoctorjFormatter implements AsciidoctorjOutputFormatter {
    final String name
    final Provider<AsciidoctorNamedBackend> backend

    protected final ConfigCacheSafeOperations ccso
    protected final AsciidoctorjToolchain toolchain
    protected final ObjectFactory objectFactory
    protected final String projectPath
    protected final Property<ExecutionContext> executionContext

    private final Provider<Set<String>> emptyRequires

    /**
     * A list of {@code requires} that a component places on the associated toolchain.
     *
     * @return List of {@code requires}. Can be empty, but never {@code null}.
     */
    @Override
    Provider<Set<String>> getRequires() {
        this.emptyRequires
    }

    /**
     * When running this output formatter, do it in-process, but with classpath isolation.
     *
     * <p>This is the default behaviour.</p>
     */
    @Override
    void useClassloaderIsolation() {
        this.executionContext.set((ExecutionContext)null)
    }

    /**
     * Use process isolation when using this output formatter to perform conversions.
     *
     * @param forkOptions Reduced set of fork options.
     */
    @Override
    void useProcessIsolation(Action<GrolifantSimpleSetJavaForkOptions> forkOptions) {
        final ec = objectFactory.newInstance(DefaultExecutionContext)
        forkOptions.execute(ec)
        this.executionContext.set(ec)
    }

    /**
     * Use process isolation when using this output formatter to perform conversions.
     *
     * @param forkOptions Reduced set of fork options.
     */
    @Override
    void useProcessIsolation(@DelegatesTo(GrolifantSimpleSetJavaForkOptions.class) Closure<?> forkOptions) {
        final ec = objectFactory.newInstance(DefaultExecutionContext)
        ClosureUtils.configureItem(ec, forkOptions)
        this.executionContext.set(ec)
    }

    protected AbstractAsciidoctorjFormatter(String name, String backendName, AsciidoctorjToolchain tc, Project project) {
        this.name = name
        this.toolchain = tc
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.objectFactory = project.objects
        this.projectPath = ccso.projectTools().fullProjectPath
        this.backend = ccso.providerTools().provider { -> AsciidoctorNamedBackend.of(name, backendName) }
        this.emptyRequires = ccso.providerTools().provider { -> Collections.EMPTY_SET }
        this.executionContext = ccso.providerTools().property(ExecutionContext)

        tc.registerExecutionContext(name, executionContext)
    }
}
