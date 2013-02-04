package org.asciidoctor.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class AsciidoctorPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.task("asciidoctor", type: AsciidoctorTask)
    }
}
