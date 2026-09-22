package org.asciidoctor.internal.classic

import groovy.transform.CompileStatic
import org.asciidoctor.internal.common.AsciidoctorGradleProjectExtension
import org.asciidoctor.internal.common.RemoveSpockGroovyDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.plugins.quality.CodeNarcExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.base.TestingExtension

import static org.gradle.api.logging.LogLevel.INFO
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME

@CompileStatic
class AsciidoctorGradlePluginProject implements Plugin<Project> {
    void apply(Project project) {
        project.pluginManager.identity {
            apply 'maven-publish'
            apply 'groovy-gradle-plugin'
            apply 'java-gradle-plugin'
            apply 'com.gradle.plugin-publish'
            apply 'jvm-test-suite'
            apply 'org.ysb33r.ivypot'
            apply AsciidoctorGradleGroovyProject
        }

        configureJava(project)

        addMainDependencies(project)
        addTestDependencies(project)
        configureCodenarc(project)

        project.tasks.withType(Test).configureEach { Test t ->
            t.useJUnitPlatform()
        }
    }

    private void addMainDependencies(Project project) {
        final agProject = project.extensions.getByType(AsciidoctorGradleProjectExtension)
        final sourceSets = project.extensions.getByType(SourceSetContainer)
        final main = sourceSets.getByName(MAIN_SOURCE_SET_NAME)
        project.dependencies.identity {
            add(main.implementationConfigurationName, localGroovy())
            add(main.implementationConfigurationName, gradleApi())
            add(main.apiConfigurationName, "org.ysb33r.gradle:grolifant5-core:${agProject.versionOf('grolifant')}")
            add(main.runtimeOnlyConfigurationName, "org.ysb33r.gradle:grolifant5-herd:${agProject.versionOf('grolifant')}")
        }
    }

    private void addTestDependencies(Project project) {
        project.extensions.getByType(TestingExtension).suites.withType(JvmTestSuite).configureEach {
            it.useSpock()
            it.dependencies.implementation.add(it.dependencies.project(':testfixtures-jvm'))
            it.dependencies.implementation.add(it.dependencies.project())
            it.dependencies.implementation.add(it.dependencies.gradleApi())
            it.dependencies.implementation.add(it.dependencies.gradleTestKit())
        }

        project.dependencies.components.withModule(
                'org.spockframework:spock-core',
                RemoveSpockGroovyDependency
        )
    }

    private void configureJava(Project project) {
        final java = project.extensions.getByType(JavaPluginExtension)
        java.tap {
            withJavadocJar()
            withSourcesJar()
        }
    }

    private void configureCodenarc(Project project) {
        project.pluginManager.apply('codenarc')
        final codenarc = project.extensions.getByType(CodeNarcExtension)
        codenarc.configFile = project.file("${project.rootDir}/gradle/codenarc/codenarc.groovy")
        codenarc.sourceSets = [project.extensions.getByType(SourceSetContainer).getByName('main')]

        if (project.gradle.startParameter.logLevel == INFO) {
            codenarc.reportFormat = 'console'
        }

        project.tasks.register('codenarcAll') {
            it.dependsOn('codenarcMain')
        }
    }
}
