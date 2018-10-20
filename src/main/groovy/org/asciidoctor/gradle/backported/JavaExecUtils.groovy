/*
 * Copyright 2013-2018 the original author or authors.
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
package org.asciidoctor.gradle.backported

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection

import static org.asciidoctor.gradle.AsciidoctorUtils.getClassLocation
import static org.ysb33r.grolifant.api.FileUtils.toSafeFileName

/** Utilities for dealing with Asciidoctor in an external JavaExec process.
 *
 * @since 1.5.9 (Backported from 2.0)
 * @author Schalk W. Cronjé
 */
@CompileStatic
class JavaExecUtils {

    /** Get the classpath that needs to be passed to the external Java process.
     *
     * @param project Current Gradle project
     * @param asciidoctorClasspath External asciidoctor dependencies
     * @return A computed classpath that can be given to an external Java process.
     */
    static FileCollection getJavaExecClasspath(final Project project, final FileCollection asciidoctorClasspath) {
        File entryPoint = getClassLocation(AsciidoctorJavaExec)
        File groovyJar = getClassLocation(GroovyObject)
        project.files(entryPoint, groovyJar, asciidoctorClasspath, org.asciidoctor.gradle.AsciidoctorUtils.contextClasspath)
    }

    /** The file to which execution configuration data can be serialised to.
     *
     * @param task Task for which execution data will be serialised.
     * @return File that will (eventually) contain the execution data.
     */
    static File getExecConfigurationDataFile(final Task task) {
        task.project.file("${task.project.buildDir}/tmp/${toSafeFileName(task.name)}.javaexec-data")
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
}
