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
import org.gradle.api.tasks.*

/**
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Tom Bujok
 * @author Lukasz Pielak
 * @author Dmitri Vyazelenko
 */
class AsciidoctorTask extends DefaultTask {
    private static final boolean IS_WINDOWS = System.getProperty('os.name').contains('Windows')
    private static final String DOUBLE_BACKLASH = '\\\\'
    private static final String BACKLASH = '\\'
    private static final ASCIIDOC_FILE_EXTENSION_PATTERN = ~/.*\.a((sc(iidoc)?)|d(oc)?)$/

    @Optional @InputFile File sourceDocumentName
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

    @TaskAction
    void gititdone() {
        validateInputs()

        outputDir.mkdirs()

        if (sourceDocumentName) {
            processSingleDocument()
        } else {
            processAllDocuments()
        }
    }

    /**
     * Validates input values. If an input value is not valid an exception is thrown.
     */
    private void validateInputs() {
        if (!AsciidoctorBackend.isSupported(backend)) {
            throw new InvalidUserDataException("Unsupported backend: $backend")
        }
    }

    @SuppressWarnings('CatchException')
    private void processSingleDocument() {
        try {
            if (sourceDocumentName.name =~ ASCIIDOC_FILE_EXTENSION_PATTERN) {
                asciidoctor.renderFile(sourceDocumentName, mergedOptions(options, outputDir, backend))
            }
        } catch (Exception e) {
            throw new GradleException('Error running Asciidoctor on single source', e)
        }
    }

    @SuppressWarnings('CatchException')
    private void processAllDocuments() {
        try {
            sourceDir.eachFileRecurse { File file ->
                if (file.directory) {
                    outputDirFor(file, sourceDir.absolutePath, outputDir)
                } else {
                    File destinationParentDir = outputDirFor(file, sourceDir.absolutePath, outputDir)
                    if (file.name =~ ASCIIDOC_FILE_EXTENSION_PATTERN) {
                        asciidoctor.renderFile(file, mergedOptions(options, outputDir, backend))
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

    private static File outputDirFor(File source, String basePath, File outputDir) {
        String filePath = source.directory ? source.absolutePath : source.parentFile.absolutePath
        String relativeFilePath = normalizePath(filePath) - normalizePath(basePath)
        File destinationParentDir = new File("${outputDir}/${relativeFilePath}")
        if (!destinationParentDir.exists()) destinationParentDir.mkdirs()
        destinationParentDir
    }

    private static String normalizePath(String path) {
        if (IS_WINDOWS) {
            path = path.replace(DOUBLE_BACKLASH, BACKLASH)
            path = path.replace(BACKLASH, DOUBLE_BACKLASH)
        }
        path
    }

    private static Map<String, Object> mergedOptions(Map options, File outputDir, String backend) {
        Map<String, Object> mergedOptions = [:]
        mergedOptions.putAll(options)
        mergedOptions.in_place = false
        mergedOptions.safe = 0i
        mergedOptions.to_dir = outputDir.absolutePath
        Map attributes = mergedOptions.get('attributes', [:])
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
        mergedOptions
    }
}
