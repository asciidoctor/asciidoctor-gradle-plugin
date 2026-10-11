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
package org.asciidoctor.gradle.model5.jrubytools.engines

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jrubytools.GemUtils
import org.asciidoctor.gradle.model5.reducer.engines.EngineOptions
import org.asciidoctor.gradle.model5.reducer.internal.engines.DefaultEngineOptions
import org.asciidoctor.gradle.model5.reducer.internal.utils.PluginUtils
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.LegacyLevel
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleJavaForkOptions

/**
 * A base engine for building JRUBY-based tools on top of.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class BaseJRubyEngine implements Named {

    public static final String JRUBY_PROP = 'jruby'

    final String name

    protected final DefaultEngineOptions configuredEngineOptions
    protected final ConfigurableFileCollection classpath
    protected final Property<Integer> forkAfterDocCount
    protected final ConfigCacheSafeOperations ccso
    protected final String jrubyConfigurationName
    protected final String gemsConfigurationName

//    private final Property<DefaultLauncher> jvmLauncher
    private final Property<String> jrubyVersion
    private final GrolifantSimpleJavaForkOptions forkOptions

    protected BaseJRubyEngine(
        String name,
        String jrubyfgName,
        String gemsCfgName,
        Project tempProjectReference
    ) {
        this.name = name
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
//        this.objectFactory = tempProjectReference.objects
        this.configuredEngineOptions = ccso.providerTools().newInstance(DefaultEngineOptions)
        this.classpath = ccso.fsOperations().emptyFileCollection()
        this.forkOptions = ccso.jvmTools().simpleJavaForkOptions()
        this.forkAfterDocCount = tempProjectReference.objects.property(Integer).convention(0)
        this.jrubyConfigurationName = jrubyfgName
        this.gemsConfigurationName = gemsCfgName
        createResolvableConfigurations(tempProjectReference, this.jrubyConfigurationName, this.gemsConfigurationName)

//        this.reducerVersion = ccso.providerTools().property(String).convention(
//            PluginUtils.loadDefaultVersion(REDUCER_PROP, tempProjectReference, this.class.classLoader)
//        )
        this.jrubyVersion = ccso.providerTools().property(String).convention(
            PluginUtils.loadDefaultVersion(JRUBY_PROP, tempProjectReference, this.class.classLoader)
        )

        final runtimeClasspath = tempProjectReference.configurations.getByName(this.jrubyConfigurationName)

        tempProjectReference.dependencies.addProvider(jrubyfgName, jrubyVersion.map {
            if (it =~ /^\d\./) {
                "org.jruby:jruby-complete:${it}"
            } else {
                "org.jruby:jruby:${it}"
            }
        })

//        tempProjectReference.dependencies.addProvider(gemsConfigurationName, reducerVersion.map {
//            "rubygems:${REDUCER_PACKAGE}:${it}"
//        })

        this.classpath.from(runtimeClasspath)
        this.classpath.from(createToolchainPrepareTask(tempProjectReference))
//
//        this.jvmLauncher = objectFactory.property(DefaultLauncher)
//            .value(createLauncher(tempProjectReference))
//
//        this.jvmLauncher.finalizeValue()
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
     * Access to configure fork options.
     *
     * @return AN instance that implements {@link GrolifantSimpleJavaForkOptions}
     */
    GrolifantSimpleJavaForkOptions getForkOptions() {
        this.forkOptions
    }

    /**
     * Access to configure options for running {@code asciidoctor-reducer}.
     *
     * @return An implementation of {@link org.asciidoctor.gradle.model5.reducer.engines.EngineOptions}.
     */
    EngineOptions getEngineOptions() {
        this.configuredEngineOptions
    }

    /**
     * Number of documents to process before forking a new worker.
     *
     * @param count Count. If less than one, all documents will be processed in a single worker.
     */
    void setForkEvery(int count) {
        this.forkAfterDocCount.set(count)
    }

    private Provider<Directory> createToolchainPrepareTask(Project project) {
        final task = GemUtils.registerGemPrepareTask(name, project)
        task.flatMap { it.outputDirectory }
    }

//    private DefaultLauncher createLauncher(Project tempProjectReference) {
//        final cp = this.classpath
//        tempProjectReference.objects.newInstance(
//            DefaultLauncher,
//            this.forkOptions,
//            this.configuredEngineOptions
//        ).tap {
//            classpath(cp)
//            forkEvery = forkAfterDocCount
//        }
//    }

    private void createResolvableConfigurations(Project project, String... names) {
        names.each {
            if (LegacyLevel.PRE_8_4) {
                createResolvableConfigurationPre84(project, it)
            } else {
                createResolvableConfigurationPost83(project, it)
            }
        }
    }

    @CompileDynamic
    private void createResolvableConfigurationPre84(Project project, String name) {
        project.configurations.create(name).tap {
            canBeResolved = true
            if (!LegacyLevel.PRE_8_2) {
                canBeDeclared = true
            }
            canBeConsumed = false
        }
    }

    private void createResolvableConfigurationPost83(Project project, String name) {
        project.configurations.resolvable(name)
    }
}
