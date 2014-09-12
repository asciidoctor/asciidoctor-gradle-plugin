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
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.FileUtils
import org.gradle.util.CollectionUtils

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
 * @author Schalk W. Cronjé
 */
@SuppressWarnings('MethodCount')
class AsciidoctorTask extends DefaultTask {
    private static final boolean IS_WINDOWS = System.getProperty('os.name').contains('Windows')
    private static final String PATH_SEPARATOR=System.getProperty('path.separator')
    private static final String DOUBLE_BACKLASH = '\\\\'
    private static final String BACKLASH = '\\'
    private static final String SAFE_MODE_CLASSNAME = 'org.asciidoctor.SafeMode'

    private static final String DEFAULT_BACKEND = AsciidoctorBackend.HTML5.id
    public static final String ASCIIDOCTOR_FACTORY_CLASSNAME = 'org.asciidoctor.Asciidoctor$Factory'

    private boolean baseDirSetToNull
    private Object outDir
    private Object srcDir
    private final List<Object> gemPaths = []
    private Set<String> backends
    private Set<String> requires
    private Map opts = [:]
    private Map attrs = [:]
    private PatternSet sourceDocumentPattern
    private CopySpec resourceCopy
    private static ClassLoader cl

    /** If set to true each backend will be output to a separate subfolder below {@code outputDir}
     * @since 1.5.1
     */
    @Input
    boolean separateOutputDirs = true

    @Optional @InputDirectory
    File baseDir

    /** Logs documents as they are converted
     *
     */
    @Optional
    boolean logDocuments = false

    /** Old way to set only one source document
     * @deprecated Use {@code sources} instead
     */
    @Optional @InputFile File sourceDocumentName

    /** Old way to define the backend to use
     * @deprecated USe {@code backends} instead
     */
    @Optional @Input String backend


    AsciidoctorProxy asciidoctor
    ResourceCopyProxy resourceCopyProxy
    Configuration classpath

    AsciidoctorTask() {
        srcDir = project.file('src/docs/asciidoc')
        outputDir = new File(project.buildDir, 'asciidoc')
    }

    /** Returns all of the Asciidoctor options
     *
     */
    @Optional @Input
    Map getOptions() {this.opts}

    /** Apply a new set of Asciidoctor options, clearing any options previously set.
     *
     * For backwards compatibility it is still possible to replace attributes via this call. However the
     * use of {@link #setAttributes(java.util.Map)} and {@link #attributes(java.util.Map)} are the now
     * correct way of working with attributes
     *
     * @param m Map with new options
     */
    @SuppressWarnings('DuplicateStringLiteral')
    void setOptions( Map m ) {
        if(m.containsKey('attributes')) {
            logger.warn 'Attributes found in options. Existing attributes qill be replaced due to assignment. ' +
                    'Please use \'attributes\' method instead as current behaviour will be removed in future'
            attrs = coerceLegacyAttributeFormats(m.attributes)
            m.remove('attributes')
        }
        this.opts=m
    }

    /** Appends a new set of Asciidoctor options, clearing any options previously set.
     *
     * For backwards compatibility it is still possible to append attributes via this call. However the
     * use of {@link #setAttributes(java.util.Map)} and {@link #attributes(java.util.Map)} are the now
     * correct way of working with attributes
     *
     * @param m Map with new options
     * @since 1.5.1
     */
    @SuppressWarnings('DuplicateStringLiteral')
    void options( Map m ) {
        if(m.containsKey('attributes')) {
            logger.warn 'Attributes found in options. These will be added to existing attributes. ' +
                    'Please use \'attributes\' method instead as current behaviour will be removed in future'
            attributes coerceLegacyAttributeFormats(m.attributes)
            m.remove('attributes')
        }
        this.opts+=m
    }

    /** Returns the current set of Asciidoctor attributes
     *
     * @since 1.5.1
     */
    @Optional @Input
    Map getAttributes() {this.attrs}

    /** Applies a new set of Asciidoctor attributes, clearing any previsouly set
     *
     * @param m New map of attributes
     * @since 1.5.1
     */
    void setAttributes(Map m) {this.attrs=m}

    /** Appends a set of Asciidoctor attributes, clearing any previsouly set
     *
     * @param m Map of additional attributes
     * @since 1.5.1
     */
    void attributes(Map m) {this.attrs+=m}

    /** Returns the set of  Ruby modules to be included.
     *
     * @since 1.5.0
     */
    @Optional @Input
    Set<String> getRequires() {this.requires}

    /** Applies a new set of  Ruby modules to be included, clearing any previous set.
     *
     * @param b One or more ruby modules to be included
     * @since 1.5.0
     */
    void setRequires(Object... b) {
        this.requires?.clear()
        requires(b)
    }

    /** Appends new set of  Ruby modules to be included.
     *
     * @param b One or more ruby modules to be included
     * @since 1.5.1
     */
    @SuppressWarnings('ConfusingMethodName')
    void requires(Object... b) {
        if(this.requires==null) {this.requires=[]}
        this.requires.addAll(CollectionUtils.stringize(b as List))
    }

    /** Returns the current set of Asciidoctor backends that will be used for document generation
     *
     * @since 0.7.1
     * @deprecated
     */
    @Optional @Input
    Set<String> getBackends() {this.backends}

    void setBackend(final String b) {
        deprecated 'setBackend','backends','Using `backend` and `backends` together will result in `backend` being ignored.'
        backend = b
    }

    /** Applies a new set of Asciidoctor backends that will be used for document generation clearing any
     * previous backends
     *
     * @param b List of backends. Each item must be convertible to string.
     *
     * @since 0.7.1
     */
    void setBackends(Object... b) {
        this.backends?.clear()
        backends(b)
    }

    /** Appends additional Asciidoctor backends that will be used for document generation.
     *
     * @param b List of backends. Each item must be convertible to string.
     *
     * @since 1.5.1
     */
    @SuppressWarnings('ConfusingMethodName')
    void backends(Object... b) {
        if(this.backends==null) {this.backends=[]}
        this.backends.addAll(CollectionUtils.stringize(b as List))
    }

    /** Sets a new gemPath to be used
     *
     * @param f A path object can be be converted with {@code project.file}.
     * @since 1.5.1
     */
    @SuppressWarnings('ConfusingMethodName')
    void gemPath(Object... f) {
        this.gemPaths.addAll(f as List)
    }

    /** Sets a new list of GEM paths to be used.
     *
     * @param f A {@code File} object pointing to list of installed GEMs
     * @since 1.5.0
     */
    void setGemPath(Object... f) {
        this.gemPaths.clear()
        this.gemPaths.addAll(f as List)
    }

    /** Assigns a single string to a GEM path, scanning it for concatenated GEM Paths, separated by the platform
     * separator. This utility is only for backwards compatibility
     *
     * @param s
     */
    void setGemPath(final Object path) {
        this.gemPaths.clear()
        if(path instanceof CharSequence) {
            this.gemPaths.addAll(setGemPath(path.split(PATH_SEPARATOR)))
        } else {
            this.gemPaths.addAll(path)
        }
    }

    /** Returns the list of paths to be used for {@code GEM_HOME}
     *
     * @since 1.5.0
     */
    @Optional
    @Input
    FileCollection getGemPath() {
        project.files(this.gemPaths)
    }

    /** Returns the list of paths to be used for GEM installations in a format that is suitable for assignment to {@code GEM_HOME}
     *
     * Calling this will cause gemPath to be resolved immediately.
     * @since 1.5.1
     */
    @Optional
    @InputDirectory
    String asGemPath() {
        gemPath.files*.toString().join(PATH_SEPARATOR)
    }

    /** Sets the new Asciidoctor parent source directory.
     *
     * @param f An object convertible via {@code project.file}
     * @since 1.5.1
     */
    void sourceDir(Object f) {
        this.srcDir=f
    }

    /** Sets the new Asciidoctor parent source directory.
     *
     * @param f A {@code File} object pointing to the parent source directory
     */
    void setSourceDir(File f) {
        this.srcDir=f
    }

    /** Returns the parent directory for Asciidoctor source. Default is {@code src/asciidoc}.
     */
    @Optional
    @InputDirectory
    File getSourceDir() {
        project.file(srcDir)
    }

    /** Sets the new Asciidoctor parent output directory.
     *
     * @param f An object convertible via {@code project.file}
     * @since 1.5.1
     */
    void outputDir(Object f) {
        this.outDir=f
    }

    /** Sets the new Asciidoctor parent output directory.
     *
     * @param f A {@code File} object pointing to the parent output directory
     */
    void setOutputDir(File f) {
        this.outDir=f
    }

    /** Returns the current toplevel output directory
     *
     */
    @OutputDirectory
    File getOutputDir() {project.file(this.outDir)}

    /** Returns the collection of source documents
     *
     * If sourceDocumentNames was not set or is empty, it will return all asciidoc files
     * in {@code sourceDir}. Otherwise only the files provided earlier to sourceDocumentNames
     * are returned if they are found below {@code sourceDir}
     * @since 1.5.0
     * @deprecated
     */
//    @Optional
//    @InputFiles
    FileCollection getSourceDocumentNames() {
        deprecated 'getSourceDocumentNames', 'getSourceFileTree'
        sourceFileTree
    }

    /** Sets a single file to the main source file
     *
     * @param f
     * @deprecated
     */
    void setSourceDocumentName(File f) {
        deprecated 'setSourceDocumentName', 'setIncludes', 'File will be converted to a pattern.'
        sources {
            setIncludes([AsciidoctorUtils.getRelativePath(f.absoluteFile,sourceDir.absoluteFile)])
        }
    }

    /** Replaces the current set of source documents with a new set
     *
     * @parm src List of source documents, which must be convertible using {@code project.files}
     * @since 1.5.0
     * @deprecated
     */
    @SuppressWarnings('DuplicateStringLiteral')
    void setSourceDocumentNames(Object... src) {
        deprecated 'setSourceDocumentNames', 'setIncludes', 'Files are converted to patterns. Some might not convert correctly' +
                'FileCollections will not convert'
        sources {
            setIncludes(CollectionUtils.stringize(src as List))
        }
    }

    // TODO: Remove before 1.5.1 release
    /** Appends more source documents
     *
     * @parm src List of source documents, which must be convertible using {@code project.files}
     * @since 1.5.1
     * @deprecated
     */
    @SuppressWarnings('ConfusingMethodName')
    @SuppressWarnings('DuplicateStringLiteral')
    void sourceDocumentNames(Object... src) {
        deprecated 'sourceDocumentNames', 'includes', 'Files are converted to patterns. Some might not convert correctly' +
                'FileCollections will not convert'
        sources {
            include CollectionUtils.stringize(src as List)
        }
    }


    void setBaseDir(File baseDir) {
        this.baseDir = baseDir
        baseDirSetToNull = baseDir == null
    }

    /** Returns a list of all output directories.
     * @since 1.5.1
     */
    @OutputDirectories
    getOutputDirectories() {
        if (separateOutputDirs) {
            backends.collect { new File(outputDir, it) } as Set
        } else {
            [outputDir] as Set
        }
    }

    /** Returns a FileTree containing all of the source documents
     *
     * @return If {@code sources} was never called then all asciidoc source files below {@code sourceDir} will
     * be included
     * @since 1.5.1
     */
    @InputFiles
    @SkipWhenEmpty
    FileTree getSourceFileTree() {
        project.fileTree(sourceDir).
                matching (this.sourceDocumentPattern ?: this.defaultSourceDocumentPattern)
    }

    /** Add patterns for source files or source files via a closure
     *
     * @param cfg
     * @since 1.5.1
     */
    void sources(Closure cfg) {
        if(sourceDocumentPattern==null)  {
            sourceDocumentPattern=new PatternSet()
        }
        def configuration = cfg.clone()
        configuration.delegate=sourceDocumentPattern
        configuration()
    }


    void resources( Closure cfg ) {
        if(this.resourceCopy==null) {
            this.resourceCopy = project.copySpec(cfg)
        } else {
            def configuration = cfg.clone()
            configuration.delegate = this.resourceCopy
            configuration()
        }
    }

    /** The default PatternSet that will be used if {@code sources} was never called
     *
     * By default all *.adoc,*.ad,*.asc,*.asciidoc is included. Files beginning with underscore are excluded
     *
     * @since 1.5.1
     */
    PatternSet getDefaultSourceDocumentPattern() {
        PatternSet ps = new PatternSet()
        ps.include '**/*.adoc'
        ps.include '**/*.ad'
        ps.include '**/*.asc'
        ps.include '**/*.asciidoc'
        ps.exclude '**/_*'
    }

    /** The default CopySpec that will be used if {@code resources} was never called
     *
     * By default anything below {@code $sourceDir/images} will be included.
     *
     * @since 1.5.1
     */
    CopySpec getDefaultResourceCopySpec() {
        project.copySpec {
            from (sourceDir) {
                include 'images/**'
            }
        }
    }

    // --- Experimental functions start here
    /** Gets the CopySpec for additional resources
     *
     */
    CopySpec getResourceCopySpec() {
        this.resourceCopy ?: defaultResourceCopySpec
    }
    // --- Stops here

    @TaskAction
    void processAsciidocSources() {

        if(sourceFileTree.files.size()==0) {
            logger.lifecycle 'Asciidoc source file tree is empty. Nothing will be processed.'
            return
        }

        if(classpath == null) {
            classpath = project.configurations.getByName(AsciidoctorPlugin.ASCIIDOCTOR)
        }
        setupClassLoader()

        if (!asciidoctor) {
            instantiateAsciidoctor()
        }

        if(resourceCopyProxy==null) {
            resourceCopyProxy = new ResourceCopyProxyImpl(project)
        }

        if (requires) {
            for (require in requires) {
                // FIXME AsciidoctorJ should provide a public API for requiring paths in the Ruby runtime
                asciidoctor.delegate.rubyRuntime.evalScriptlet(
                        'require \'' + require.replaceAll('[^A-Za-z0-9/\\\\.\\-_]', '') + '\'')
            }
        }

        for (activeBackend in activeBackends()) {
            if (!AsciidoctorBackend.isBuiltIn(activeBackend)) {
                logger.lifecycle("Passing through unknown backend: $activeBackend")
            }
            processDocumentsAndResources(activeBackend)
        }

    }


    @groovy.transform.PackageScope
    File outputDirFor(final File source, final String basePath, final File outputDir, final String backend) {
        String filePath = source.directory ? source.absolutePath : source.parentFile.absolutePath
        String relativeFilePath = normalizePath(filePath) - normalizePath(basePath)
        File baseOutputDir = outputBackendDir(outputDir,backend)
        File destinationParentDir = new File(baseOutputDir,relativeFilePath)
        if (!destinationParentDir.exists()) {destinationParentDir.mkdirs()}
        destinationParentDir
    }

    private File outputBackendDir(final File outputDir, final String backend) {
        separateOutputDirs ? new File(outputDir,FileUtils.toSafeFileName(backend)) : outputDir
    }

    private static String normalizePath(String path) {
        if (IS_WINDOWS) {
            path = path.replace(DOUBLE_BACKLASH, BACKLASH)
            path = path.replace(BACKLASH, DOUBLE_BACKLASH)
        }
        path
    }


    @SuppressWarnings('CatchException')
    private void instantiateAsciidoctor() {
        if (gemPaths.size()) {
            asciidoctor = new AsciidoctorProxyImpl(delegate: loadClass(ASCIIDOCTOR_FACTORY_CLASSNAME).create(asGemPath()))
        } else {
            try {
                asciidoctor = new AsciidoctorProxyImpl(delegate: loadClass(ASCIIDOCTOR_FACTORY_CLASSNAME).create(null as String))
            } catch (Exception e) {
                // Asciidoctor < 1.5.1 can't handle a null gemPath, so fallback to default create() method
                asciidoctor = new AsciidoctorProxyImpl(delegate: loadClass(ASCIIDOCTOR_FACTORY_CLASSNAME).create())
            }
        }
    }

    private Set<String> activeBackends() {
        if (this.backends) {
            return this.backends
        } else if (backend) {
            return [backend]
        }
        [DEFAULT_BACKEND]
    }

    @SuppressWarnings('CatchException')
    @SuppressWarnings('DuplicateStringLiteral')
    private void processDocumentsAndResources(final String backend) {

        try {
            sourceFileTree.files.each { File file ->
                if(file.name.startsWith('_')) {
                    throw new InvalidUserDataException('Source documents may not start with an underscore')
                }
                File destinationParentDir = owner.outputDirFor(file, sourceDir.absolutePath, outputDir,backend)
                processSingleFile(backend, destinationParentDir, file)
            }

            resourceCopyProxy.copy(outputBackendDir(outputDir, backend),resourceCopySpec)
            // TODO: Might have to copy specific per backend

        } catch (Exception e) {
            throw new GradleException('Error running Asciidoctor', e)
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
                        attributes : attrs,
                        baseDir: !baseDir && !baseDirSetToNull ? file.parentFile : baseDir,
                        projectDir: project.projectDir,
                        rootDir: project.rootDir,
                        outputDir: destinationParentDir,
                        backend: backend ]))
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
        processMapAttributes(attributes, params.attributes)

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

    @SuppressWarnings('DuplicateStringLiteral')
    @SuppressWarnings('DuplicateNumberLiteral')
    private static Map coerceLegacyAttributeFormats( Object attributes ) {
        Map transformedMap=[:]
        switch(attributes) {
            case Map:
                transformedMap = attributes
                break
            case CharSequence:
                attributes.replaceAll('([^\\\\]) ', '$1\0').replaceAll('\\\\ ', ' ').split('\0').collect {
                    def split = it.split('=')
                    if(split.size()<2) {
                        throw new InvalidUserDataException("Unsupported format for attributes: ${attributes}")
                    }
                    transformedMap[split[0]] = split.drop(1).join('=')
                }
                break
            case Collection:
                processCollectionAttributes(transformedMap, attributes)
                break
            default:
                if(attributes.class.isArray()) {
                    processCollectionAttributes(transformedMap, attributes)
                } else {
                    throw new InvalidUserDataException("Unsupported type for attributes: ${attributes.class}")
                }
        }

        transformedMap
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

    private void deprecated(final String method,final String alternative,final String msg='') {
        logger.lifecycle "Asciidoctor: ${method} is deprecated and will be removed in a future version." +
                "Use ${alternative} instead. ${msg}"
    }


}
