import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class AsciidoctorGradlePluginProject implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: AsciidoctorGradleGroovyProject
    }
}
