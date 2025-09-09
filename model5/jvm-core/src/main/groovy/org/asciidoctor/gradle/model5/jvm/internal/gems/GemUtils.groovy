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
package org.asciidoctor.gradle.model5.jvm.internal.gems

import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.tasks.AsciidoctorjGemPrepareTask
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.bundling.Jar
import org.ysb33r.grolifant5.api.core.ProjectOperations

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.jvm.internal.PluginUtils.loadDefaultVersion

/**
 * Internal utilities for dealing with GEMs.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class GemUtils {
    public static final String JRUBY_COMPLETE_NAME = 'jruby-complete'
    public static final String JRUBY_ALL_DEPENDENCY = "org.jruby:${JRUBY_COMPLETE_NAME}"
    public static final String TASK_GROUP = PublicationUtils.GROUP_NAME
    public static final String GEM_GROUP = System.getProperty(
        'org.asciidoctor.gradle.override.gem.group', 'rubygems'
    )
    public static final String GEM_TILT = 'tilt'

    /**
     * Name of a declarable configuration for use with a specific AsciidoctorJ engine.
     *
     * @param engineName Name of engine.
     * @return Configuration name
     */
    static String nameForJrubyConfiguration(String engineName) {
        "asciidocjRuby${engineName.capitalize()}"
    }

    /**
     * Name of a resolvable configuration for use with a specific AsciidoctorJ engine.
     *
     * @param engineName Name of engine.
     * @return Configuration name
     */
    static String nameForJrubyConfigurationResolvable(String engineName) {
        "${nameForJrubyConfiguration(engineName)}RuntimeClasspath"
    }

    /**
     * Name of a declarable configuration for use with a specific AsciidoctorJ toolchain.
     *
     * @param tcName Name of toolchain.
     * @return Configuration name
     */

    static String nameForGemPrepareTask(String tcName) {
        "${JvmModel.TASK_PREFIX}Gems${tcName.capitalize()}Prepare"
    }

    /**
     * Name of a declarable configuration for use with a specific AsciidoctorJ toolchain.
     *
     * @param tcName Name of toolchain.
     * @return Configuration name
     */

    static String nameForJarPrepareTask(String tcName) {
        "${JvmModel.TASK_PREFIX}GemJar${tcName.capitalize()}Prepare"
    }

    /**
     * Name of a declarable configuration for use with a specific AsciidoctorJ toolchain.
     *
     * @param tcName Name of toolchain.
     * @return Configuration name
     */

    static String nameForToolchainConfiguration(String tcName) {
        "asciidocGems${tcName.capitalize()}"
    }

    /**
     * Name of a resolvable configuration for use with a specific AsciidoctorJ toolchain.
     *
     * @param tcName Name of toolchain.
     * @return Configuration name
     */
    static String nameForToolchainConfigurationResolvable(String tcName) {
        "${nameForToolchainConfiguration(tcName)}RuntimeClasspath"
    }

    /**
     * Registers the appropriate configurations and tasks for working with GEMs
     * specific to one toolchain.
     *
     * <p>
     * This method can be called more than one, but will only register anything on the toolchain on the first call.
     * </p>
     *
     * @param tc Toolchain.
     * @param project Associated project.
     */
    @Synchronized
    static void registerToolchainSupport(AsciidoctorjToolchain tc, Project project) {
        final cfgTools = ProjectOperations.find(project).configurations
        final cfgName = nameForToolchainConfiguration(tc.name)

        if (!project.configurations.findByName(cfgName)) {
            final runtimeName = nameForToolchainConfigurationResolvable(tc.name)
            cfgTools.createLocalRoleFocusedConfiguration(cfgName, runtimeName, true)

            final jrubyCfgName = nameForJrubyConfiguration(tc.name)
            final jrubyRuntimeName = nameForJrubyConfigurationResolvable(tc.name)
            cfgTools.createLocalRoleFocusedConfiguration(jrubyCfgName, jrubyRuntimeName, false)

            final jrubyVersion = loadDefaultVersion('jruby', project, tc.class.classLoader)
            project.dependencies.add(
                jrubyCfgName,
                tc.JRubyVersion.orElse(jrubyVersion).map {
                    "${JRUBY_ALL_DEPENDENCY}:${it}"
                }
            )

            final gemPrepare = project.tasks.register(
                nameForGemPrepareTask(tc.name),
                AsciidoctorjGemPrepareTask,
                jrubyRuntimeName
            )

            gemPrepare.configure { t ->
                t.group = TASK_GROUP
                t.description = "Prepare GEMs for the ${tc.name} toolchain"
                t.gemConfiguration = t.project.configurations.getByName(runtimeName)
                t.outputDir = project.layout.buildDirectory.dir("asciidoc-gems/${tc.name}")
            }

            final gemDir = gemPrepare.flatMap { it.outputDir }
            final jarTaskName = nameForJarPrepareTask(tc.name)
            final jarTask = project.tasks.register(jarTaskName, Jar) { jar ->
                jar.tap {
                    group = TASK_GROUP
                    description = "Bundles GEMs into a JAR suitable for the ${tc.name} toolchain"
                    from(gemDir)
                    include 'gems/**', 'specifications/**'
                    archiveFileName.set("${tc.name}.jar".toString())
                    destinationDirectory.set(project.layout.buildDirectory.dir('.asciidoctorGemJars'))
                    dependsOn(gemPrepare)
                }
            }

            tc.classpath(project.files(jarTask))
        }
    }

    /**
     * Registers the appropriate configurations and tasks for working with GEMs
     * specific to one toolchain.
     *
     * <p>
     * This method can be called more than one, but will only register anything on the toolchain on the first call.
     * </p>
     *
     * @param tc Toolchain.
     * @param objectFactory An instance of an {@link ObjectFactory}.
     */
    static void registerToolchainSupport(AsciidoctorjToolchain tc, ObjectFactory objectFactory) {
        registerToolchainSupport(tc, objectFactory.newInstance(ProjectLoader).project)
    }

    static class ProjectLoader {
        final Project project

        @Inject
        ProjectLoader(Project project) {
            this.project = project
        }
    }
}
