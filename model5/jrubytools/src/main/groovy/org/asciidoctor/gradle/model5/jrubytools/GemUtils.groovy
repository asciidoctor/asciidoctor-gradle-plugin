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
package org.asciidoctor.gradle.model5.jrubytools

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.jrubytools.tasks.AsciidoctorGemPrepareTask
import org.asciidoctor.gradle.model5.reducer.ReducerModel
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar

/**
 * Utilities for dealing with GEMs.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class GemUtils {
    /**
     * The toolchain preparation task name
     * @param toolchainName Name of the toolchains
     * @return Task name
     */
    static String toolchainPrepareTaskName(String toolchainName) {
        "${PublicationUtils.TASK_PREFIX}Prepare${toolchainName.capitalize()}Toolchain"
    }

    /**
     * Name of a declarable configuration for use with a specific AsciidoctorJ toolchain.
     *
     * @param tcName Name of toolchain.
     * @return Configuration name
     */
    static String nameForJarPrepareTask(String tcName) {
        "${PublicationUtils.TASK_PREFIX}GemJar${tcName.capitalize()}Prepare"
    }

    /**
     * Registers a GEM prepare task.
     *
     * @param tcName Toolchain name.
     * @param jrubyRuntimeName Name of the JRuby Runtime configuration
     * @param gemsRuntimeName Name of the GEMs Runtime configuration
     * @param project Associated project.
     * @return Provider to the task.
     */
    static TaskProvider<AsciidoctorGemPrepareTask> registerGemPrepareTask(
        String tcName,
        String jrubyRuntimeName,
        String gemsRuntimeName,
        Project project
    ) {
//        final jrubyRuntimeName = ReducerModel.nameForEngineConfiguration(tcName)
//        final gemsRuntimeName = ReducerModel.nameForGemsConfiguration(tcName)
        final gemPrepare = project.tasks.register(
            toolchainPrepareTaskName(tcName),
            AsciidoctorGemPrepareTask,
            jrubyRuntimeName
        )

        gemPrepare.configure { t ->
            t.group = PublicationUtils.GROUP_NAME
            t.description = "Prepare GEMs for the ${tcName} toolchain"
            t.gemConfiguration = t.project.configurations.getByName(gemsRuntimeName)
            t.outputDir = project.layout.buildDirectory.dir("asciidoc-gems/${tcName}")
        }

        gemPrepare
    }

    /**
     * Registers a Gem Jar Task.
     *
     * @param tcName Toolchain name.
     * @param gemPrepare Gem Prepare task.
     * @param project Associated project.
     * @return Provider to the task.
     */
    static TaskProvider<Jar> registerGemJarTask(
        String tcName,
        TaskProvider<AsciidoctorGemPrepareTask> gemPrepare,
        Project project
    ) {
        final gemDir = gemPrepare.flatMap { it.outputDirectory }
        final jarTaskName = nameForJarPrepareTask(tcName)
        final jarTask = project.tasks.register(jarTaskName, Jar) { jar ->
            jar.tap {
                group = PublicationUtils.GROUP_NAME
                description = "Bundles GEMs into a JAR suitable for the ${tcName} toolchain"
                from(gemDir)
                include 'gems/**', 'specifications/**'
                archiveFileName.set("${tcName}.jar".toString())
                destinationDirectory.set(project.layout.buildDirectory.dir('.asciidoctorGemJars'))
                dependsOn(gemPrepare)
            }
        }
        jarTask
    }
}
