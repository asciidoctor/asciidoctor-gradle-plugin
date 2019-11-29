/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.compat

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.AsciidoctorTask
import org.asciidoctor.gradle.base.AsciidoctorBasePlugin
import org.asciidoctor.gradle.base.internal.DeprecatedFeatures
import org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler

import static org.asciidoctor.gradle.base.internal.DeprecatedFeatures.addDeprecationMessage
import static org.asciidoctor.gradle.jvm.AsciidoctorJExtension.JRUBY_COMPLETE_DEPENDENCY

/**
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Patrick Reimers
 * @author Markus Schlichting
 * @author Schalk W. Cronjé
 */
@Deprecated
@CompileStatic
@SuppressWarnings('LineLength')
class AsciidoctorCompatibilityPlugin implements Plugin<Project> {
    static final String ASCIIDOCTOR = 'asciidoctor'
    static final String ASCIIDOCTORJ = 'asciidoctorj'
    static final String ASCIIDOCTORJ_CORE_DEPENDENCY = 'org.asciidoctor:asciidoctorj:'
    static final String ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY = 'org.asciidoctor:asciidoctorj-groovy-dsl:'

    void apply(Project project) {
        ['jvm.convert', 'js.base'].each { String s ->
            String pluginName = "org.asciidoctor.${s}"
            if (project.gradle.plugins.hasPlugin(pluginName)) {
                throw new GradleException("'${pluginName}' and 'org.asciidoctor.convert' cannot be used within the same (sub)project")
            }
        }

        project.apply plugin: DeprecatedFeatures
        project.apply plugin: AsciidoctorBasePlugin

        AsciidoctorExtension extension = project.extensions.create(ASCIIDOCTORJ, AsciidoctorExtension, project)

        addDefaultRepositories(project)
        Configuration configuration = project.configurations.maybeCreate(ASCIIDOCTOR)
        project.logger.info("[Asciidoctor] asciidoctorj: ${extension.version}")
        project.logger.info("[Asciidoctor] asciidoctorj-groovy-dsl: ${extension.groovyDslVersion}")

        configuration.incoming.beforeResolve(new Action<ResolvableDependencies>() {
            @SuppressWarnings('UnusedMethodParameter')
            void execute(ResolvableDependencies resolvableDependencies) {
                DependencyHandler dependencyHandler = project.dependencies
                DependencySet dependencies = configuration.dependencies
                dependencies.add(dependencyHandler.create(ASCIIDOCTORJ_CORE_DEPENDENCY + extension.version))
                dependencies.add(
                    dependencyHandler.create(
                        ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY + extension.groovyDslVersion,
                        excludeGroovy()
                    )
                )
            }
        })

        configuration.resolutionStrategy.eachDependency { DependencyResolveDetails dsr ->
            dsr.with {
                if (target.name == 'jruby' && target.group == 'org.jruby') {
                    useTarget "${JRUBY_COMPLETE_DEPENDENCY}:${target.version}"
                }
            }
        }

        AsciidoctorTask asciidoctor = project.tasks.create(ASCIIDOCTOR, AsciidoctorTask)
        asciidoctor.group = AsciidoctorJBasePlugin.TASK_GROUP
        asciidoctor.description = 'Compatibility task to convert AsciiDoc files and copy related resources'
        asciidoctor.classpath = configuration

        final String pluginIdentifier = 'org.asciidoctor.convert'

        addDeprecationMessage(
            project,
            pluginIdentifier,
            "'${pluginIdentifier}' is deprecated. When you have time please switch over to 'org.asciidoctor.jvm.convert'."
        )

        addDeprecationMessage(
            project,
            pluginIdentifier,
            'jcenter() is no longer added by default. If you relied on this behaviour in the past, please add jcenter() to the repositories block.'
        )
    }

    @CompileDynamic
    Closure excludeGroovy() {
        return {
            exclude module: 'groovy-all'
        }
    }

    @CompileDynamic
    private void addDefaultRepositories(Project project) {
        project.afterEvaluate {
            if (!project.extensions.asciidoctorj.noDefaultRepositories) {
                project.repositories {
                    jcenter()
                }
            }
        }
    }
}
