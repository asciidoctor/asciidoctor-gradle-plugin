/*
 * Copyright 2013-2018 the original author or authors.
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
package org.asciidoctor.gradle

import groovy.transform.CompileDynamic
import org.asciidoctor.gradle.compat.AsciidoctorExtension
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.logging.LogLevel
import org.gradle.util.GradleVersion

/**
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Patrick Reimers
 * @author Markus Schlichting
 * @author Schalk W. Cronjé
 */
@Deprecated
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

        project.apply plugin: 'org.asciidoctor.base'

        AsciidoctorExtension extension = project.extensions.create(ASCIIDOCTORJ, AsciidoctorExtension, project)

        project.afterEvaluate {
            if (!project.extensions.asciidoctorj.noDefaultRepositories) {
                project.repositories {
                    jcenter()
                }
            }
        }

        Configuration configuration = project.configurations.maybeCreate(ASCIIDOCTOR)
        project.logger.info("[Asciidoctor] asciidoctorj: ${extension.version}")
        project.logger.info("[Asciidoctor] asciidoctorj-groovy-dsl: ${extension.groovyDslVersion}")

        configuration.incoming.beforeResolve(new Action<ResolvableDependencies>() {
            @SuppressWarnings('UnusedMethodParameter')
            void execute(ResolvableDependencies resolvableDependencies) {
                DependencyHandler dependencyHandler = project.dependencies
                def dependencies = configuration.dependencies
                dependencies.add(dependencyHandler.create(ASCIIDOCTORJ_CORE_DEPENDENCY + extension.version))
                dependencies.add(dependencyHandler.create(ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY + extension.groovyDslVersion))
            }
        })

        project.task(ASCIIDOCTOR,
            type: AsciidoctorTask,
            group: 'Documentation',
            description: 'Converts AsciiDoc files and copies the output files and related resources to the build directory.') {
            classpath = configuration
        }

        addMigrationSupport(
            project,
            "'org.asciidoctor.convert' is deprecated. When you have time please switch over to 'org.asciidoctor.jvm.convert'.",
            'jcenter() is no longer added by default. If you relied on this behaviour in the past, please add jcenter() to the repositories block.'
        )
    }

    @CompileDynamic
    private addMigrationSupport(Project project, String... pluginMessages) {
        project.afterEvaluate {
            Set<String> messages = []
            messages.addAll(pluginMessages)
            project.tasks.withType(AsciidoctorTask) { AsciidoctorTask task ->
                messages.addAll(task.migrationMessages)
            }

            if (messages.empty) {
                return
            }

            if (GradleVersion.current() >= GradleVersion.version('4.5')) {
                switch (project.gradle.startParameter.warningMode.toString().toLowerCase()) {
                    case 'all':
                        project.logger.lifecycle(createMigrationOutputMessage(messages))
                        break
                    case 'none':
                        break
                    default:
                        project.logger.lifecycle 'You are using one or more deprecated Asciidoctor task or plugins. To help with migration run with --warnings=all'
                }
            } else {
                if (project.gradle.startParameter.logLevel != LogLevel.QUIET) {
                    if (project.gradle.startParameter.logLevel == LogLevel.LIFECYCLE) {
                        project.logger.lifecycle 'You are using one or more deprecated Asciidoctor task or plugins. To help with migration run with -i or --info'
                    } else {
                        project.logger.info(createMigrationOutputMessage(messages))
                    }
                }
            }
        }
    }

    @SuppressWarnings('FactoryMethodName')
    static String createMigrationOutputMessage(final Set<String> messages) {
        StringWriter output = new StringWriter()
        output.withCloseable {
            output.println 'You are using one or more deprecated Asciidoctor task or plugins. These will be removed in a future release. To help you migrate we have compiled some tips for you based upon your current usage:'
            output.println()

            messages.each { String msg ->
                output.println "  - ${msg}"
            }
            output.println()
            output.toString()
        }
    }
}
