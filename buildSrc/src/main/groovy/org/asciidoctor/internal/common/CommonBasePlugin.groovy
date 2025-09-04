package org.asciidoctor.internal.common

import groovy.transform.CompileStatic
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.testing.Test
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin

import java.time.LocalDate

/**
 * Common plugin stuff for both classic and model 5.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class CommonBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply 'java-library'
            apply 'groovy'
            apply 'com.github.hierynomus.license'
            apply GrolifantServicePlugin
        }

        project.extensions.create('agProject', AsciidoctorGradleProjectExtension, project)
        configureRepositories(project)
        configureLicense(project)
        configureTestCommons(project)
        configureGroovydocLinks(project)
    }

    private void configureRepositories(Project project) {
        project.repositories.mavenCentral()
        project.repositories.gradlePluginPortal()

        if (project.extensions.getByType(AsciidoctorGradleProjectExtension).snapshot) {
            project.repositories.mavenLocal()
        }
    }

    private void configureLicense(Project project) {
        final currentYear = LocalDate.now().year.toString()
        final inceptionYear = project.providers.gradleProperty('projectInceptionYear').get()
        final yearRange = "${inceptionYear} - ${currentYear}"

        final license = project.extensions.getByType(LicenseExtension).tap {
            header = new File(project.rootDir, 'gradle/license/HEADER')
            strictCheck = true
            ignoreFailures = false
            excludes([
                    '**/*.ad',
                    '**/*.asciidoc',
                    '**/*.adoc',
                    '**/fake.txt',
                    '**/*.properties'
            ])
        }

        ((ExtensionAware) license).extensions.extraProperties.set('year', yearRange)
    }

    private void configureTestCommons(Project project) {
        final offline = project.gradle.startParameter.offline.toString()
        project.tasks.withType(Test).configureEach {
            it.systemProperty('IS_OFFLINE', offline)
            it.systemProperty('OFFLINE_REPO', new File(project.rootDir, '.offline-repo').absolutePath)
        }
    }

    private void configureGroovydocLinks(Project project) {
        final agProject = project.extensions.getByType(AsciidoctorGradleProjectExtension)
        project.tasks.withType(Groovydoc).configureEach { t ->
            t.include('**/*.java')
            t.link(
                "https://grolifant.ysb33r.org/grolifant-plugin-development/${agProject.versionOf('grolifant')}/project-artifacts/_attachments/-grolifant5-core/groovydoc/",
                'org.ysb33r.grolifant5.api'
            )
            t.link(
                "https://docs.gradle.org/${project.gradle.gradleVersion}/javadoc",
                'org.gradle'
            )
        }
    }
}
