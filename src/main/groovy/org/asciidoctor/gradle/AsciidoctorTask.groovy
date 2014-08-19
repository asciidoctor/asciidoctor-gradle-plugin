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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Tom Bujok
 * @author Lukasz Pielak
 * @author Dmitri Vyazelenko
 * @author Dan Allen
 * @author Rob Winch
 * @author Stefan Schlott
 * @author Stephan Classen
 * @author Marcus Fihlon
 */
class AsciidoctorTask extends DefaultTask {
    private static final boolean IS_WINDOWS = System.getProperty('os.name').contains('Windows')
    private static final String DOUBLE_BACKLASH = '\\\\'
    private static final String BACKLASH = '\\'
    private static final ASCIIDOC_FILE_EXTENSION_PATTERN = ~/.*\.a((sc(iidoc)?)|d(oc)?)$/
    private static final DOCINFO_FILE_PATTERN = ~/(.+\-)?docinfo(-footer)?\.[^.]+/
    private static final String SAFE_MODE_CLASSNAME = 'org.asciidoctor.SafeMode'

    private static final String DEFAULT_BACKEND = AsciidoctorBackend.HTML5.id
    private static final Closure<Boolean> EXCLUDE_DOCINFO_AND_FILES_STARTING_WITH_UNDERSCORE = { File file ->
        // skip files & directories that begin with an underscore and docinfo files
        !file.name.startsWith('_') && (file.directory || !(file.name ==~ DOCINFO_FILE_PATTERN))
    }
    public static final String ASCIIDOCTOR_FACTORY_CLASSNAME = 'org.asciidoctor.Asciidoctor$Factory'

    @Optional @InputFile File sourceDocumentName
    @Optional @InputFiles FileCollection sourceDocumentNames
    @Optional @InputDirectory File baseDir
    @Optional @Input String gemPath
    @InputDirectory File sourceDir
    @OutputDirectory File outputDir
    @Optional @Input String backend
    @Optional @Input Set<String> backends
    @Input Map options = [:]
    @Optional @Input Set<String> requires
    @Optional boolean logDocuments = false
    private boolean baseDirSetToNull

    AsciidoctorProxy asciidoctor
    Configuration classpath
    private static ClassLoader cl

    AsciidoctorTask() {
        sourceDir = project.file('src/asciidoc')
        outputDir = new File(project.buildDir, 'asciidoc')
    }

    void setBaseDir(File baseDir) {
        this.baseDir = baseDir
        baseDirSetToNull = baseDir == null
    }

    /**
     * Validates input values. If an input value is not valid an exception is thrown.
     */
    private void validateInputs() {
        setupClassLoader()
        for(backend in backends) {
            if (!AsciidoctorBackend.isBuiltIn(backend)) {
                logger.lifecycle("Passing through unknown backend: $backend")
            }
        }
        if (backend) {
            logger.warn('The `backend` property is deprecated and may not be supported in future versions. Please use the `backends` property instead.')
            if (backends) {
                logger.error('Both the `backend` and `backends` properties were specified. The `backend` property will be ignored.')
            } else if (!AsciidoctorBackend.isBuiltIn(backend)) {
                logger.lifecycle("Passing through unknown backend: $backend")
            }
        }
        if(sourceDocumentName) {
            logger.warn('The `sourceDocumentName` property is deprecated and may not be supported in future versions. Please use the `sourceDocumentNames` property instead.')
            if(sourceDocumentNames) {
                logger.error('Both the `sourceDocumentName` and `sourceDocumentNames` properties were specified. The `sourceDocumentName` property will be ignored.')
            } else {
                validateSourceDocuments(new SimpleFileCollection(sourceDocumentName))
            }
        }
        if (sourceDocumentNames) {
            validateSourceDocuments(sourceDocumentNames)
        }
    }

    private void validateSourceDocuments(FileCollection srcDocs) {
        srcDocs.files.findAll({it.isAbsolute()}).each {
            logger.warn("Entry '$it' of `sourceDocumentNames` should be specified relative to sourceDir ($sourceDir)")
        }
        Collection<File> allReachableFiles = []
        eachFileRecurse(sourceDir, EXCLUDE_DOCINFO_AND_FILES_STARTING_WITH_UNDERSCORE) { File file ->
            allReachableFiles.add(file)
        }
        srcDocs.files.each { File file ->
            if (! allReachableFiles.find {it.canonicalPath == file.canonicalPath}) {
                throw new GradleException("'$file' is not reachable from sourceDir ($sourceDir). " +
                        'all files given ins sourceDocumentNames must be located in the sourceDir ' +
                        'or a subfolder of sourceDir')
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
    void processAsciidocSources() {
        validateInputs()
        outputDir.mkdirs()

        if (!asciidoctor) {
            if (gemPath) {
                asciidoctor = (loadClass(ASCIIDOCTOR_FACTORY_CLASSNAME).create(gemPath) as AsciidoctorProxy)
            } else {
                try {
                    asciidoctor = (loadClass(ASCIIDOCTOR_FACTORY_CLASSNAME).create(null as String) as AsciidoctorProxy)
                } catch (Exception e) {
                    // Asciidoctor < 1.5.1 can't handle a null gemPath, so fallback to default create() method
                    asciidoctor = (loadClass(ASCIIDOCTOR_FACTORY_CLASSNAME).create() as AsciidoctorProxy)
                }
            }
        }

        if (requires) {
            for (require in requires) {
               // FIXME AsciidoctorJ should provide a public API for requiring paths in the Ruby runtime
               asciidoctor.proxyTarget.rubyRuntime.evalScriptlet(
                   'require \'' + require.replaceAll('[^A-Za-z0-9/\\\\.\\-]', '') + '\'')
            }
        }

        for (activeBackend in activeBackends()) {
            processDocumentsAndResources(activeBackend)
        }
    }

    private Set<String> activeBackends() {
        if (backends) {
            return backends
        } else if (backend) {
            return [backend]
        }
        [DEFAULT_BACKEND]
    }

    @SuppressWarnings('CatchException')
    private void processDocumentsAndResources(String backend) {
        try {
            eachFileRecurse(sourceDir, EXCLUDE_DOCINFO_AND_FILES_STARTING_WITH_UNDERSCORE) { File file ->
                processSourceDir(backend, file)
            }
        } catch (Exception e) {
            throw new GradleException('Error running Asciidoctor', e)
        }
    }

    protected void processSourceDir(String backend, File file) {
        File destinationParentDir = outputDirFor(file, sourceDir.absolutePath, outputDir)
        if (file.name =~ ASCIIDOC_FILE_EXTENSION_PATTERN) {
            if (sourceDocumentNames) {
                // sourceDocumentNames is defined and there's no match we stop
                // iow, we don't process sourceDocumentName if both are defined
                // as sourceDocumentNames takes precedence
                if (sourceDocumentNames.files.find { it.canonicalPath == file.canonicalPath }) {
                    processSingleFile(backend, destinationParentDir, file)
                }
                // check if single file was given
            } else if (!sourceDocumentName || file.canonicalPath == sourceDocumentName.canonicalPath) {
                processSingleFile(backend, destinationParentDir, file)
            }
        } else {
            File target = new File(destinationParentDir, file.name)
            target.withOutputStream { it << file.newInputStream() }
        }
    }

    protected void processSingleFile(String backend, File destinationParentDir, File file) {
        if (logDocuments) {
            logger.lifecycle("Converting $file")
        }
        asciidoctor.renderFile(file, mergedOptions(file,
                [
                        project: project,
                        options: options,
                        baseDir: !baseDir && !baseDirSetToNull ? file.parentFile : baseDir,
                        projectDir: project.projectDir,
                        rootDir: project.rootDir,
                        outputDir: destinationParentDir,
                        backend: backend ]))
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

    @SuppressWarnings('AbcMetric')
    private static Map<String, Object> mergedOptions(File file, Map params) {
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
            processMapAttributes(attributes, rawAttributes)
        } else {
            if (rawAttributes instanceof CharSequence) {
                // replace non-escaped spaces with null character, then replace escaped spaces with space,
                // finally split on the null character
                rawAttributes = rawAttributes.replaceAll('([^\\\\]) ', '$1\0').replaceAll('\\\\ ', ' ').split('\0')
            }

            if (rawAttributes.getClass().isArray() || rawAttributes instanceof Collection) {
                processCollectionAttributes(attributes, rawAttributes)
            } else {
                // QUESTION should we just coerce it to a String?
                throw new InvalidUserDataException("Unsupported type for attributes: ${rawAttributes.class}")
            }
        }

        // Note: Directories passed as relative to work around issue #83
        // Asciidoctor cannot handle absolute paths in Windows properly
        attributes.projectdir = AsciidoctorUtils.getRelativePath(params.projectDir, file.parentFile)
        attributes.rootdir = AsciidoctorUtils.getRelativePath(params.rootDir, file.parentFile)

        // resolve these properties here as we want to catch both Map and String definitions parsed above
        attributes.'project-name' = attributes.'project-name' ?: params.project.name
        attributes.'project-group' = attributes.'project-group' ?: (params.project.group ?: '')
        attributes.'project-version' = attributes.'project-version' ?: (params.project.version ?: '')
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

    protected static void processMapAttributes(Map attributes, Map rawAttributes) {
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
    }

    protected static void processCollectionAttributes(Map attributes, rawAttributes) {
        for(attr in rawAttributes) {
            if (attr instanceof CharSequence) {
                def (k, v) = attr.toString().split('=', 2) as List
                attributes.put(k, v != null ? v : '')
            } else {
                // QUESTION should we just coerce it to a String?
                throw new InvalidUserDataException("Unsupported type for attribute ${attr}: ${attr.getClass()}")
            }
        }
    }

    private static int resolveSafeModeLevel(Object safe, int defaultLevel) {
        if (safe == null) {
            defaultLevel
        } else if (safe.class.name == SAFE_MODE_CLASSNAME) {
            safe.level
        } else if (safe instanceof CharSequence) {
            try {
                Enum.valueOf(loadClass(SAFE_MODE_CLASSNAME), safe.toString().toUpperCase()).level
            } catch (IllegalArgumentException e) {
                defaultLevel
            }
        } else {
            safe.intValue()
        }
    }

    private static Class loadClass(String className) {
        cl.loadClass(className)
    }

    private void setupClassLoader() {
        if (classpath?.files) {
            def urls = classpath.files.collect { it.toURI().toURL() }
            cl = new URLClassLoader(urls as URL[], Thread.currentThread().contextClassLoader)
            Thread.currentThread().contextClassLoader = cl
        } else {
            cl = Thread.currentThread().contextClassLoader
        }
    }
}
