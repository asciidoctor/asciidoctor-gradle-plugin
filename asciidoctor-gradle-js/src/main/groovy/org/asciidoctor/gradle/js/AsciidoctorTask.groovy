package org.asciidoctor.gradle.js

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.OutputOptions
import org.gradle.api.Action
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.FileUtils

import javax.inject.Inject

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * @since 3.0
 */
@CompileStatic
class AsciidoctorTask extends AbstractAsciidoctorTask {

    @Inject
    AsciidoctorTask(WorkerExecutor we) {
        super(we)
        final String taskPrefix = 'asciidoctor'
        String folderName
        if (name.startsWith(taskPrefix)) {
            folderName = name.replaceFirst(taskPrefix, 'asciidoc')
        } else {
            folderName = "asciidoc${name.capitalize()}"
        }
        final String safeFolderName = FileUtils.toSafeFileName(folderName)
        sourceDir = "src/docs/${folderName}"
        outputDir = { project.file("${project.buildDir}/docs/${safeFolderName}") }
    }

    /** Configures output options for this task.
     *
     * @param cfg Closure which will delegate to a {@link org.asciidoctor.gradle.base.OutputOptions} instance.
     */
    void outputOptions(Closure cfg) {
        Closure configurator = (Closure) cfg.clone()
        configurator.delegate = this.configuredOutputOptions
        configurator.resolveStrategy = DELEGATE_FIRST
        configurator.call()
    }

    /** Configures output options for this task.
     *
     * @param cfg Action which will be passed an instances of {@link org.asciidoctor.gradle.base.OutputOptions} to configure.
     */
    void outputOptions(Action<OutputOptions> cfg) {
        cfg.execute(this.configuredOutputOptions)
    }
}
