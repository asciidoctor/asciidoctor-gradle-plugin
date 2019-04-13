import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

/**
 * @since 3.0.0
 */
@CompileStatic
class ModuleVersions extends DefaultTask {

    public final static String BASEFOLDER_NAME = '.asciidoctor-module-versions.generated'
    public final static String INTERMEDIATE_FOLDER_PATH = 'META-INF/asciidoctor.gradle'

    private final Map<String,String> additionalProperties = [:]

    static File baseFolderFor(Project project) {
        new File(project.rootProject.projectDir, "${BASEFOLDER_NAME}/${project.name}")
    }

    static File metaInfFolderFor(Project project) {
        new File(baseFolderFor(project), INTERMEDIATE_FOLDER_PATH)
    }

    ModuleVersions() {
        onlyIf { !propertyNames.empty }
        group = 'plugin development'
        description = 'Generates a resource file containing module versions'
    }

    @Input
    final List<Pattern> propertyNames = []

    @Input
    String basename

    void propertyNames(Pattern... patterns) {
        this.propertyNames.addAll(patterns.toList())
    }

    @Input
    Map<String,String> getAdditionalProperties() {
        this.additionalProperties
    }

    @InputFile
    File getModulePropertiesFile() {
        new File(project.rootProject.projectDir, 'module-versions.properties')
    }

    @OutputFile
    File getOutputFile() {
        new File(metaInfFolderFor(project),"${basename}.properties")
    }

    void additionalProperties(Map<String,String> props) {
        this.additionalProperties.putAll(props)
    }

    @TaskAction
    void create() {
        Properties allProps = new Properties()
        modulePropertiesFile.withInputStream { strm ->
            allProps.load(strm)
        }
        Map<Object, Object> filtered = allProps.findAll { key, value ->
            propertyNames.any { pat ->
                key.toString() =~ pat
            }
        }

        outputFile.withOutputStream { strm ->
            Properties props = new Properties()
            props.putAll(filtered)
            props.putAll(additionalProperties)
            props.store(strm, "Asciidoctor module versions for ${basename}")
        }
    }
}
