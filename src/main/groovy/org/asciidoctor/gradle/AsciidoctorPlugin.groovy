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

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Patrick Reimers
 * @author Markus Schlichting
 * @author Schalk W. Cronjé
 */
class AsciidoctorPlugin implements Plugin<Project> {
    static final String ASCIIDOCTOR = 'asciidoctor'
    static final String ASCIIDOCTORJ = 'asciidoctorj'
    static final String ASCIIDOCTORJ_CORE_DEPENDENCY = 'org.asciidoctor:asciidoctorj:'
    static final String ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY = 'org.asciidoctor:asciidoctorj-groovy-dsl:'

    void apply(Project project) {
        project.apply(plugin: 'base')

        AsciidoctorExtension extension = project.extensions.create(ASCIIDOCTORJ, AsciidoctorExtension, project)

        project.afterEvaluate {
            if(!project.extensions.asciidoctorj.noDefaultRepositories) {
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
    }
}
