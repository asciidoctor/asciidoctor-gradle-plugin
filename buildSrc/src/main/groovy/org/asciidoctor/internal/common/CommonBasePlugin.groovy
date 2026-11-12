package org.asciidoctor.internal.common

import groovy.transform.CompileStatic
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.javadoc.GroovydocAccess
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.signing.SigningExtension
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin

import java.time.LocalDate

/**
 * Common plugin stuff for both classic and model 5.
 *
 * @author Schalk W. Cronjé
 * @author Mattias Reichel
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
        configureSigning(project)
    }

    /**
     * Signs the published plugins when a signing key is available.
     * <p>
     * The Plugin Publish plugin signs the plugin and plugin marker publications by itself
     * once the Signing plugin is applied.
     * The properties are the same as in AsciidoctorJ: {@code signingKey}, {@code signingPassword}
     * and optionally {@code signingKeyId}, for instance from {@code ORG_GRADLE_PROJECT_signingKey},
     * or {@code signing.keyId} for a key from a key ring as described for the Signing plugin.
     */
    private void configureSigning(Project project) {
        final signingKey = project.providers.gradleProperty('signingKey')
        final keyRingKeyId = project.providers.gradleProperty('signing.keyId')
        if (!signingKey.present && !keyRingKeyId.present) {
            return
        }

        project.pluginManager.withPlugin('com.gradle.plugin-publish') {
            project.pluginManager.apply('signing')
            if (signingKey.present) {
                final signing = project.extensions.getByType(SigningExtension)
                final signingKeyId = project.providers.gradleProperty('signingKeyId').orNull
                final signingPassword = project.providers.gradleProperty('signingPassword').orNull
                if (signingKeyId) {
                    signing.useInMemoryPgpKeys(signingKeyId, signingKey.get(), signingPassword)
                } else {
                    signing.useInMemoryPgpKeys(signingKey.get(), signingPassword)
                }
            }
        }
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
            t.access.set(GroovydocAccess.PROTECTED)
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
