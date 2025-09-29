package org.asciidoctor.internal.model5

import groovy.transform.CompileStatic
import org.asciidoctor.internal.common.AsciidoctorGradleProjectExtension
import org.asciidoctor.internal.common.CommonBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolutionStrategy
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.testing.base.TestingExtension
import org.gradle.util.GradleVersion
import org.ysb33r.gradle.gradletest.ClasspathManifest
import org.ysb33r.gradle.gradletest.ConfigurationCacheMode
import org.ysb33r.gradle.gradletest.GradleTestSet
import org.ysb33r.gradle.gradletest.GradleTestSetExtension
import org.ysb33r.grolifant5.api.core.Version

@CompileStatic
class TestAllPlugins implements Plugin<Project> {
    public static final Version CONFIG_CACHE_MINIMUM_VER = Version.of('8.9')
    public static final Version GRADLE_8_NO_DEPRECATION_WARNINGS = Version.of('8.11')

    @Override
    void apply(Project project) {
        project.pluginManager.tap {
            apply(CommonBasePlugin)
            apply('jvm-test-suite')
            apply('org.ysb33r.ivypot')
        }

        final agProject = project.extensions.getByType(AsciidoctorGradleProjectExtension)
        agProject.withCompatibilityTests()

        final gradleTest = project.extensions.getByType(GradleTestSetExtension)

        final vers = project.providers.gradleProperty('minGradle').zip(
                project.providers.gradleProperty('otherGradleTestVersions')
        ) { min, others ->
            final moreVersions = others.split(',')
            moreVersions.toList() + [min]
        }

        final withConfigCache = vers.map { list ->
            list.findAll { Version.of(it) >= CONFIG_CACHE_MINIMUM_VER }
        }

        final gradle9OrLater = vers.map { list -> list.findAll { !it.startsWith('8.') } }

        gradleTest.testSets.create('configCache') { GradleTestSet ts ->
            ts.versions(withConfigCache)
            ts.configurationCacheMapping { ConfigurationCacheMode.FAIL }

            // Turn off deprecation warnings for these 8.x versions as they will complain about build running < JDK17
            vers.get().findAll {
                it.startsWith('8.') && Version.of(it) >= GRADLE_8_NO_DEPRECATION_WARNINGS
            }.each {
                ts.deprecationMessageChecksForVersion(it, [])
            }
            ts.defaultDeprecationMessageChecks([])
        }

        gradleTest.testSets.create('jdk17') { GradleTestSet ts ->
            ts.versions(gradle9OrLater)
            ts.gradleArguments('-s')
        }

        gradleTest.testSets.create('jdk21') { GradleTestSet ts ->
            ts.versions(gradle9OrLater)
            ts.configurationCacheMapping { ConfigurationCacheMode.FAIL }
        }

        gradleTest.testSets.all { GradleTestSet ts ->
            ts.useCustomManifest()
            ts.copyNotSymlink(true)
        }

        final tc = project.extensions.getByType(JavaToolchainService)
        final jdk11 = tc.launcherFor { it.languageVersion.set(JavaLanguageVersion.of(11)) }
        final jdk17 = tc.launcherFor { it.languageVersion.set(JavaLanguageVersion.of(17)) }
        final jdk21 = tc.launcherFor { it.languageVersion.set(JavaLanguageVersion.of(21)) }
        project.tasks.withType(Test).configureEach {
            if (it.name == 'gradleTest') {
                it.javaLauncher.set(jdk11)
                it.dependsOn("${it.name}ClasspathManifest")
            } else if (it.name == 'configCacheGradleTest') {
                it.javaLauncher.set(jdk11)
                it.dependsOn("${it.name}ClasspathManifest")
            } else if (it.name == 'jdk17GradleTest') {
                it.javaLauncher.set(jdk17)
                it.dependsOn("${it.name}ClasspathManifest")
            } else if (it.name == 'jdk21GradleTest') {
                it.javaLauncher.set(jdk21)
                it.dependsOn("${it.name}ClasspathManifest")
            }
            it.forkEvery = 1
            it.maxParallelForks = 2
        }

        final coreConfig = project.configurations.getByName('gradleTestImplementation')
        project.configurations.getByName('configCacheGradleTestImplementation').extendsFrom(coreConfig)

        [
                'jdk17',
                'jdk21'
        ].each { cfg ->
            final implementation = project.configurations.getByName("${cfg}GradleTestImplementation").tap {
                extendsFrom(coreConfig)
            }

            final manifestClasspath = project.configurations.getByName("${cfg}GradleTestRuntimeClasspath").filter { File f ->
                !f.path.contains("gradle-${GradleVersion.current().version}")
            }

            final compilerClasspath = project.configurations.create("${cfg}GradleTestCompiler")
            final gradleVer = agProject.versionOf('groovyForGradle9Testing')
            project.dependencies.add(compilerClasspath.name, "org.apache.groovy:groovy:${gradleVer}")

            project.extensions.getByType(TestingExtension).suites
                    .named("${cfg}GradleTest", JvmTestSuite)
                    .configure { jts ->
                        jts.useSpock(agProject.versionOf('spock').replace('-3.0', '-4.0'))
                    }

            project.tasks.named("compile${cfg.capitalize()}GradleTestGroovy", GroovyCompile) {
                it.groovyClasspath = compilerClasspath
            }

            project.tasks.named("${cfg}GradleTestClasspathManifest", ClasspathManifest) {
                it.classpath.setFrom(manifestClasspath)
            }
        }
    }
}
