package org.asciidoctor.internal.common

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin

@CompileStatic
class RootPlugin implements Plugin<Project> {
    public static final String VALIDATE_ANTORA_TASK = 'validateAntoraYml'
    public static final String FIX_ANTORA_TASK = 'updateAntoraYml'

    @Override
    void apply(Project project) {
        if(project == project.rootProject) {
            project.pluginManager.tap {
                apply(GrolifantServicePlugin)
                apply(LifecycleBasePlugin)
            }
            configureValidateAntora(project)
        }
    }

    private void configureValidateAntora(Project project) {
        project.tasks.register(VALIDATE_ANTORA_TASK, ValidateAntora) {
            it.group = 'Verification'
            it.description = "Checks that the Antora file has the correct information"
        }

        project.tasks.named('check') {
            it.dependsOn(VALIDATE_ANTORA_TASK)
        }

        project.tasks.register(FIX_ANTORA_TASK, FixAntora) {
            it.group = 'Other'
            it.description = "Updates the Antora file with the correct information"
        }
    }

}
