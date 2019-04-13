import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.ysb33r.gradle.nodejs.NodeJSExtension

@CompileStatic
class AsciidoctorGradleGroovyProject implements Plugin<Project> {

    public final static String GENERATOR_NAME = 'generateModuleVersions'

    void apply(Project project) {
        project.apply plugin: 'groovy'

        TaskProvider generateModuleVersions = project.tasks.register(GENERATOR_NAME, ModuleVersions)

        SourceSetContainer sourceSets = project.extensions.getByType(SourceSetContainer)
        SourceSet main = sourceSets.getByName('main')

        project.tasks.named(main.processResourcesTaskName).configure { Copy copy ->
            copy.from generateModuleVersions, { CopySpec cs ->
                cs.into "${ModuleVersions.INTERMEDIATE_FOLDER_PATH}"
            }
        }

        addDefaultVersions(project)
        configureIdea(project)
    }

    @CompileDynamic
    void addDefaultVersions(Project project) {
        project.ext {
            defaultNodeJsVersion = NodeJSExtension.NODEJS_DEFAULT
        }
    }

    @CompileDynamic
    void configureIdea(Project project) {
        project.pluginManager.withPlugin('idea') {
            IdeaModel ideaModel = project.extensions.getByName('idea')
            ideaModel.module {
                resourceDirs += ModuleVersions.baseFolderFor(project)
            }
            project.tasks.named('ideaModule').configure {
                dependsOn project.tasks.named(GENERATOR_NAME)
            }
        }
    }
}