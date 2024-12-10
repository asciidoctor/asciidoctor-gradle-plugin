package org.asciidoctor.gradle.model5.jvm.core.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorPublication
import org.asciidoctor.gradle.model5.core.OutputFormatter
import org.asciidoctor.gradle.model5.core.ToolchainUtils
import org.asciidoctor.gradle.model5.core.internal.PublicationUtils
import org.asciidoctor.gradle.model5.jvm.core.AsciidoctorJToolchain
import org.gradle.api.Project

@CompileStatic
abstract class AbstractAsciidoctorJFormatter implements OutputFormatter {
    final String name
    protected final AsciidoctorJToolchain toolchain
    protected final Project project

    /**
     * Registers the tasks associated with this given output formatter, its toolchain and the corresponding publication.
     *
     * @param publication Publication
     */
    @Override
    void registerTasksIfAbsent(AsciidoctorPublication publication) {
        final taskName = ToolchainUtils.asciidoctorTaskName(toolchain,this,publication)
        // Register the task by that name
        project.tasks.register(taskName/*,AsciidoctorJTask*/) {t ->
            t.group = PublicationUtils.GROUP_NAME
            t.description = "Convert Asdiidoc source to format identified as '${name}'"

            // configure a bunch of stuff from the publication.
        }
    }

    protected AbstractAsciidoctorJFormatter(String name, AsciidoctorJToolchain tc, Project project) {
        this.name = name
        this.toolchain = toolchain
    }
}
