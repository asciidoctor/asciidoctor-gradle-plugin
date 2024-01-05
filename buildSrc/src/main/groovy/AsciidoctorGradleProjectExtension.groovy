import com.gradle.publish.PluginBundleExtension
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.ysb33r.grolifant.api.core.ProjectOperations

@CompileStatic
class AsciidoctorGradleProjectExtension {

    private final ProjectOperations projectOperations
    private final ConfigurationContainer configurations
    private final ExtensionContainer extensions
    private final Provider<String> pluginExtraTextProvider

    AsciidoctorGradleProjectExtension(Project project) {
        this.projectOperations = ProjectOperations.find(project)
        this.configurations = project.configurations
        this.extensions = project.extensions

        this.pluginExtraTextProvider = projectOperations.versionProvider.map { version ->
            (version.contains('-alpha') || version.contains('-beta')) ?
                    ". (If you need a production-ready version of the AsciidoctorJ plugin for Gradle use a 3.x release of this plugin instead)."
                    : ''
        }
    }

    void withOfflineTestConfigurations() {
        final intTestOfflineRepo = configurations.maybeCreate('intTestOfflineRepo')
        final intTestOfflineRepo2 = configurations.maybeCreate('intTestOfflineRepo2')
        intTestOfflineRepo.tap {
            extendsFrom(configurations.getByName('compileOnly'))
            canBeResolved = true
            canBeConsumed = false
        }
        intTestOfflineRepo2.tap {
            canBeResolved = true
            canBeConsumed = false
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

    String versionOf(String versionPropName) {
        // For now it reads a Gradle property. IN future it will will read from a TOML file.
        projectOperations.gradleProperty(
                "${versionPropName}Version",
                projectOperations.atConfigurationTime()
        ).get()
    }

    void configurePlugin(
            String pluginId,
            String providedDisplayName,
            String providedDescription,
            String implClass,
            List<String> providedTags
    ) {
        final gradlePlugin = extensions.getByType(GradlePluginDevelopmentExtension)
        final pluginBundle = extensions.getByType(PluginBundleExtension)
        final providedName = "${pluginId.replaceAll(~/\./, '')}Plugin".toString()
        final extraText = pluginExtraTextProvider.get()
        gradlePlugin.plugins.create(providedName) { PluginDeclaration pd ->
            pd.tap {
                id = pluginId
                displayName = providedDisplayName
                description = extraText ? "${providedDescription}. ${extraText}" : "${extraText}."
                implementationClass = implClass
            }
        }
        if (pluginBundle.pluginTags.isEmpty()) {
            pluginBundle.pluginTags = [(providedName): (Collection<String>)(['asciidoctor'] + providedTags)]
        }
        pluginBundle.pluginTags.putAll([(providedName): (['asciidoctor'] + providedTags)])
    }
}
