package org.asciidoctor.internal.model5

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Specifically for plugin projects that rely on AsciidoctorJ as an engine
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
class AsciidoctorJGradlePluginProject implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(AsciidoctorGradlePluginProject)
        configureJava(project)
    }

    void configureJava(Project project) {
        final java = project.extensions.getByType(JavaPluginExtension)
        final ver = project.providers.gradleProperty('jdkVersionAsciidoctorj').orElse('11').get()
        java.toolchain {
            it.languageVersion.set(JavaLanguageVersion.of(ver))
        }
    }
}
