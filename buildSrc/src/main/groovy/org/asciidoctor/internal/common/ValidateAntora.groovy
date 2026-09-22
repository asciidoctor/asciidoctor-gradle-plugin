package org.asciidoctor.internal.common

import groovy.transform.CompileStatic
import groovy.yaml.YamlSlurper
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant5.api.core.StringTools
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

@CompileStatic
class ValidateAntora extends GrolifantDefaultTask {
    private final RegularFile antoraYml
    private final Provider<String> versionProvider
    private final Provider<RegularFile> outputFile

    ValidateAntora() {
        this.antoraYml = project.layout.projectDirectory.file('docs/antora.yml')
        this.outputFile = project.layout.buildDirectory.file('.antora-yml-checked')
        this.versionProvider = projectTools().versionProvider
        inputs.property('version', versionProvider)
        inputs.file(antoraYml)
        outputs.file(outputFile)
    }

    @TaskAction
    void exec() {
        final map = new YamlSlurper().parse(antoraYml.asFile)
        final antoraNamedVersion = map['version'].toString()
        final antoraReleaseVersion = map['asciidoc']['attributes']['release-version'].toString()
        final projectVersion = versionProvider.get()

        if (!antoraReleaseVersion.startsWith(antoraNamedVersion)) {
            throw new GradleException(
                    'The version named in antora.yml does not match up with the release version.\nThe major + minor of ' +
                            'the two settings must match.\n' +
                            "Currently version=${antoraNamedVersion} and " +
                            "asciidoc.attributes.release-version is ${antoraReleaseVersion}.\n" +
                            "You can fix this by running ${RootPlugin.FIX_ANTORA_TASK}."
            )
        }

        if (antoraReleaseVersion != projectVersion.replaceFirst(~/-SNAPSHOT$/, '')) {
            throw new GradleException(
                    'The asciidoc.attributes.release-version does not match the project version.\n' +
                            "asciidoc.attributes.release-version = ${antoraReleaseVersion} and " +
                            "project version = ${projectVersion}.\n" +
                            "You can fix this by running ${RootPlugin.FIX_ANTORA_TASK}."
            )
        }

        outputFile.get().asFile.text = StringTools.EMPTY
    }
}
