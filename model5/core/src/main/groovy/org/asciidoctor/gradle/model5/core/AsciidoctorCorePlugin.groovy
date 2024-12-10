package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.tasks.ShowAsciidocToolchains
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin

@CompileStatic
class AsciidoctorCorePlugin implements Plugin<Project> {
    public final static String INTERMEDIATE_RESOURCE_PATH = 'META-INF/asciidoctor.gradle'
    public final static String TOOLCHAIN_DISPLAY_TASK = 'asciidoctorToolchains'

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(GrolifantServicePlugin)
        }

        project.extensions.create(AsciidoctorCoreExtension.NAME, AsciidoctorCoreExtension, project)

        project.tasks.register(TOOLCHAIN_DISPLAY_TASK, ShowAsciidocToolchains) {
            it.group = 'help'
            it.description = 'Displays registered Asciidoctor toolchains.'
        }
    }
}
