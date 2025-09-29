package org.asciidoctor.internal.model5

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.internal.classic.ModuleVersions
import org.asciidoctor.internal.common.AsciidoctorGradleProjectExtension
import org.asciidoctor.internal.common.CommonBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin

@CompileStatic
class AsciidoctorGradleGroovyProject implements Plugin<Project> {

    void apply(Project project) {
        project.pluginManager.tap {
            apply(CommonBasePlugin)
        }
    }
}