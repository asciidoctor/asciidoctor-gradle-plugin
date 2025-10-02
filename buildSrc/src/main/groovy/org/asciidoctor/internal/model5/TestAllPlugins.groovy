package org.asciidoctor.internal.model5

import groovy.transform.CompileStatic
import org.asciidoctor.internal.common.AsciidoctorGradleProjectExtension
import org.asciidoctor.internal.common.CommonBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.base.TestingExtension
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

        gradleTest.testSets.named('main') { GradleTestSet ts ->
            ts.jdk.useJdk(11)
            ts.deprecationMessages.ignoreIf('Use JVM 17 or greater to execute Gradle')
        }

        gradleTest.testSets.create('configCache') { GradleTestSet ts ->
            ts.versions(withConfigCache)
            ts.jdk.useJdk(11)
            ts.configurationCache.mode = 'fail'
            ts.forVersionsMatching(~/^8\.14/) {
                deprecationMessages.failIfFound = false
            }
            ts.deprecationMessages.ignoreIf('Use JVM 17 or greater to execute Gradle')
        }

        gradleTest.testSets.create('jdk17') { GradleTestSet ts ->
            ts.jdk.useJdk(17)
            ts.versions(gradle9OrLater)
            ts.gradleArguments('-s')
            ts.debug = true

            ts.forVersionsMatching(~/^9\.[789]/) {
                isolatedProjects.mode = 'fail'
            }
        }

        gradleTest.testSets.create('jdk21') { GradleTestSet ts ->
            ts.jdk.useJdk(21)
            ts.versions(gradle9OrLater)
            ts.configurationCache.mode = 'fail'
        }

        gradleTest.testSets.all { GradleTestSet ts ->
            ts.copyNotSymlink(true)
            ts.useCustomManifestPerVersion { cmc ->
                cmc.useGradleApiVersion {
                    final ver = Version.of(it)
                    switch (ver.major) {
                        case '7':
                            return '7.3.3'
                        case '8':
                            return '8.0'
                        default:
                            return "${ver.major}.0.0"
                    }
                }
            }
        }

        project.tasks.withType(Test).configureEach {
            it.forkEvery = 1
            it.maxParallelForks = 2
        }

        final coreConfig = project.configurations.getByName('gradleTestImplementation')
        project.configurations.getByName('configCacheGradleTestImplementation').extendsFrom(coreConfig)

        [
            'jdk17',
            'jdk21'
        ].each { cfg ->
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
        }
    }
}
