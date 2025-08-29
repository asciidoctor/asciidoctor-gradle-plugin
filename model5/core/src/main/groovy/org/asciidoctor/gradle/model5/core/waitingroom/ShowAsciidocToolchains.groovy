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
package org.asciidoctor.gradle.model5.core.waitingroom

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.toolchains.ToolchainInformation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

/**
 * Displays detected Asciidoctor toolchains
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
@UntrackedTask(because = 'Produces only non-cacheable console output')
class ShowAsciidocToolchains extends GrolifantDefaultTask {

    private final Provider<List<ToolchainInformation>> toolchains

    ShowAsciidocToolchains() {
//        this.toolchains = project.extensions.getByType(AsciidoctorCoreExtension).registeredToolchains
    }

    @TaskAction
    void exec() {
        toolchains.get()
            .sort { lhs, rhs -> lhs.name <=> rhs.name }
            .each {
                println ''
                println " + ${it.name}:"
                println "   Type: ${it.className.replaceFirst(~/_Decorated$/, '')}"

                if (!it.formatters.isEmpty()) {
                    println "   Formatters:"
                    it.formatters.each { fmt ->
                        println "     | ${fmt.key} (${fmt.value.replaceFirst(~/_Decorated$/, '')})"
                    }
                }
                println ''
            }
    }
}
