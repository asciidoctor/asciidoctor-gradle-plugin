/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidoctor.gradle

import org.asciidoctor.Asciidoctor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Tom Bujok
 * @author Lukasz Pielak
 * @author Dmitri Vyazelenko
 */
class AsciidoctorTask extends DefaultTask {
    private static final boolean isWindows = System.getProperty('os.name').contains('Windows')

    @InputDirectory File sourceDir
    @OutputDirectory File outputDir
    @Input String backend
    @Input Map options = [:]

    Asciidoctor asciidoctor

    AsciidoctorTask() {
        sourceDir = project.file('src/asciidoc')
        outputDir = new File(project.buildDir, 'asciidoc')
        backend = AsciidoctorBackend.HTML5.id
        asciidoctor = Asciidoctor.Factory.create()
    }

    /**
     * Validates input values. If an input value is not valid an exception is thrown.
     */
    private void validateInputs() {
        if (!AsciidoctorBackend.isSupported(backend)) {
            throw new InvalidUserDataException("Unsupported backend: $backend")
        }
    }

    private static File outputDirFor(File source, String basePath, File outputDir) {
        String filePath = source.directory ? source.absolutePath : source.parentFile.absolutePath
        String relativeFilePath = normalizePath(filePath) - normalizePath(basePath)
        File destinationParentDir = new File("${outputDir}/${relativeFilePath}")
        if (!destinationParentDir.exists()) destinationParentDir.mkdirs()
        destinationParentDir
    }

    private static String normalizePath (String path) {
        if (isWindows) {
            path = path.replace('\\\\', '\\')
            path = path.replace('\\', '\\\\')
        }
        path
    }

    @TaskAction
    void gititdone() {
        validateInputs()

        outputDir.mkdirs()

        try {
            sourceDir.eachFileRecurse { File file ->
                if (file.directory) {
                    outputDirFor(file, sourceDir.absolutePath, outputDir)
                } else {
                    File destinationParentDir = outputDirFor(file, sourceDir.absolutePath, outputDir)
                    if (file.name =~ /.*\.a((sc(iidoc)?)|d(oc)?)$/) {
                        Map mergedOptions = [:]
                        mergedOptions.putAll(options)
                        mergedOptions.in_place = false
                        mergedOptions.safe = 0i
                        mergedOptions.to_dir = outputDir.absolutePath
                        Map attributes = mergedOptions.get("attributes", [:])
                        attributes.backend = backend

                        // Issue #14 force GString -> String as jruby will fail
                        // to find an exact match when invoking Asciidoctor
                        for (entry in mergedOptions) {
                            if (entry.value instanceof CharSequence) {
                                mergedOptions[entry.key] = entry.value.toString()
                            }
                        }
                        for (entry in attributes) {
                            if (entry.value instanceof CharSequence) {
                                attributes[entry.key] = entry.value.toString()
                            }
                        }

                        asciidoctor.renderFile(file, mergedOptions)
                    } else {
                        File target = new File("${destinationParentDir}/${file.name}")
                        target.withOutputStream { it << file.newInputStream() }
                    }
                }
            }
        } catch (Exception e) {
            throw new GradleException('Error running Asciidoctor', e)
        }
    }
}