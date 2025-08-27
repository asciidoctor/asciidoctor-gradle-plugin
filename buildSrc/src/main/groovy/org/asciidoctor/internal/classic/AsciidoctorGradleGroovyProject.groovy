package org.asciidoctor.internal.classic

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
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

    public final static String GENERATOR_NAME = 'generateModuleVersions'

    void apply(Project project) {
        project.pluginManager.tap {
            apply(CommonBasePlugin)
        }

        TaskProvider generateModuleVersions = project.tasks.register(GENERATOR_NAME, ModuleVersions)

        SourceSetContainer sourceSets = project.extensions.getByType(SourceSetContainer)
        SourceSet main = sourceSets.getByName('main')

        project.tasks.named(main.processResourcesTaskName, Copy).configure { copy ->
            copy.from(generateModuleVersions.get().outputs.files) { CopySpec cs ->
                cs.into "${ModuleVersions.INTERMEDIATE_FOLDER_PATH}"
            }
        }

        addDefaultVersions(project)
        configureIdea(project)
//        configureRepositories(project)
//        configureJava(project)
    }

//    void configureRepositories(Project project) {
//        project.repositories.mavenCentral()
//        project.repositories.gradlePluginPortal()
//
//        if (project.extensions.getByType(AsciidoctorGradleProjectExtension).snapshot) {
//            project.repositories.mavenLocal()
//        }
//    }

//    void configureJava(Project project) {
//        final java = project.extensions.getByType(JavaPluginExtension)
//        final ver = project.providers.gradleProperty('jdkVersion').orElse('8').get()
//        java.toolchain {
//            it.languageVersion.set(JavaLanguageVersion.of(ver))
//        }
//    }

    @CompileDynamic
    void addDefaultVersions(Project project) {
        project.ext {
            defaultNodeJsVersion = '0'//NodeJSExtension.NODEJS_DEFAULT
        }
    }

    @CompileDynamic
    void configureIdea(Project project) {
        project.pluginManager.withPlugin('idea') {
            IdeaModel ideaModel = project.extensions.getByName('idea')
            ideaModel.module {
                resourceDirs += ModuleVersions.baseFolderFor(project)
            }
            project.tasks.named('ideaModule').configure {
                dependsOn project.tasks.named(GENERATOR_NAME)
            }
        }
    }
}