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
package org.asciidoctor.gradle.model5.jvm.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.engines.AsciidoctorEngine
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.internal.PluginUtils
import org.asciidoctor.gradle.model5.jvm.internal.engines.DefaultEngineOptions
import org.asciidoctor.gradle.model5.jvm.internal.engines.DefaultLauncher
import org.asciidoctor.gradle.model5.jvm.internal.utils.DependencyUpdater
import org.asciidoctor.gradle.model5.jvm.toolchains.ClasspathManagement
import org.asciidoctor.gradle.model5.jvm.toolchains.CoreVersions
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.ProjectOperations

import javax.inject.Inject

/**
 * Runs the Asciidoctor engine.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjEngine implements AsciidoctorEngine, CoreVersions, ClasspathManagement, EngineOptions {

    final String name

    private final ConfigCacheSafeOperations ccso
    private final ObjectFactory objectFactory
    private final ConfigurableFileCollection classpath
    private final Property<String> asciidoctorjVersion
    private final Provider<String> asciidoctorjProvider
    private final Property<String> jrubyVersion
    private final String configurationName
    private final Property<DefaultLauncher> jvmLauncher

    @Delegate(includes = ['setEruby', 'setCatalogAssets', 'setSourceMap'])
    private final DefaultEngineOptions engineOptions

    @Inject
    AsciidoctorjEngine(String name, Project tempProjectReference) {
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.objectFactory = tempProjectReference.objects

        this.name = name
        this.classpath = ccso.fsOperations().emptyFileCollection()
        this.asciidoctorjVersion = ccso.providerTools().property(String).convention(
            PluginUtils.loadDefaultVersion('asciidoctorj', tempProjectReference, this.class.classLoader)
        )
        this.asciidoctorjProvider = asciidoctorjVersion.map {
            "${JvmModel.ASCIIDOCTORJ_CORE_DEPENDENCY}:${it}".toString()
        }
        this.jrubyVersion = ccso.providerTools().property(String)

        this.configurationName = JvmModel.nameForEngineConfiguration(name)
        final runtime = JvmModel.nameForEngineConfigurationResolvable(name)
        ProjectOperations.find(tempProjectReference).configurations.createLocalRoleFocusedConfiguration(
            configurationName,
            runtime,
            true
        )

        final runtimeClasspath = tempProjectReference.configurations.getByName(runtime)
        tempProjectReference.dependencies.addProvider(configurationName, this.asciidoctorjProvider)
        this.classpath.from(runtimeClasspath)
        setupJrubyRule(tempProjectReference, runtimeClasspath)

        this.engineOptions = objectFactory.newInstance(DefaultEngineOptions)

        this.jvmLauncher = objectFactory.property(DefaultLauncher)
            .value(createLauncher(tempProjectReference))

        this.jvmLauncher.finalizeValue()
    }

    /** Set a new version to use.
     *
     * @param v New version to be used. Can be of anything that can be resolved by
     * {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    void useAsciidoctorj(Object ver) {
        ccso.stringTools().updateStringProperty(this.asciidoctorjVersion, ver)
    }

    /**
     * Set a version of JRuby to use.
     *
     * @param v JRuby version
     */
    void useJRuby(Object ver) {
        ccso.stringTools().updateStringProperty(this.jrubyVersion, ver)
    }

    /**
     * Add additional elements to the classpath.
     *
     * @param fc Files to add to classpath.
     */
    @Override
    void classpath(FileCollection fc) {
        this.classpath.from(fc)
    }

    /**
     * The toolchain's configuration should extend from the given configuration as well.
     *
     * @param srcCfgName Name of configuration.
     */
    @Override
    void classpathExtendsFrom(String srcCfgName) {
        objectFactory.newInstance(DependencyUpdater).extendsFrom(configurationName, srcCfgName)
    }

    /**
     * Something that can execute Asciidoctor conversions.
     *
     * @return Provider to a runnable Asciidoctor engine.
     */
    @Override
    Provider<? extends AsciidoctorLauncher> getLauncher() {
        this.jvmLauncher
    }

    @Override
    Provider<String> getJRubyVersion() {
        this.jrubyVersion
    }

    void registerExecutionContext(
        String toolchainName,
        String formatterName,
        Provider<ExecutionContext> executionContext
    ) {
        this.jvmLauncher.get().registerExecutionContext(toolchainName, formatterName, executionContext)
    }

    private void setupJrubyRule(Project tempProjectReference, Configuration runtimeClasspath) {
        tempProjectReference.afterEvaluate {
            if (jrubyVersion.present) {
                runtimeClasspath.resolutionStrategy.eachDependency { drd ->
                    final mvs = drd.requested
                    if (mvs.group == 'org.jruby' && mvs.name.startsWith('jruby')) {
                        drd.useVersion(jrubyVersion.get())
                    }
                }
            }
        }
    }

    private DefaultLauncher createLauncher(Project tempProjectReference) {
        final defaultLauncher = tempProjectReference.objects.newInstance(DefaultLauncher)
        defaultLauncher.classpath(this.classpath)
        defaultLauncher.engineOptions = engineOptions.engineOptionsProvider
        defaultLauncher
    }
}
