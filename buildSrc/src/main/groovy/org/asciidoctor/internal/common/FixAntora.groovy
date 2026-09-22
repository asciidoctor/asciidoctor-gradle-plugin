package org.asciidoctor.internal.common

import groovy.transform.CompileStatic
import groovy.yaml.YamlBuilder
import groovy.yaml.YamlSlurper
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant5.api.core.Version
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

@CompileStatic
class FixAntora extends GrolifantDefaultTask {
    private final RegularFile antoraYml
    private final Provider<String> versionProvider

    FixAntora() {
        this.antoraYml = project.layout.projectDirectory.file('docs/antora.yml')
        this.versionProvider = projectTools().versionProvider
    }

    @TaskAction
    void exec() {
        final projectVersion = versionProvider.get()
        final ver = Version.of(projectVersion)
        final ymlFile = antoraYml.asFile
        final map = (Map) new YamlSlurper().parse(ymlFile)
        map['version'] = "${ver.major}.${ver.minor}"
        map['asciidoc']['attributes']['release-version'] = projectVersion

        final yml = new YamlBuilder()
        yml.call(map)
        ymlFile.withWriter { w ->
            yml.writeTo(w)
        }
    }
}
