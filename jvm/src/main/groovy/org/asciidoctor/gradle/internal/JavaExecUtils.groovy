/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.gradle.internal

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.asciidoctor.gradle.remote.AsciidoctorJavaExec
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.ysb33r.grolifant.api.core.ProjectOperations

import java.util.regex.Pattern

import static org.asciidoctor.gradle.base.AsciidoctorUtils.getClassLocation

/** Utilities for dealing with Asciidoctor in an external JavaExec process.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.0
 */
@CompileStatic
@Slf4j
class JavaExecUtils {

    /** The {@code jruby-complete} dependency without a version.
     *
     */
    public static final String JRUBY_COMPLETE_DEPENDENCY = 'org.jruby:jruby-complete'

    /** Get the classpath that needs to be passed to the external Java process.
     *
     * @param project Current Gradle project
     * @param asciidoctorClasspath External asciidoctor dependencies
     * @return A computed classpath that can be given to an external Java process.
     *
     * @deprecated
     */
    @Deprecated
    static FileCollection getJavaExecClasspath(
            final Project project,
            final FileCollection asciidoctorClasspath
    ) {
        File entryPoint = getClassLocation(AsciidoctorJavaExec)
        File groovyJar = getClassLocation(GroovyObject)

        FileCollection fc = project.files(entryPoint, groovyJar, asciidoctorClasspath)

        fc
    }

    /** Get the classpath that needs to be passed to the external Java process.
     *
     * @param project Current Gradle project
     * @param asciidoctorClasspath External asciidoctor dependencies
     * @return A computed classpath that can be given to an external Java process.
     */
    static FileCollection getJavaExecClasspath(
            final ProjectOperations po,
            final FileCollection asciidoctorClasspath
    ) {
        File entryPoint = getClassLocation(AsciidoctorJavaExec)
        File groovyJar = getClassLocation(GroovyObject)

        final fc = po.fsOperations.emptyFileCollection()
        fc.from(entryPoint, groovyJar)

        fc + asciidoctorClasspath
    }

    /** The file to which execution configuration data can be serialised to.
     *
     * @param task Task for which execution data will be serialised.
     * @return File that will (eventually) contain the execution data.
     */
    static File getExecConfigurationDataFile(final Task task) {
        final fso = ProjectOperations.find(task.project).fsOperations
        task.project.file("${task.project.buildDir}/tmp/${fso.toSafeFileName(task.name)}.javaexec-data")
    }

    /**
     * Serializes execution configuration data.
     *
     * @param task Task for which execution data will be serialised.
     * @param executorConfigurations Executor configuration to be serialised
     * @return File that the execution data was written to.
     *
     * @deprecated
     */
    @Deprecated
    static File writeExecConfigurationData(final Task task, Iterable<ExecutorConfiguration> executorConfigurations) {
        log.debug("Executor configurations: ${executorConfigurations}")
        File execConfigurationData = getExecConfigurationDataFile(task)
        execConfigurationData.parentFile.mkdirs()
        ExecutorConfigurationContainer.toFile(execConfigurationData, executorConfigurations)
        execConfigurationData
    }

    /**
     * Serializes execution configuration data.
     *
     * @param execConfigurationData File to be use for serialization data.
     * @param executorConfigurations Executor configuration to be serialised
     * @return File that the execution data was written to.
     *
     * @since 4.0
     */
    static void writeExecConfigurationData(
            final File execConfigurationData,
            Iterable<ExecutorConfiguration> executorConfigurations
    ) {
        log.debug("Executor configurations: ${executorConfigurations}")
        execConfigurationData.parentFile.mkdirs()
        ExecutorConfigurationContainer.toFile(execConfigurationData, executorConfigurations)
        execConfigurationData
    }

    /**
     * Returns the location of the local Groovy Jar that is used by Gradle.
     *
     * @return Location on filesystem where the Groovy Jar is located.
     */
    static File getLocalGroovy() {
        getClassLocation(GroovyObject)
    }

    static File getInternalGradleLibraryLocation(ProjectOperations po, final Pattern libraryPattern) {
        final filter = new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                name.matches(libraryPattern)
            }
        }

        File[] files = new File(po.gradleHomeDir.get(), 'lib').listFiles(filter)

        if (!files) {
            throw new InternalGradleLibraryLocationException(
                    "Cannot locate a library in the Gradle distribution using ${libraryPattern}"
            )
        } else if (files.size() > 1) {
            throw new InternalGradleLibraryLocationException(
                    "Found more than one library matching ${libraryPattern} in the Gradle distribution: ${files*.name}"
            )
        }
        files[0]
    }

    /** Thrown when an internal Guava JAR cannot be located.
     *
     * @since 4.0
     */
    @InheritConstructors
    static class InternalGradleLibraryLocationException extends RuntimeException {
    }
}
