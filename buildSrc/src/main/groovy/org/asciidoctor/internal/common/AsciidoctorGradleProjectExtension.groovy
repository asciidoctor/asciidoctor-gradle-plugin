package org.asciidoctor.internal.common

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.plugin.compatibility.CompatibilityExtension
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.gradle.testing.base.TestingExtension
import org.ysb33r.gradle.gradletest.GradleTestSetExtension
import org.ysb33r.grolifant5.api.core.ProjectOperations

import static org.asciidoctor.internal.classic.ModuleVersions.INTERMEDIATE_FOLDER_PATH

@CompileStatic
@Slf4j
class AsciidoctorGradleProjectExtension {

    final boolean snapshot

    private final Project project
    private final ProjectOperations projectOperations
    private final ConfigurationContainer configurations
    private final ExtensionContainer extensions
    private final Provider<String> pluginExtraTextProvider

    AsciidoctorGradleProjectExtension(Project project) {
        this.project = project
        this.projectOperations = ProjectOperations.find(project)
        this.configurations = project.configurations
        this.extensions = project.extensions
        this.snapshot = projectOperations.projectTools.versionProvider.get().endsWith('-SNAPSHOT')
        this.pluginExtraTextProvider = projectOperations.versionProvider.map { version ->
            (version.contains('-alpha') || version.contains('-beta')) ?
                ". (If you need a production-ready version of the AsciidoctorJ plugin for Gradle use a 4.x release of this plugin instead)."
                : ''
        }
        withJdkVersionFromProperty('jdkVersion')
    }

    void withJdkVersionFromProperty(String propName) {
        final ver = project.providers.gradleProperty(propName).get()
        extensions.getByType(JavaPluginExtension).toolchain.languageVersion.set(JavaLanguageVersion.of(ver))
    }

    void withIntegrationTests() {
        final testProjects = new File(project.rootDir, 'testfixtures/projects')
        project.extensions.getByType(TestingExtension).suites.create('integrationTest', JvmTestSuite) { jts ->
            jts.tap {
                useSpock(versionOf('spock'))
                targets*.testTask*.configure { t ->
                    t.mustRunAfter('test')
                }
                dependencies.implementation.add(dependencies.project(':testfixtures-jvm'))
            }
        }

        project.tasks.named('check') {
            it.dependsOn('integrationTest')
        }

        project.tasks.named('integrationTest', Test) { t ->
            t.systemProperty('TEST_PROJECTS_DIR', testProjects.absolutePath)
            t.inputs.dir(testProjects)

            t.minHeapSize = "1g"
            t.maxHeapSize = "3g"
            t.jvmArgs = [
                '-XX:+HeapDumpOnOutOfMemoryError'
            ]

        }

        project.pluginManager.withPlugin('java-gradle-plugin') {
            final intTest = project.extensions.getByType(SourceSetContainer).getByName('integrationTest')
            project.extensions.getByType(GradlePluginDevelopmentExtension).testSourceSets(intTest)
        }
    }

    void withClassicOfflineTestConfigurations() {
        ['intTestOfflineRepo', 'intTestOfflineRepo2'].each {
            projectOperations.configurations.createLocalRoleFocusedConfiguration(it, "${it}Resolved")
            configurations.getByName(it).extendsFrom(configurations.getByName('compileOnly'))
        }
    }

    void withOfflineCacheConfiguration() {
        project.configurations.create('cachingOnly').tap {
            canBeConsumed = false
        }
    }

    void withCompatibilityTests() {
        project.pluginManager.apply('org.ysb33r.gradletest')

        final main = project.extensions.getByType(GradleTestSetExtension).testSets.getByName('main')

        final minGradle = projectOperations.providerTools.gradleProperty('minGradle')
        final otherGradle =            projectOperations.providerTools.gradleProperty('otherGradleTestVersions')
                .orElse('')
                .map { vers ->
                    vers.split(',').findAll { it.startsWith('8.') }
                }
        final allGradle = minGradle.zip(otherGradle) { single,list ->
            (list + [single]).toList()
        }

        main.versions(allGradle)
        main.copyNotSymlink(true)
        main.forVersion('8.11.1') {
            deprecationMessages.failIfFound = false
        }
        main.forVersion('8.14.3') {
            deprecationMessages.failIfFound = false
        }
    }

    void withAdditionalPluginClasspath() {
        final apc = configurations.maybeCreate('additionalPluginClasspath')
        apc.tap {
            canBeResolved = true
            canBeConsumed = false
        }
        projectOperations.tasks.whenNamed('pluginUnderTestMetadata', PluginUnderTestMetadata) { t ->
            t.pluginClasspath.from(apc)
        }
    }

    void withVersionSubstitution(String name, Map<String, Object> vers) {
        project.tasks.named('processResources', Copy) { t ->
            final values = projectOperations.stringTools.stringizeValues(vers)
            t.tap {
                t.filesMatching "**/${INTERMEDIATE_FOLDER_PATH}/${name}", { fcd ->
                    fcd.filter org.apache.tools.ant.filters.ReplaceTokens,
                        beginToken: '@@', endToken: '@@',
                        tokens: values
                    fcd.filter { String line ->
                        line.startsWith('#') ? null : line
                    }
                }
            }
            t.inputs.property('versions', values).optional(true)
        }
    }

    String versionOf(String versionPropName) {
        try {
            extensions.getByType(VersionCatalogsExtension)
                .named('libs')
                .findVersion(versionPropName)
                .get().toString()
        } catch (Exception e) {
            final fromProps = projectOperations.gradleProperty("${versionPropName}Version")
            if (fromProps.present) {
                log.warn "Found ${versionPropName} via ${versionPropName}Version in gradle.properties. Please move it to the version catalog."
                fromProps.get()
            } else {
                throw new GradleException("Cannot find ${versionPropName} in catalog libs", e)
            }
        }
    }

    void configurePlugin(
        String pluginId,
        String providedDisplayName,
        String providedDescription,
        String implClass,
        List<String> providedTags,
        boolean ccCompatible = true
    ) {
        final gradlePlugin = extensions.getByType(GradlePluginDevelopmentExtension)
        final providedName = "${pluginId.replaceAll(~/\./, '')}Plugin".toString()
        final extraText = pluginExtraTextProvider.get()
        gradlePlugin.website.set('https://docs.asciidoctor.org/gradle-plugin/latest/')
        gradlePlugin.vcsUrl.set('https://github.com/asciidoctor/asciidoctor-gradle-plugin.git')
        final plugin = gradlePlugin.plugins.create(providedName) { PluginDeclaration pd ->
            pd.tap {
                id = pluginId
                displayName = providedDisplayName
                description = extraText ? "${providedDescription}. ${extraText}" : "${extraText}."
                implementationClass = implClass
                tags.set(['asciidoctor'] + providedTags)
            }
        }
//        setCcFlag(plugin,ccCompatible)
    }

    @CompileDynamic
    private void setCcFlag(PluginDeclaration pd,boolean ccCompatible) {
        pd.compatibility(pd).features.configurationCache.set(ccCompatible)
    }
}
