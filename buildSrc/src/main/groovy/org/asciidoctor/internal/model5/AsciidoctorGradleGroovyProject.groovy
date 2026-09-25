package org.asciidoctor.internal.model5

import groovy.transform.CompileStatic
import org.asciidoctor.internal.common.CommonBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class AsciidoctorGradleGroovyProject implements Plugin<Project> {

    void apply(Project project) {
        project.pluginManager.tap {
            apply(CommonBasePlugin)
        }
    }
}