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
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.core.DocType
import org.asciidoctor.gradle.model5.core.errors.IncorrectOutputFormatException
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.engines.ExecutionContext
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjGenericOutputFormatter
import org.asciidoctor.gradle.model5.jvm.internal.engines.DefaultExecutionContext
import org.asciidoctor.gradle.model5.jvm.internal.gems.GemUtils
import org.asciidoctor.gradle.model5.jvm.internal.utils.DependencyUpdater
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.ProjectOperations
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleSetJavaForkOptions

import javax.inject.Inject
import java.util.concurrent.Callable

import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjGemsPlugin.PLUGIN_ID

/**
 * Allow any backend for {@code asciidoctorj} to be used..
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjGenericOutputFormatter implements AsciidoctorjGenericOutputFormatter {

    final String name
    private final ConfigCacheSafeOperations ccso
    private final ObjectFactory objectFactory
    private final Property<ExecutionContext> executionContext
    private final Property<AsciidoctorNamedBackend> backend
    private final MapProperty<String, Object> attributes
    private final SetProperty<String> requires
    private final ConfigurableFileCollection classpath
    private final String jarConfigurationName
    private final Callable<Boolean> hasPlugin
    private final AsciidoctorjToolchain toolchain
    private Boolean copyResources
    private Optional<DocType> docType

    @Inject
    DefaultAsciidoctorjGenericOutputFormatter(String name, AsciidoctorjToolchain tc, Project project) {
        this.name = name
        this.toolchain = tc
        this.objectFactory = project.objects
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.executionContext = ccso.providerTools().property(ExecutionContext)
        this.attributes = objectFactory.mapProperty(String, Object)
        this.requires = objectFactory.setProperty(String)
        this.copyResources = false
        this.docType = Optional.empty()
        this.backend = objectFactory.property(AsciidoctorNamedBackend).convention(project.provider { ->
            throw new IncorrectOutputFormatException("A backend must be defined for ${tc.name}:${name}")
        })
        this.classpath = ccso.fsOperations().emptyFileCollection()
        this.hasPlugin = { -> project.pluginManager.hasPlugin(PLUGIN_ID) }

        this.jarConfigurationName = JvmModel.nameForOutputFormatterConfiguration(tc.name, name)
        final runtime = JvmModel.nameForOutputFormatterConfigurationResolvable(tc.name, name)

        ProjectOperations.find(project).configurations
            .createLocalRoleFocusedConfiguration(jarConfigurationName, runtime, true)
        this.classpath.from(project.configurations.getByName(runtime))
    }

    @Override
    Provider<Set<String>> getRequires() {
        this.requires
    }

    @Override
    void requires(String... reqs) {
        this.requires.addAll(reqs)
    }

    @Override
    void setBackend(String backendName) {
        this.backend.set(AsciidoctorNamedBackend.of(name, backendName))
    }

    @Override
    Provider<AsciidoctorNamedBackend> getBackend() {
        this.backend
    }

    @Override
    void useClassloaderIsolation() {
        this.executionContext.set((ExecutionContext) null)
    }

    @Override
    void useProcessIsolation(Action<GrolifantSimpleSetJavaForkOptions> forkOptions) {
        final ec = objectFactory.newInstance(DefaultExecutionContext)
        forkOptions.execute(ec)
        this.executionContext.set(ec)
    }

    @Override
    void useProcessIsolation(@DelegatesTo(GrolifantSimpleSetJavaForkOptions) Closure<?> forkOptions) {
        final ec = objectFactory.newInstance(DefaultExecutionContext)
        ClosureUtils.configureItem(ec, forkOptions)
        this.executionContext.set(ec)
    }

    @Override
    Provider<Map<String, Object>> getAttributeProvider() {
        this.attributes
    }

    @Override
    void attributes(Map<String, ?> attrs) {
        this.attributes.putAll(attrs)
    }

    @Override
    void setCopyResources(boolean flag) {
        this.copyResources = flag
    }

    @Override
    boolean getCopyResources() {
        this.copyResources
    }

    @Override
    String getDisplayType() {
        AsciidoctorjGenericOutputFormatter.canonicalName
    }

    @Override
    void useGem(String gemName, Object version) {
        checkForGemsPlugin()
        objectFactory.newInstance(DependencyUpdater).add(
            GemUtils.nameForToolchainConfiguration(toolchain.name),
            "${GemUtils.GEM_GROUP}:${gemName}",
            ccso.stringTools().provideString(version)
        )
    }

    @Override
    void useModule(String moduleName, Object version) {
        objectFactory.newInstance(DependencyUpdater).add(
            jarConfigurationName,
            moduleName,
            ccso.stringTools().provideString(version)
        )
    }

    @Override
    void useModule(ProjectDependency module) {
        objectFactory.newInstance(DependencyUpdater).add(jarConfigurationName, module)
    }

    @Override
    void setEnforcedDocType(String doctype) {
        this.docType = Optional.of(DocType.valueOf(doctype.toUpperCase(Locale.US)))
    }

    @Override
    FileCollection getClasspath() {
        this.classpath
    }

    private void checkForGemsPlugin() {
        if (hasPlugin.call()) {
            GemUtils.registerToolchainSupport(toolchain, objectFactory)
        } else {
            throw new GradleException("Templates cannot be configured if '${PLUGIN_ID}' has not been applied")
        }
    }

}
