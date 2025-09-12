package org.asciidoctor.internal.classic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.ysb33r.grolifant5.api.core.ProjectOperations

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

        ProjectOperations.find(project).tasks.whenNamed('integrationTest', Test) {
            it.maxParallelForks = 1
            it.forkEvery = 2
            it.maxHeapSize = '2g'
        }
    }
}
