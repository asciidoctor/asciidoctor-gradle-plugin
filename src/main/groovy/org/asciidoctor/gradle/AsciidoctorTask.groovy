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
package org.asciidoctor.gradle

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.extension.spi.ExtensionRegistry
import org.asciidoctor.gradle.backported.AsciidoctorJavaExec
import org.asciidoctor.gradle.backported.ExecutorConfiguration
import org.asciidoctor.gradle.backported.ExecutorConfigurationContainer
import org.asciidoctor.gradle.backported.JavaExecUtils
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.copy.CopySpecInternal
@java.lang.SuppressWarnings('NoWildcardImports')
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.FileUtils
import org.gradle.process.JavaExecSpec
import org.gradle.util.CollectionUtils
import org.gradle.util.GradleVersion

import static org.asciidoctor.gradle.AsciidoctorUtils.getClassLocation
import static org.asciidoctor.gradle.backported.JavaExecUtils.getJavaExecClasspath

/**
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Tom Bujok
 * @author Lukasz Pielak
 * @author Dmitri Vyazelenko
 * @author Benjamin Muschko
 * @author Dan Allen
 * @author Rob Winch
 * @author Stefan Schlott
 * @author Stephan Classen
 * @author Marcus Fihlon
 * @author Schalk W. Cronjé
 * @author Robert Panzer
 */
@SuppressWarnings(['MethodCount', 'Instanceof'])
class AsciidoctorTask extends DefaultTask {
    static final boolean GRADLE_4_OR_BETTER = GradleVersion.current() >= GradleVersion.version('4.0')
    static final boolean IS_WINDOWS = System.getProperty('os.name').contains('Windows')
    static final String DOUBLE_BACKLASH = '\\\\'
    static final String BACKLASH = '\\'
    private static final String PATH_SEPARATOR = System.getProperty('path.separator')
    private static final String SAFE_MODE_CLASSNAME = 'org.asciidoctor.SafeMode'

    private static final String DEFAULT_BACKEND = AsciidoctorBackend.HTML5.id

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

    @Optional
    @InputDirectory
    File baseDir

    /** Logs documents as they are converted
     *
     */
    @Internal
    boolean logDocuments = false

    /** Old way to set only one source document
     * @deprecated Use {@code sources} instead
     */
    @Optional
    @InputFile
    File sourceDocumentName

    /** Old way to define the backend to use
     * @deprecated Use {@code backends} instead
     */
    @Optional
    @Input
    String backend

    /**
     * Stores the extensions defined in the configuration phase
     * to register them in the execution phase.
     */
    @Internal
    List<Object> asciidoctorExtensions = []

    @Optional
    @InputFiles
    Configuration classpath

    AsciidoctorTask() {
        srcDir = project.file('src/docs/asciidoc')
    }

    /** Returns all of the Asciidoctor options
     *
     */
    @Optional
    @Input
    Map getOptions() { this.opts }

    /** Apply a new set of Asciidoctor options, clearing any options previously set.
     *
     * For backwards compatibility it is still possible to replace attributes via this call. However the
     * use of {@link #setAttributes(java.util.Map)} and {@link #attributes(java.util.Map)} are the now
     * correct way of working with attributes
     *
     * @param m Map with new options
     */
    @SuppressWarnings('DuplicateStringLiteral')
    void setOptions(Map m) {
        if (!m) return // null check
        if (m.containsKey('attributes')) {
            logger.warn 'Attributes found in options. Existing attributes will be replaced due to assignment. ' +
                'Please use \'attributes\' method instead as current behaviour will be removed in future'
            attrs = coerceLegacyAttributeFormats(m.attributes)
            m.remove('attributes')
        }
        this.opts = m
    }

    /** Appends a new set of Asciidoctor options.
     *
     * For backwards compatibility it is still possible to append attributes via this call. However the
     * use of {@link #setAttributes(java.util.Map)} and {@link #attributes(java.util.Map)} are the now
     * correct way of working with attributes
     *
     * @param m Map with new options
     * @since 1.5.1
     */
    @SuppressWarnings('DuplicateStringLiteral')
    void options(Map m) {
        if (!m) return // null check
        if (m.containsKey('attributes')) {
            logger.warn 'Attributes found in options. These will be added to existing attributes. ' +
                'Please use \'attributes\' method instead as current behaviour will be removed in future'
            attributes coerceLegacyAttributeFormats(m.attributes)
            m.remove('attributes')
        }
        this.opts += m
    }

    /** Returns the current set of Asciidoctor attributes
     *
     * @since 1.5.1
     */
    @Optional
    @Input
    Map getAttributes() { this.attrs }

    /** Applies a new set of Asciidoctor attributes, clearing any previously set
     *
     * @param m New map of attributes
     * @since 1.5.1
     */
    void setAttributes(Map m) {
        if (m) {
            this.attrs = m
        } else {
            this.attrs.clear()
        }
    }

    /** Appends a set of Asciidoctor attributes.
     *
     * @param o a Map, List or a literal (String) definition
     * @since 1.5.1
     */
    void attributes(Object... o) {
        if (!o) {
            this.attrs.clear()
            return
        }
        for (input in o) {
            this.attrs += coerceLegacyAttributeFormats(input)
        }
    }

    /** Returns the set of  Ruby modules to be included.
     *
     * @since 1.5.0
     */
    @Optional
    @Input
    Set<String> getRequires() { this.requires }

    /** Applies a new set of Ruby modules to be included, clearing any previous set.
     *
     * @param b One or more ruby modules to be included
     * @since 1.5.0
     */
    void setRequires(Object... b) {
        this.requires?.clear()
        requires(b)
    }

    /** Appends new set of Ruby modules to be included.
     *
     * @param b One or more ruby modules to be included
     * @since 1.5.1
     */
    @SuppressWarnings('ConfusingMethodName')
    void requires(Object... b) {
        if (this.requires == null) {
            this.requires = []
        }
        if (!b) return // null check
        this.requires.addAll(CollectionUtils.stringize(b as List))
    }

    /** Returns the current set of Asciidoctor backends that will be used for document generation
     *
     * @since 0.7.1
     * @deprecated
     */
    @Optional
    @Input
    Set<String> getBackends() { this.backends }

    void setBackend(String b) {
        if (!b) return // null check
        deprecated 'setBackend', 'backends', 'Using `backend` and `backends` together will result in `backend` being ignored.'
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
        if (this.backends == null) {
            this.backends = []
        }
        if (!b) return // null check
        this.backends.addAll(CollectionUtils.stringize(b as List))
    }

    /** Defines extensions. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     */
    void extensions(Object... exts) {
        if (!exts) return // null check
        asciidoctorExtensions.addAll(exts as List)
    }

    /** Sets a new gemPath to be used
     *
     * @param f A path object can be be converted with {@code project.file}.
     * @since 1.5.1
     */
    @SuppressWarnings('ConfusingMethodName')
    void gemPath(Object... f) {
        if (!f) return // null check
        this.gemPaths.addAll(f as List)
    }

    /** Sets a new list of GEM paths to be used.
     *
     * @param f A {@code File} object pointing to list of installed GEMs
     * @since 1.5.0
     */
    void setGemPath(Object... f) {
        this.gemPaths.clear()
        if (!f) return // null check
        this.gemPaths.addAll(f as List)
    }

    /** Assigns a single string to a GEM path, scanning it for concatenated GEM Paths, separated by the platform
     * separator. This utility is only for backwards compatibility
     *
     * @param s
     */
    @SuppressWarnings('UnnecessarySetter')
    void setGemPath(Object path) {
        this.gemPaths.clear()
        if (path instanceof CharSequence) {
            this.gemPaths.addAll(setGemPath(path.split(PATH_SEPARATOR)))
        } else if (path) {
            this.gemPaths.addAll(path)
        }
    }

    /** Returns the list of paths to be used for {@code GEM_HOME}
     *
     * @since 1.5.0
     */
    @Optional
    @InputFiles
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
        this.srcDir = f
    }

    /** Sets the new Asciidoctor parent source directory.
     *
     * @param f A {@code File} object pointing to the parent source directory
     */
    void setSourceDir(File f) {
        this.srcDir = f
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
        this.outDir = f
    }

    /** Sets the new Asciidoctor parent output directory.
     *
     * @param f A {@code File} object pointing to the parent output directory
     */
    void setOutputDir(File f) {
        this.outDir = f
    }

    /** Returns the current toplevel output directory
     *
     */
    @OutputDirectory
    File getOutputDir() {
        if (this.outDir == null) {
            this.outDir = new File(project.buildDir, 'asciidoc')
        }
        project.file(this.outDir)
    }

    /** Returns the collection of source documents
     *
     * If sourceDocumentNames was not set or is empty, it will return all asciidoc files
     * in {@code sourceDir}. Otherwise only the files provided earlier to sourceDocumentNames
     * are returned if they are found below {@code sourceDir}
     * @since 1.5.0
     * @deprecated
     */
    @Internal
    FileCollection getSourceDocumentNames() {
        deprecated 'getSourceDocumentNames', 'getSourceFileTree'
        sourceFileTree
    }

    /** Sets a single file to the main source file
     *
     * @param f A file that is relative to {@code sourceDir}
     * @deprecated
     */
    @SuppressWarnings('UnnecessarySetter')
    void setSourceDocumentName(File f) {
        deprecated 'setSourceDocumentName', 'setIncludes', 'File will be converted to a pattern.'
        sources {
            setIncludes([AsciidoctorUtils.getRelativePath(f.absoluteFile, sourceDir.absoluteFile)])
        }
    }

    /** Replaces the current set of source documents with a new set
     *
     * @parm src List of source documents, which must be convertible using {@code project.files}
     * @since 1.5.0
     * @deprecated
     */
    @SuppressWarnings(['DuplicateStringLiteral', 'UnnecessarySetter'])
    void setSourceDocumentNames(Object... src) {
        deprecated 'setSourceDocumentNames', 'setIncludes', 'Files are converted to patterns. Some might not convert correctly. ' +
            'FileCollections will not convert'
        File base = sourceDir.absoluteFile
        def patterns = CollectionUtils.stringize(src as List).collect { String it ->
            def tmpFile = new File(it)
            String relPath
            if (tmpFile.isAbsolute()) {
                relPath = AsciidoctorUtils.getRelativePath(tmpFile.absoluteFile, base)
            } else {
                relPath = it
            }
            logger.debug "setSourceDocumentNames - Found ${it}, converted to ${relPath}"
            relPath
        }
        sources {
            setIncludes(patterns)
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
    Set<File> getOutputDirectories() {
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
            matching(this.sourceDocumentPattern ?: defaultSourceDocumentPattern)
    }

    /** Add patterns for source files or source files via a closure
     *
     * @param cfg PatternSet configuration closure
     * @since 1.5.1
     */
    void sources(Closure cfg) {
        if (sourceDocumentPattern == null) {
            sourceDocumentPattern = new PatternSet()
        }
        def configuration = cfg.clone()
        configuration.delegate = sourceDocumentPattern
        configuration()
    }

    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg CopySpec configuration closure
     * @since 1.5.1
     */
    void resources(Closure cfg) {
        if (this.resourceCopy == null) {
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
    @Internal
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
     * @return A {@code CopySpec}, never null
     * @since 1.5.1
     */
    @Internal
    CopySpec getDefaultResourceCopySpec() {
        project.copySpec {
            from(sourceDir) {
                include 'images/**'
            }
        }
    }

    /** Gets the CopySpec for additional resources
     * If {@code resources} was never called, it will return a default CopySpec otherwise it will return the
     * one built up via successive calls to {@code resources}
     *
     * @return A {@code CopySpec}, never null
     * @since 1.5.1
     */
    @Internal
    CopySpec getResourceCopySpec() {
        this.resourceCopy ?: defaultResourceCopySpec
    }

    /** Gets the additional resources as a FileCollection.
     * If {@code resources} was never called, it will return the file collections as per default CopySpec otherwise it
     * will return the collections as built up via successive calls to {@code resources}
     *
     * @return A {@code FileCollection}, never null
     * @since 1.5.2
     */
    @InputFiles
    @SkipWhenEmpty
    @Optional
    FileCollection getResourceFileCollection() {
        (resourceCopySpec as CopySpecInternal).buildRootResolver().allSource
    }

    @groovy.transform.PackageScope
    File outputDirFor(final File source, final String basePath, final File outputDir, final String backend) {
        String filePath = source.directory ? source.absolutePath : source.parentFile.absolutePath
        String relativeFilePath = normalizePath(filePath) - normalizePath(basePath)
        File baseOutputDir = outputBackendDir(outputDir, backend)
        File destinationParentDir = new File(baseOutputDir, relativeFilePath)
        if (!destinationParentDir.exists()) {
            destinationParentDir.mkdirs()
        }
        destinationParentDir
    }

    @TaskAction
    @CompileStatic
    void processAsciidocSources() {
        if (sourceFileTree.files.size() == 0) {
            logger.lifecycle 'Asciidoc source file tree is empty. Nothing will be processed.'
            return
        }

        if (classpath == null) {
            classpath = project.configurations.getByName(AsciidoctorPlugin.ASCIIDOCTOR)
        }

        File output = outputDir

        ExecutorConfigurationContainer ecc = new ExecutorConfigurationContainer(activeBackends().collect { backend ->
            new ExecutorConfiguration(
                sourceDir: sourceDir,
                outputDir: outputBackendDir(output, backend),
                projectDir: project.projectDir,
                rootDir: project.rootProject.projectDir,
                baseDir: getBaseDir(),
                sourceTree: sourceFileTree.files,
                fatalMessagePatterns: [],
                backendName: backend,
                gemPath: asGemPath(),
                logDocuments: this.logDocuments,
                copyResources: true,
                safeModeLevel: resolveSafeModeLevel(options['safe'], 0),
                requires: (Set) (getRequires() ?: []),
                options: options,
                attributes: attributes,
                asciidoctorExtensions: dehydrateExtensions(getAsciidoctorExtensions())
            )
        })

        Set<File> closurePaths = getAsciidoctorExtensions().findAll {
            it instanceof Closure
        }.collect {
            getClassLocation(it.class)
        }.toSet()

        if(GRADLE_4_OR_BETTER) {
            closurePaths.add(getClassLocation(org.gradle.internal.scripts.ScriptOrigin))
        }

        FileCollection javaExecClasspath = project.files(
            getJavaExecClasspath(
                project,
                classpath ?: project.configurations.getByName(AsciidoctorPlugin.ASCIIDOCTOR)
            ),
            closurePaths
        )

        File execConfigurationData = JavaExecUtils.writeExecConfigurationData(this, ecc.configurations)
        logger.debug("Serialised AsciidoctorJ configuration to ${execConfigurationData}")
        logger.info "Running AsciidoctorJ instance with classpath ${javaExecClasspath.files}"
        checkForExtensionsOnClasspath()

        activeBackends().each { backend ->
            copyResources(outputBackendDir(outputDir, backend), resourceCopySpec)
        }

        runJavaExec(execConfigurationData, javaExecClasspath)
    }

    @CompileStatic
    void checkForExtensionsOnClasspath() {
        URLClassLoader cl = (URLClassLoader)Thread.currentThread().contextClassLoader
        ServiceLoader<ExtensionRegistry> loader = ServiceLoader.load(ExtensionRegistry,cl)
        List<String> names = loader.iterator().collect {
            it.class.name
        }

        if(!names.empty) {
            project.logger.warn "Asciidoctor extensions found on the Gradle classpath. This behaviour is deprecated and will be removed in 2.0. Use a configuration such as `asciidoctor` to explicitly declare those extensions and add the configuration to the asciidoctor task if necessary.\nDiscovered extensions: ${names.join(', ')}"
        }
    }

    @CompileDynamic
    private WorkResult copyResources(File outputDir, CopySpec spec) {
        project.copy {
            into outputDir
            with spec
        }
    }

    @CompileStatic
    private void runJavaExec(File execConfigurationData, FileCollection javaExecClasspath) {
        project.javaexec { JavaExecSpec jes ->
            logger.debug "Running AsciidoctorJ instance with environment: ${jes.environment}"
            jes.main = AsciidoctorJavaExec.canonicalName
            jes.classpath = javaExecClasspath
            jes.args execConfigurationData.absolutePath
        }
    }

    @CompileStatic
    private List<Object> dehydrateExtensions(final Iterable<Object> exts) {
        exts.collect {
            switch (it) {
                case Closure:
                    ((Closure) it).dehydrate()
                    break
                default:
                    it
            }
        } as List<Object>
    }

    private File outputBackendDir(final File outputDir, final String backend) {
        separateOutputDirs ? new File(outputDir, FileUtils.toSafeFileName(backend)) : outputDir
    }

    private static String normalizePath(String path) {
        if (IS_WINDOWS) {
            path = path.replace(DOUBLE_BACKLASH, BACKLASH)
            path = path.replace(BACKLASH, DOUBLE_BACKLASH)
        }
        path
    }

    protected Set<String> activeBackends() {
        if (this.backends) {
            return this.backends
        } else if (backend) {
            return [backend]
        }
        [DEFAULT_BACKEND]
    }

    private static List stringifyList(List input) {
        input.collect { element ->
            if (element instanceof CharSequence) {
                element.toString()
            } else if (element instanceof List) {
                stringifyList(element)
            } else if (element instanceof Map) {
                stringifyMap(element)
            } else if (element instanceof File) {
                element.absolutePath
            } else {
                element
            }
        }
    }

    private static Map stringifyMap(Map input) {
        Map output = [:]
        input.each { key, value ->
            if (value instanceof CharSequence) {
                output[key] = value.toString()
            } else if (value instanceof List) {
                output[key] = stringifyList(value)
            } else if (value instanceof Map) {
                output[key] = stringifyMap(value)
            } else if (value instanceof File) {
                output[key] = value.absolutePath
            } else {
                output[key] = value
            }
        }
        output
    }

    protected static void processCollectionAttributes(Map attributes, rawAttributes) {
        for (attr in rawAttributes) {
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
    private static Map coerceLegacyAttributeFormats(Object attributes) {
        Map transformedMap = [:]
        switch (attributes) {
            case Map:
                transformedMap = attributes
                break
            case CharSequence:
                attributes.replaceAll('([^\\\\]) ', '$1\0').replaceAll('\\\\ ', ' ').split('\0').collect {
                    def split = it.split('=')
                    if (split.size() < 2) {
                        throw new InvalidUserDataException("Unsupported format for attributes: ${attributes}")
                    }
                    transformedMap[split[0]] = split.drop(1).join('=')
                }
                break
            case Collection:
                processCollectionAttributes(transformedMap, attributes)
                break
            default:
                if (attributes.class.isArray()) {
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

    static Class loadClass(String className) {
        cl.loadClass(className)
    }

    private void deprecated(final String method, final String alternative, final String msg = '') {
        logger.lifecycle "Asciidoctor: ${method} is deprecated and will be removed in a future version. " +
            "Use ${alternative} instead. ${msg}"
    }
}

