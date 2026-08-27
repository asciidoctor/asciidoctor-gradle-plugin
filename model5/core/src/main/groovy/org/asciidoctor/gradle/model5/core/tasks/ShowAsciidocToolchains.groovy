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
package org.asciidoctor.gradle.model5.core.tasks

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.toolchains.ToolchainInfo
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

import javax.inject.Inject

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

    private final Provider<ToolchainInfo> toolchains

    @Inject
    ShowAsciidocToolchains(Provider<ToolchainInfo> tc) {
        this.toolchains = tc
    }

    @TaskAction
    void exec() {
        final allToolchains = toolchains.get().toolchains

        allToolchains.each { k, tc ->
            println ''
            println " + ${k}:"
            println " |  Type: ${tc.type}"

            if (!tc.formatters.isEmpty()) {
                println ' |  Formatters:'
                tc.formatters.each { fName, fmt ->
                    println " |   | ${fName} (${fmt.type})"
                    println " |   +---- Backend: ${fmt.backend}"
                }
            }
            if (!tc.asciidocExtensions.isEmpty()) {
                println ' |  Extensions:'
                tc.asciidocExtensions.each { eName, ext ->
                    println " |   + ${eName} (${ext.type})"
                }
            }
            println ' |'
        }
        if (allToolchains.size()) {
            println ' \\--------------------'
        }
    }
}
