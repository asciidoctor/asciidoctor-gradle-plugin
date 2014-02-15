/*
 * Copyright 2013-2014 the original author or authors.
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
package org.asciidoctor.gradle

import org.asciidoctor.Asciidoctor
import org.asciidoctor.SafeMode
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.*
import org.gradle.util.ConfigureUtil

/**
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Tom Bujok
 * @author Lukasz Pielak
 * @author Dmitri Vyazelenko
 * @author Dan Allen
 * @author Rob Winch
 * @author Stefan Schlott
 */
class AsciidoctorTask extends DefaultTask {
    private static final boolean IS_WINDOWS = System.getProperty('os.name').contains('Windows')
    private static final String DOUBLE_BACKLASH = '\\\\'
    private static final String BACKLASH = '\\'
    private static final ASCIIDOC_FILE_EXTENSION_PATTERN = ~/.*\.a((sc(iidoc)?)|d(oc)?)$/
    private static final DOCINFO_FILE_PATTERN = ~/(.+\-)?docinfo(-footer)?\.[^.]+/

    @Optional @InputFile File sourceDocumentName
    @Optional @InputDirectory File baseDir
    @InputDirectory File sourceDir
    @OutputDirectory File outputDir
    @Input Set<String> backends
    @Input Map options = [:]
    @Optional boolean logDocuments = false
    FopubOptions fopubOptions = new FopubOptions()

    Asciidoctor asciidoctor
    FopubFacade fopub = new FopubFacade()

    AsciidoctorTask() {
        sourceDir = project.file('src/asciidoc')
        outputDir = new File(project.buildDir, 'asciidoc')
        setBackend(AsciidoctorBackend.HTML5.id)
        baseDir = project.projectDir
    }

    void setBackend(String backend) {
        this.backends = [backend]
    }

    FopubOptions fopub(Closure closure) {
        fopubOptions = ConfigureUtil.configure(closure, fopubOptions);
        return fopubOptions
    }

    /**
     * Validates input values. If an input value is not valid an exception is thrown.
     */
    private void validateInputs() {
        for(backend in backends) {
            if (!AsciidoctorBackend.isBuiltIn(backend)) {
                logger.lifecycle("Passing through unknown backend: $backend")
            }
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
        for(backend in backends) {
            processDocumentsAndResources(backend)
        }
    }

    @SuppressWarnings('CatchException')
    private void processDocumentsAndResources(String backend) {
        boolean isFoPub = backend == AsciidoctorBackend.FOPUB.id
        String asciidoctorBackend = isFoPub ? AsciidoctorBackend.DOCBOOK.id : backend

        try {
            def fileFilter = { File file ->
                // skip files & directories that begin with an underscore and docinfo files
                !file.name.startsWith('_') && (file.directory || !(file.name ==~ DOCINFO_FILE_PATTERN))
            }
            eachFileRecurse(sourceDir, fileFilter) { File file ->
                File destinationParentDir = outputDirFor(file, sourceDir.absolutePath, outputDir)
                if (file.name =~ ASCIIDOC_FILE_EXTENSION_PATTERN) {
                    if (!sourceDocumentName || file.name == sourceDocumentName.name) {
                        if (logDocuments) {
                            logger.lifecycle("Rendering $file")
                        }
                        asciidoctor.renderFile(file, mergedOptions(
                            options: options,
                            baseDir: baseDir,
                            projectDir: project.projectDir,
                            rootDir: project.rootDir,
                            outputDir: destinationParentDir,
                            backend: asciidoctorBackend))

                        if (isFoPub) {
                            File workingDir = new File("${outputDir}/$backend/work")
                            fopub.renderPdf(file, workingDir, destinationParentDir, fopubOptions)
                        }
                    }
                } else {
                    File target = new File(destinationParentDir, file.name)
                    target.withOutputStream { it << file.newInputStream() }
                }
            }
        } catch (Exception e) {
            throw new GradleException('Error running Asciidoctor', e)
        }
    }

    private static void eachFileRecurse(File dir, Closure fileFilter, Closure fileProcessor) {
        dir.eachFile { File file ->
            if (fileFilter(file)) {
                if (file.directory) {
                    eachFileRecurse(file, fileFilter, fileProcessor)
                } else {
                    fileProcessor(file)
                }
            }
        }
    }

    private static Map<String, Object> mergedOptions(Map params) {
        Map<String, Object> mergedOptions = [:]
        mergedOptions.putAll(params.options)
        mergedOptions.backend = params.backend
        mergedOptions.in_place = false
        mergedOptions.safe = resolveSafeModeLevel(mergedOptions.safe, 0i)
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

    private static int resolveSafeModeLevel(Object safe, int defaultLevel) {
        if (safe == null) {
            defaultLevel
        } else if (safe instanceof SafeMode) {
            safe.level
        } else if (safe instanceof CharSequence) {
            try {
                Enum.valueOf(SafeMode, safe.toString().toUpperCase()).level
            } catch (IllegalArgumentException e) {
                defaultLevel
            }
        } else {
            safe.intValue()
        }
    }
}
