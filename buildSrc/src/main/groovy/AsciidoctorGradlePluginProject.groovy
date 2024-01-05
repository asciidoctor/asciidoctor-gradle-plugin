import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test

import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import static org.gradle.api.tasks.SourceSet.TEST_SOURCE_SET_NAME

@CompileStatic
class AsciidoctorGradlePluginProject implements Plugin<Project> {
    void apply(Project project) {
        project.pluginManager.identity {
            apply 'maven-publish'
            apply 'groovy-gradle-plugin'
            apply 'java-gradle-plugin'
            apply 'com.gradle.plugin-publish'
            apply AsciidoctorGradleGroovyProject
        }

        final agProject = project.extensions.getByType(AsciidoctorGradleProjectExtension)
        final sourceSets = project.extensions.getByType(SourceSetContainer)
        final main = sourceSets.getByName(MAIN_SOURCE_SET_NAME)
        final test = sourceSets.getByName(TEST_SOURCE_SET_NAME)

        project.dependencies.identity {
            add(main.implementationConfigurationName, gradleApi())
            add(main.apiConfigurationName, "org.ysb33r.gradle:grolifant-herd:${agProject.versionOf('grolifant')}")
        }

        addTestDependencies(agProject, project.dependencies, test.implementationConfigurationName)

        project.configurations.whenObjectAdded { Configuration configuration ->
            if (configuration.name.contains('Test')) {
                addTestDependencies(agProject, project.dependencies, configuration.name)
            }
        }

        project.tasks.withType(Test).configureEach { Test t ->
            t.useJUnitPlatform()
        }
    }

    static void addTestDependencies(
            AsciidoctorGradleProjectExtension agProject,
            DependencyHandler dependencies,
            String configurationName
    ) {
        dependencies.identity {
            add(configurationName, it.project(path: ':testfixtures-jvm'))
            add(configurationName, "org.spockframework:spock-core:${agProject.versionOf('spock')}") { ExternalModuleDependency emd ->
                emd.identity {
                    exclude(group: 'org.codehaus.groovy')
                    exclude(group: 'org.hamcrest', module: 'hamcrest-core')
                }
            }
        }
    }
}
