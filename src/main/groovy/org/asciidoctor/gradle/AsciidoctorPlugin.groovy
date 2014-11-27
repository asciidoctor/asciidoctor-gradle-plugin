/*
 * Copyright 2013-2014 the original author or authors.
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * @author Noam Tenne
 * @author Andres Almiray
 */
class AsciidoctorPlugin implements Plugin<Project> {
    static final String ASCIIDOCTOR = 'asciidoctor'
    static final String ASCIIDOCTORJ = 'asciidoctorj'

    void apply(Project project) {
        project.apply(plugin: 'base')

        AsciidoctorExtension extension = project.extensions.create(ASCIIDOCTORJ, AsciidoctorExtension, project)

        project.repositories {
            jcenter()
        }

        Configuration configuration = project.configurations.maybeCreate(ASCIIDOCTOR)
        project.afterEvaluate {
            project.logger.info("[Asciidoctor] asciidoctorj: ${extension.version}")
            project.logger.info("[Asciidoctor] asciidoctorj-groovy-dsl: ${extension.groovyDslVersion}")
            project.dependencies {
                asciidoctor("org.asciidoctor:asciidoctorj:${extension.version}")
                asciidoctor("org.asciidoctor:asciidoctorj-groovy-dsl:${extension.groovyDslVersion}")
            }
        }

        project.task(ASCIIDOCTOR,
            type: AsciidoctorTask,
            group: 'Documentation',
            description: 'Converts AsciiDoc files and copies the output files and related resources to the build directory.') {
            classpath = configuration
        }
    }
}
