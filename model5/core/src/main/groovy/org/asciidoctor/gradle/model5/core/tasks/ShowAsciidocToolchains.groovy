package org.asciidoctor.gradle.model5.core.tasks

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorCoreExtension
import org.asciidoctor.gradle.model5.core.ToolchainInformation
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
        this.toolchains = project.extensions.getByType(AsciidoctorCoreExtension).registeredToolchains
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
