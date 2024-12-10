package org.asciidoctor.gradle.model5.jvm.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorCoreExtension
import org.asciidoctor.gradle.model5.core.AsciidoctorCorePlugin
import org.asciidoctor.gradle.model5.jvm.core.internal.AsciidoctorJToolchainFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class AsciidoctorJBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(AsciidoctorCorePlugin)
        }

        final asciidoc = project.extensions.getByType(AsciidoctorCoreExtension)

        asciidoc.toolchains.registerFactory(
                AsciidoctorJToolchain,
                project.objects.newInstance(AsciidoctorJToolchainFactory)
        )
    }
}
