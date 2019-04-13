import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class AsciidoctorGradleModuleVersions implements Plugin<Project> {
    void apply(Project project) {
        File versionsFile = project.file("${project.rootProject.projectDir}/module-versions.properties")

        Properties props = new Properties()
        versionsFile.withInputStream { stream ->
            props.load(stream)
        }

        project.extensions.create('moduleVersions', ModuleVersions, props)
    }

    static class ModuleVersions {
        @Delegate
        final Map<String, Object> props

        ModuleVersions(Properties p) {
            props = p as Map<String, Object>
        }
    }
}

