package org.asciidoctor.gradle.model5.core.testfixtures.examples

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorCoreExtension
import org.asciidoctor.gradle.model5.core.AsciidoctorCorePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class ExamplePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(AsciidoctorCorePlugin)

        final asciidoc = project.extensions.getByType(AsciidoctorCoreExtension)
        final objectFactory = project.objects
        asciidoc.toolchains.registerFactory(ExampleToolchain) { String name ->
            objectFactory.newInstance(ExampleToolchain, name)
        }

        asciidoc.toolchains.create('myexample', ExampleToolchain)
    }
}
