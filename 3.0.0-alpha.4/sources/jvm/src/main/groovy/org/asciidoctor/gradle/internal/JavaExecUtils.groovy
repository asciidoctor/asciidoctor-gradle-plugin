/*
 * Copyright 2013-2020 the original author or authors.
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
import org.asciidoctor.gradle.remote.AsciidoctorJavaExec
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle
import org.gradle.util.GradleVersion
import org.ysb33r.grolifant.api.FileUtils

import java.util.regex.Pattern

import static org.asciidoctor.gradle.base.AsciidoctorUtils.getClassLocation

/** Utilities for dealing with Asciidoctor in an external JavaExec process.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.0
 */
@CompileStatic
class JavaExecUtils {

    /** The {@code jruby-complete} dependency without a version.
     *
     */
    public static final String JRUBY_COMPLETE_DEPENDENCY = 'org.jruby:jruby-complete'

    /** The name of the Guava JAR used internally by Gradle.
     *
     */
    private static final FilenameFilter INTERNAL_GUAVA_PATTERN = internalGuavaPattern()

    /** Get the classpath that needs to be passed to the external Java process.
     *
     * @param project Current Gradle project
     * @param asciidoctorClasspath External asciidoctor dependencies
     * @param addInternalGuava Set to {@code true} to add internal Guava to classpath
     * @return A computed classpath that can be given to an external Java process.
     */
    static FileCollection getJavaExecClasspath(
        final Project project,
        final FileCollection asciidoctorClasspath,
        boolean addInternalGuava = false
    ) {
        File entryPoint = getClassLocation(AsciidoctorJavaExec)
        File groovyJar = getClassLocation(GroovyObject)

        FileCollection fc = project.files(entryPoint, groovyJar, asciidoctorClasspath)

        addInternalGuava ? project.files(fc, getInternalGuavaLocation(project.gradle)) : fc
    }

    /** The file to which execution configuration data can be serialised to.
     *
     * @param task Task for which execution data will be serialised.
     * @return File that will (eventually) contain the execution data.
     */
    static File getExecConfigurationDataFile(final Task task) {
        task.project.file("${task.project.buildDir}/tmp/${FileUtils.toSafeFileName(task.name)}.javaexec-data")
    }

    /** Serializes execution configuration data.
     *
     * @param task Task for which execution data will be serialised.
     * @param executorConfigurations Executor configuration to be serialised
     * @return File that the execution data was written to.
     */

    static File writeExecConfigurationData(final Task task, Iterable<ExecutorConfiguration> executorConfigurations) {
        File execConfigurationData = getExecConfigurationDataFile(task)
        execConfigurationData.parentFile.mkdirs()
        ExecutorConfigurationContainer.toFile(execConfigurationData, executorConfigurations)
        execConfigurationData
    }

    /** Returns the location of the local Groovy Jar that is used by Gradle.
     *
     * @return Location on filesystem where the Groovy Jar is located.
     */
    static File getLocalGroovy() {
        getClassLocation(GroovyObject)
    }

    /** Locate the internal Guava JAR from the Gradle distribution
     *
     * @param gradle Gradle instance
     * @return Return Guava location. Never {@code null}
     * @throw InternalGuavaLocationException
     */
    static File getInternalGuavaLocation(Gradle gradle) {
        File[] files = new File(gradle.gradleHomeDir, 'lib').listFiles(INTERNAL_GUAVA_PATTERN)

        if (!files) {
            throw new InternalGuavaLocationException('Cannot locate a Guava JAR in the Gradle distribution')
        } else if (files.size() > 1) {
            throw new InternalGuavaLocationException(
                "Found more than one Guava JAR in the Gradle distribution: ${files*.name}"
            )
        }
        files[0]
    }

    /** Thrown when an internal Guava JAR cannot be located.
     *
     * @since 3.0
     */
    @InheritConstructors
    static class InternalGuavaLocationException extends RuntimeException {
    }

    private static FilenameFilter internalGuavaPattern() {
        Pattern filter
        if (GradleVersion.current() >= GradleVersion.version('5.0')) {
            filter = ~/guava-([\d.]+)-android.jar/
        } else {
            filter = ~/guava-jdk5-([\d.]+).jar/
        }

        new FilenameFilter() {
            @Override
            boolean accept(File dir, String name) {
                name.matches(filter)
            }
        }
    }
}
