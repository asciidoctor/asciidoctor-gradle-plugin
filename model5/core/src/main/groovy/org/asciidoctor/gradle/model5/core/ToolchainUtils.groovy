package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.PublicationUtils
import org.asciidoctor.gradle.model5.core.internal.TaskRegistrar
import org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTaskMethods
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider

@CompileStatic
class ToolchainUtils {

    /**
     * Registers the task if it has not already been registered.
     *
     * @param name Name of task.
     * @param taskType Task type
     * @param objectFactory Access to {@link ObjectFactory} instance.
     * @param configurator Task configurator
     * @return Provider to the task.
     */
    static <T extends DefaultTask> TaskProvider<T> registerOnce(
            String name,
            Class<T> taskType,
            ObjectFactory objects,
            Action<T> configurator
    ) {
        objects.newInstance(TaskRegistrar).register(name, taskType, configurator)
    }

    /**
     * Configure standard providers fdr a task that implements {@link AsciidoctorTaskMethods}
     *
     * @param task Task to configure
     * @param pub Publication to use for configuration.
     */
    static void configureFromStandardPublicationProviders(
            AsciidoctorTaskMethods task,
            AsciidoctorPublication pub
    ) {
        task.tap {
            attributes = pub.attributes.attributeResolver
            safeMode = pub.processingOptions.safeMode
            logDocuments = pub.processingOptions.isLogDocuments()
            baseDirStrategy = pub.baseDir.baseDirStrategy
            sourceDir = pub.sourceDir
            sourcePatterns = pub.sourcePatterns
            secondarySourcePatterns = pub.secondarySourcePatterns
        }
    }

    /**
     * The name of an Asciidoctor task.
     *
     * @param tc Toolchain
     * @param fmt Output formatter
     * @param pub Publication
     * @return Task name.
     */
    static String asciidoctorTaskName(AsciidoctorToolchain tc, AsciidoctorOutputFormatter fmt, AsciidoctorPublication pub) {
        final pubName = pub.name == PublicationUtils.DEFAULT_PUBLICATION ? '' : pub.name.capitalize()
        final tcNameCheck = tc.name.toLowerCase(Locale.US)
        final tcName = tcNameCheck ==~ /.*?asciidoctor.*?/ ? tc.name : "${tc.name}Asciidoctor"
        "${tcName.uncapitalize()}${fmt.name.capitalize()}${pubName}"
    }
}
