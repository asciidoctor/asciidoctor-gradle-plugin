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
import org.asciidoctor.gradle.model5.jvm.internal.engines.DefaultLauncher
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

import static org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin.INTERMEDIATE_RESOURCE_PATH

/**
 * Runs the Asciidoctor engine
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjEngine implements AsciidoctorEngine, CoreVersions {

    final String name
    final Provider<AsciidoctorLauncher> launcher

    private final ConfigCacheSafeOperations ccso
    private final ObjectFactory objectFactory
    private final ConfigurableFileCollection classpath
    private final Property<String> asciidoctorjVersion
    private final Provider<String> asciidoctorjProvider
    private final Property<String> jrubyVersion

    private static final String ASCIIDOCTORJ_GROUP = 'org.asciidoctor'
    private static final String ASCIIDOCTORJ_CORE_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj"
    private static final String ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-groovy-dsl"
    private static final String ASCIIDOCTORJ_PDF_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-pdf"
    private static final String ASCIIDOCTORJ_EPUB_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-epub3"
    private static final String ASCIIDOCTORJ_DIAGRAM_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctorj-diagram"
    private static final String ASCIIDOCTORJ_LEANPUB_DEPENDENCY = "${ASCIIDOCTORJ_GROUP}:asciidoctor-leanpub-markdown"

    @Inject
    AsciidoctorjEngine(String name, Project tempProjectReference) {
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.objectFactory = tempProjectReference.objects
        final props = ccso.fsOperations().loadPropertiesFromResource(
                "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-jvm-core-plugin.properties",
                this.class.classLoader
        )

        this.name = name
        this.classpath = ccso.fsOperations().emptyFileCollection()
        this.asciidoctorjVersion = ccso.providerTools().property(String).convention(props['asciidoctorj'].toString())
        this.asciidoctorjProvider = asciidoctorjVersion.map { "${ASCIIDOCTORJ_CORE_DEPENDENCY}:${it}".toString() }
        this.jrubyVersion = ccso.providerTools().property(String)

        final cfgName = JvmModel.nameForEngineConfiguration(name)
        final runtime = JvmModel.nameForEngineConfigurationResolvable(name)
        ProjectOperations.find(tempProjectReference).configurations.createLocalRoleFocusedConfiguration(
                cfgName,
                runtime,
                true
        )

        final runtimeClasspath = tempProjectReference.configurations.getByName(runtime)
        tempProjectReference.dependencies.addProvider(cfgName, this.asciidoctorjProvider)
        this.classpath.from(runtimeClasspath)

        setupJrubyRule(tempProjectReference, runtimeClasspath)
        this.launcher = createLauncher(tempProjectReference)
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
    void classpath(FileCollection fc ) {
        this.classpath.from(fc)
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

    private Provider<? extends AsciidoctorLauncher> createLauncher(Project tempProjectReference) {
        final jvmLauncher = tempProjectReference.objects.newInstance(DefaultLauncher)
        jvmLauncher.classpath(this.classpath)

        tempProjectReference.provider { -> jvmLauncher }
    }
}
