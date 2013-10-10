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
 * @author Dan Allen
 */
class AsciidoctorTask extends DefaultTask {
    private static final boolean IS_WINDOWS = System.getProperty('os.name').contains('Windows')
    private static final String DOUBLE_BACKLASH = '\\\\'
    private static final String BACKLASH = '\\'
    private static final ASCIIDOC_FILE_EXTENSION_PATTERN = ~/.*\.a((sc(iidoc)?)|d(oc)?)$/
    private static final DOCINFO_FILE_PATTERN = ~/^(.+\-)?docinfo(-footer)?\.[^.]+$/

    @Optional @InputFile File sourceDocumentName
    @Optional @InputDirectory File baseDir
    @InputDirectory File sourceDir
    @OutputDirectory File outputDir
    @Input String backend
    @Input Map options = [:]
    @Optional boolean logDocuments = false

    Asciidoctor asciidoctor

    AsciidoctorTask() {
        sourceDir = project.file('src/asciidoc')
        outputDir = new File(project.buildDir, 'asciidoc')
        backend = AsciidoctorBackend.HTML5.id
        baseDir = project.projectDir
    }

    /**
     * Validates input values. If an input value is not valid an exception is thrown.
     */
    private void validateInputs() {
        if (!AsciidoctorBackend.isBuiltIn(backend)) {
            logger.lifecycle("Passing through unknown backend: $backend")
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

    @TaskAction
    void gititdone() {
        validateInputs()

        outputDir.mkdirs()

        asciidoctor = asciidoctor ?: Asciidoctor.Factory.create()

        if (sourceDocumentName) {
            processSingleDocument()
        } else {
            processAllDocuments()
        }
    }

    @SuppressWarnings('CatchException')
    private void processSingleDocument() {
        try {
            if (sourceDocumentName.name =~ ASCIIDOC_FILE_EXTENSION_PATTERN) {
                if (logDocuments) {
                    logger.lifecycle("Rendering $sourceDocumentName")
                }
                asciidoctor.renderFile(sourceDocumentName, mergedOptions(
                    options: options,
                    baseDir: baseDir,
                    projectDir: project.projectDir,
                    rootDir: project.rootDir,
                    outputDir: outputDir,
                    backend: backend))
            }
            sourceDir.eachFileRecurse { File file ->
                if (file.file && !(file.name =~ ASCIIDOC_FILE_EXTENSION_PATTERN)) {
                    File destinationParentDir = outputDirFor(file, sourceDir.absolutePath, outputDir)
                    File target = new File("${destinationParentDir}/${file.name}")
                    target.withOutputStream { it << file.newInputStream() }
                }
            }
        } catch (Exception e) {
            throw new GradleException('Error running Asciidoctor on single source', e)
        }
    }

    @SuppressWarnings('CatchException')
    private void processAllDocuments() {
        try {
            sourceDir.eachFileRecurse { File file ->
                if (file.file && !file.name.startsWith('_')) {
                    File destinationParentDir = outputDirFor(file, sourceDir.absolutePath, outputDir)
                    if (file.name =~ ASCIIDOC_FILE_EXTENSION_PATTERN) {
                        if (logDocuments) {
                            logger.lifecycle("Rendering $file")
                        }
                        asciidoctor.renderFile(file, mergedOptions(
                            options: options,
                            baseDir: baseDir,
                            projectDir: project.projectDir,
                            rootDir: project.rootDir,
                            outputDir: destinationParentDir,
                            backend: backend))
                    } else if (!(file.name =~ DOCINFO_FILE_PATTERN)) {
                        File target = new File("${destinationParentDir}/${file.name}")
                        target.withOutputStream { it << file.newInputStream() }
                    }
                }
            }
        } catch (Exception e) {
            throw new GradleException('Error running Asciidoctor', e)
        }
    }

    private static Map<String, Object> mergedOptions(Map params) {
        Map<String, Object> mergedOptions = [:]
        mergedOptions.putAll(params.options)
        mergedOptions.in_place = false
        mergedOptions.safe = 0i
        mergedOptions.to_dir = params.outputDir.absolutePath
        if (params.baseDir) {
          mergedOptions.base_dir = params.baseDir.absolutePath
        }

        if (mergedOptions.to_file) {
            File toFile = new File(mergedOptions.to_file)
            mergedOptions.to_file = new File(mergedOptions.remove('to_dir'), toFile.name).absolutePath
        }

        Map attributes = [:]
        def rawAttributes = mergedOptions.get('attributes', [:])
        if (rawAttributes instanceof Map) {
            // copy all attributes in order to prevent changes down
            // the Asciidoctor chain that could cause serialization
            // problems with Gradle -> all inputs/outputs get serialized
            // for caching purposes; Ruby objects are non-serializable
            // Issue #14 force GString -> String as jruby will fail
            // to find an exact match when invoking Asciidoctor
            for (entry in rawAttributes) {
                if (entry.value == null || entry.value instanceof Boolean) {
                  attributes[entry.key] = entry.value
                } else {
                  attributes[entry.key] = entry.value.toString()
                }
            }
        } else {
            if (rawAttributes instanceof CharSequence) {
                // replace non-escaped spaces with null character, then replace escaped spaces with space,
                // finally split on the null character
                rawAttributes = rawAttributes.replaceAll('([^\\\\]) ', '$1\0').replaceAll('\\\\ ', ' ').split('\0')
            }

            if (rawAttributes.getClass().isArray() || rawAttributes instanceof Collection) {
                rawAttributes.each {
                    if (it instanceof CharSequence) {
                        def (k, v) = it.toString().split('=', 2) as List
                        attributes.put(k, v != null ? v : '')
                    } else {
                        // QUESTION should we just coerce it to a String?
                        throw new InvalidUserDataException("Unsupported type for attribute ${it}: ${it.class}")
                    }
                }
            } else {
                // QUESTION should we just coerce it to a String?
                throw new InvalidUserDataException("Unsupported type for attributes: ${rawAttributes.class}")
            }
        }

        attributes.backend = params.backend
        attributes.projectdir = params.projectDir.absolutePath
        attributes.rootdir = params.rootDir.absolutePath
        mergedOptions.attributes = attributes

        // Issue #14 force GString -> String as jruby will fail
        // to find an exact match when invoking Asciidoctor
        for (entry in mergedOptions) {
            if (entry.value instanceof CharSequence) {
                mergedOptions[entry.key] = entry.value.toString()
            }
        }

        mergedOptions
    }
}
