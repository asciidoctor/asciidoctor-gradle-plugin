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
package org.asciidoctor.gradle.model5.jvm.internal.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjGenericExtension
import org.asciidoctor.gradle.model5.jvm.internal.gems.GemUtils
import org.asciidoctor.gradle.model5.jvm.internal.utils.DependencyUpdater
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.ProjectOperations

import javax.inject.Inject
import java.util.concurrent.Callable

import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjGemsPlugin.PLUGIN_ID

/**
 * Implementation of {@link AsciidoctorjGenericExtension}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjGenericExtension implements AsciidoctorjGenericExtension {

    static class Factory extends AbstractFactory implements NamedDomainObjectFactory<AsciidoctorjGenericExtension> {
        @Inject
        Factory(AsciidoctorjToolchain toolchain, Project project) {
            super(toolchain, project)
        }

        @Override
        AsciidoctorjGenericExtension create(String name) {
            objectFactory.newInstance(DefaultAsciidoctorjGenericExtension, name, toolchain)
        }
    }

    final String name

    private final ConfigCacheSafeOperations ccso
    private final ObjectFactory objectFactory
    private final MapProperty<String, Object> attributes
    private final SetProperty<String> requires
    private final ConfigurableFileCollection classpath
    private final Callable<Boolean> hasPlugin
    private final AsciidoctorjToolchain toolchain
    private final String jarConfigurationName

    @Inject
    DefaultAsciidoctorjGenericExtension(String name, AsciidoctorjToolchain tc, Project project) {
        this.name = name
        this.toolchain = tc
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.objectFactory = project.objects

        this.attributes = objectFactory.mapProperty(String, Object)
        this.requires = objectFactory.setProperty(String)
        this.classpath = ccso.fsOperations().emptyFileCollection()
        this.hasPlugin = { -> project.pluginManager.hasPlugin(PLUGIN_ID) }

        this.jarConfigurationName = JvmModel.nameForExtensionConfiguration(tc.name, name)
        final runtime = JvmModel.nameForExtensionConfigurationResolvable(tc.name, name)

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
    Provider<Map<String, Object>> getAttributeProvider() {
        this.attributes
    }

    @Override
    void attributes(Map<String, ?> attrs) {
        this.attributes.putAll(attrs)
    }

    @Override
    String getDisplayType() {
        AsciidoctorjGenericExtension.canonicalName
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

    /**
     * Additional items to add to the classpath when a conversion involving the output formatter is executed.
     *
     * <p>
     *     The classpath is empty by default.
     * </p>
     *
     * @return Classpath. Can be {@code null} to indicate that the formatter does not support additional classpath.
     */
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
