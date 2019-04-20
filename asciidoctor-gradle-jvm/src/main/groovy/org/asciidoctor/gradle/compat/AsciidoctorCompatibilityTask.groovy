/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.compat

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.SafeMode
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.internal.ExecutorConfigurationContainer
import org.asciidoctor.gradle.internal.ExecutorLogLevel
import org.asciidoctor.gradle.internal.JavaExecUtils
import org.asciidoctor.gradle.remote.AsciidoctorJavaExec
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.copy.CopySpecInternal
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.WorkResult
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.FileUtils
import org.gradle.process.JavaExecSpec
import org.ysb33r.grolifant.api.OperatingSystem

import java.util.stream.Collectors

import static org.asciidoctor.gradle.base.AsciidoctorUtils.UNDERSCORE_LED_FILES
import static org.asciidoctor.gradle.base.AsciidoctorUtils.getClassLocation
import static org.asciidoctor.gradle.base.AsciidoctorUtils.getSourceFileTree
import static org.asciidoctor.gradle.internal.JavaExecUtils.getJavaExecClasspath
import static org.asciidoctor.gradle.jvm.AsciidoctorJExtension.GUAVA_REQUIRED_FOR_EXTERNALS
import static org.ysb33r.grolifant.api.StringUtils.stringize

/** The core functionality of the Asciidoctor task type as it was in the 1.5.x series.
 *
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
@Deprecated
@SuppressWarnings(['MethodCount', 'Instanceof', 'LineLength'])
@CompileStatic
class AsciidoctorCompatibilityTask extends DefaultTask {
    private static final String PATH_SEPARATOR = OperatingSystem.current().pathSeparator
    private static
    final String MIGRATE_GEMS_MSG = 'When upgrading GEMs, \'requires\' will need to be set via the asciidoctorj project and task docExtensions. Use  setGemPaths method in extension(s) to set GEM paths.'

    private static final String DEFAULT_BACKEND = AsciidoctorBackend.HTML5.id
    private boolean baseDirSetToNull
    private Object outDir
    private Object srcDir
    private final List<Object> gemPaths = []
    private final Set<String> backends = []
    private final Set<String> requires = []
    private final Set<String> migrationMessages = []
    private final Map opts = [:]
    private final Map attrs = [:]
    private PatternSet sourceDocumentPattern
    private CopySpec resourceCopy
    private boolean separateOutputDirs = true

    @SuppressWarnings('CouldBeSwitchStatement')
    static int resolveSafeModeLevel(Object safe, int defaultLevel = SafeMode.UNSAFE.level) {
        if (safe == null) {
            defaultLevel
        } else if (safe instanceof SafeMode) {
            safe.level
        } else if (safe instanceof Number) {
            SafeMode.safeMode(safe as Integer).level
        } else if (safe instanceof CharSequence) {
            SafeMode.valueOf(safe.toString().toUpperCase()).level
        } else {
            defaultLevel
        }
    }

    /** If set to true each backend will be output to a separate subfolder below {@code outputDir}
     * @since 1.5.1
     */
    @Input
    boolean getSeparateOutputDirs() {
        migrationMessage('seperateOutputDirs', 'Separate output directories are now configured via outputOptions.separateOutputDirs')
        this.separateOutputDirs
    }

    void setSeparateOutputDirs(boolean v) {
        this.separateOutputDirs = v
    }

    @Optional
    @InputDirectory
    File baseDir

    /** Set to {@code true} in order to add legacy attributes
     * {@code projectdir} and {@code rootdir}.
     *
     * This is for ease of upgrading. Documents should be converted to
     * use {@code gradle-projectdir} amd {@code gradle-rootdir} instead.
     */
    @Input
    boolean legacyAttributes = false

    /** Logs documents as they are converted
     *
     */
    @Optional
    @Console
    boolean logDocuments = false

    /**
     * Stores the extensionRegistry defined in the runConfiguration phase
     * to register them in the execution phase.
     */
    @Internal
    final List<Object> asciidoctorExtensions = []

    @Optional
    @InputFiles
    Configuration classpath

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
        if (!m) {
            return
        } // null check
        if (m.containsKey('attributes')) {
            migrationMessage(
                'setOptions',
                """Pay attention to these attributes found in options.
Currently attributes will be replaced due to assignment.
Please use one of the following instead instead as current behaviour will no longer be available when upgrading:
   - 'attributes'
   - 'project.asciidoctorj.attributes'
   - '${name}.asciidoctorj.attributes'
""")
            attrs.clear()
            attrs.putAll(coerceLegacyAttributeFormats(m.attributes))
            m.remove('attributes')
        }
        this.opts.clear()
        this.opts.putAll(m)
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
        if (!m) {
            return
        } // null check
        if (m.containsKey('attributes')) {
            migrationMessage(
                'setOptions',
                """Pay attention to these attributes found in options.
Currently these attributes are added to existing attributes.
Please use one of the following instead instead as current behaviour will no longer be available when upgrading:
   - 'attributes'
   - 'project.asciidoctorj.attributes'
   - '${name}.asciidoctorj.attributes'
""")
            attributes coerceLegacyAttributeFormats(m.attributes)
            m.remove('attributes')
        }
        this.opts.putAll(m)
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
        migrationMessage('setAttributes', 'the behaviour of setAttributes will change when upgrading. You may decide to use the asciidoctorj.setAttributes that exists as a project or task extension instead')
        this.attrs.clear()
        if (m) {
            this.attrs.putAll(m)
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
            this.attrs.putAll(coerceLegacyAttributeFormats(input))
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
    @Deprecated
    void setRequires(Iterable<Object> b) {
        migrationMessage('setRequires', MIGRATE_GEMS_MSG)
        this.requires.clear()
        this.requires.addAll(stringize(b))
    }

    /** Appends new set of Ruby modules to be included.
     *
     * @param b One or more ruby modules to be included
     * @since 1.5.1
     */
    @Deprecated
    void requires(Object... b) {
        migrationMessage('requires', MIGRATE_GEMS_MSG)
        this.requires.addAll(stringize(b as List))
    }

    /** Returns the current set of Asciidoctor backends that will be used for document generation
     *
     * @since 0.7.1* @deprecated
     */
    @Optional
    @Input
    Set<String> getBackends() {
        migrationMessage('getBackends', 'Use outputOptions.getBackends')
        this.backends
    }

    /** Applies a new set of Asciidoctor backends that will be used for document generation clearing any
     * previous backends
     *
     * @param b List of backends. Each item must be convertible to string.
     *
     * @since 0.7.1
     */
    void setBackends(Iterable<Object> b) {
        migrationMessage('setBackends', 'Use outputOptions.setBackends')
        this.backends.clear()
        this.backends.addAll(stringize(b))
    }

    /** Appends additional Asciidoctor backends that will be used for document generation.
     *
     * @param b List of backends. Each item must be convertible to string.
     *
     * @since 1.5.1
     */
    void backends(Object... b) {
        migrationMessage('backends', 'Use outputOptions.setbackends')
        this.backends.addAll(stringize(b as List))
    }

    /** Defines extensionRegistry. The given parameters should
     * either contain Asciidoctor Groovy DSL closures or files
     * with content conforming to the Asciidoctor Groovy DSL.
     */
    @Deprecated
    void extensions(Object... exts) {
        migrationMessage(
            'docExtensions',
            'Extensions will need to be set via the asciidoctorj project and task docExtensions'
        )
        if (!exts) {
            return
        } // null check
        asciidoctorExtensions.addAll(exts as List)
    }

    /** Sets a new gemPath to be used
     *
     * @param f A path object can be be converted with {@code project.file}.
     * @since 1.5.1
     */
    @Deprecated
    void gemPath(Object... f) {
        migrationMessage(
            'gemPath',
            'GEM paths will need to be set via the asciidoctorj project and task docExtensions using the gemPaths method'
        )
        if (!f) {
            return
        } // null check
        this.gemPaths.addAll(f as List)
    }

    /** Sets a new list of GEM paths to be used.
     *
     * @param f A {@code File} object pointing to list of installed GEMs
     * @since 1.5.0
     */
    @Deprecated
    void setGemPath(Object... f) {
        migrationMessage('setGemPath(Object...)', MIGRATE_GEMS_MSG)
        this.gemPaths.clear()
        if (!f) {
            return
        } // null check
        this.gemPaths.addAll(f as List)
    }

    /** Assigns a single string to a GEM path, scanning it for concatenated GEM Paths, separated by the platform
     * separator. This utility is only for backwards compatibility
     *
     * @param s
     */
    @SuppressWarnings('UnnecessarySetter')
    @CompileDynamic
    void setGemPath(Object path) {
        migrationMessage('setGemPath(Object)', MIGRATE_GEMS_MSG)
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
            backends.stream().map {
                new File(outputDir, it)
            }.collect(Collectors.toSet())
        } else {
            [outputDir] as Set
        }
    }

    /** Returns a FileTree containing all of the source documents
     *
     * @return If{@code sources} was never called then all asciidoc source files below {@code sourceDir} will
     * be included
     * @since 1.5.1
     */
    @InputFiles
    @SkipWhenEmpty
    FileTree getSourceFileTree() {
        getSourceFileTree(project, sourceDir, this.sourceDocumentPattern ?: defaultSourceDocumentPattern)
    }

    /** Add patterns for source files or source files via a closure
     *
     * @param cfg PatternSet runConfiguration closure
     * @since 1.5.1
     */
    void sources(Closure cfg) {
        if (sourceDocumentPattern == null) {
            sourceDocumentPattern = new PatternSet().exclude('**/_*')
        }
        Closure configuration = (Closure) (cfg.clone())
        configuration.delegate = sourceDocumentPattern
        configuration()
    }

    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg CopySpec runConfiguration closure
     * @since 1.5.1
     */
    void resources(Closure cfg) {
        if (this.resourceCopy == null) {
            this.resourceCopy = project.copySpec(cfg)
        } else {
            Closure configuration = (Closure) (cfg.clone())
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
        ps.exclude UNDERSCORE_LED_FILES
    }

    /** The default CopySpec that will be used if {@code resources} was never called
     *
     * By default anything below {@code $sourceDir/images} will be included.
     *
     * @return A {@code CopySpec}, never null
     * @since 1.5.1
     */
    @Internal
    @CompileDynamic
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
    @Optional
    FileCollection getResourceFileCollection() {
        (resourceCopySpec as CopySpecInternal).buildRootResolver().allSource
    }

    @TaskAction
    @CompileStatic
    void processAsciidocSources() {
        File output = outputDir

        final Map finalAttributes = [
            'gradle-project-group': project.group,
            'gradle-project-name' : project.name,
            'revnumber'           : project.version
        ]
        if (legacyAttributes) {
            finalAttributes.putAll([
                'project-version': project.version,
                'project-group'  : project.group,
                'project-name'   : project.name
            ])
        }
        finalAttributes.putAll(attributes)

        ExecutorConfigurationContainer ecc = new ExecutorConfigurationContainer(activeBackends().stream().map { backend ->
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
                requires: getRequires(),
                options: options,
                attributes: finalAttributes,
                legacyAttributes: legacyAttributes,
                asciidoctorExtensions: dehydrateExtensions(getAsciidoctorExtensions()),
                executorLogLevel: ExecutorLogLevel.WARN
            )
        }.collect(Collectors.toList()))

        Set<File> closurePaths = getAsciidoctorExtensions().findAll {
            it instanceof Closure
        }.stream().map {
            getClassLocation(it.class)
        }.collect(Collectors.toSet())
        closurePaths.add(getClassLocation(org.gradle.internal.scripts.ScriptOrigin))

        FileCollection javaExecClasspath = project.files(
            getJavaExecClasspath(
                project,
                classpath ?: project.configurations.getByName(AsciidoctorCompatibilityPlugin.ASCIIDOCTOR),
                GUAVA_REQUIRED_FOR_EXTERNALS
            ),
            closurePaths
        )

        if (legacyAttributes) {
            migrationMessage 'legacyAttributes=true', '''Switch documents to use the following attributes instead:
   - gradle-projectdir (old=projectdir)
   - gradle-rootdir (old=rootdir)
   - gradle-project-name (old=project-name)
   - gradle-projetc-group (old=project-group)
   - revnumber (old=project-version)
            '''
        }

        File execConfigurationData = JavaExecUtils.writeExecConfigurationData(this, ecc.configurations)
        logger.debug("Serialised AsciidoctorJ configuration to ${execConfigurationData}")
        logger.info "Running AsciidoctorJ instance with classpath ${javaExecClasspath.files}"

        activeBackends().each { backend ->
            copyResources(outputBackendDir(outputDir, backend), resourceCopySpec)
        }

        runJavaExec(execConfigurationData, javaExecClasspath)
    }

    // Helper method to be able to produce migration messages
    @Internal
    Set<String> getMigrationMessages() {
        this.migrationMessages
    }

    @CompileDynamic
    protected static void processCollectionAttributes(Map attributes, List rawAttributes) {
        for (Object attr in rawAttributes) {
            if (attr instanceof CharSequence) {
                String[] tokens = attr.toString().split('=', 2)
                attributes.put(tokens[0], tokens.size() > 1 ? tokens[1] : '')
            } else {
                throw new InvalidUserDataException("Unsupported type for attribute ${attr}: ${attr.getClass()}")
            }
        }
    }

    protected AsciidoctorCompatibilityTask() {
        srcDir = project.file('src/docs/asciidoc')
    }

    protected Set<String> activeBackends() {
        this.backends.empty ? [DEFAULT_BACKEND].toSet() : this.backends
    }

    @CompileDynamic
    @SuppressWarnings(['DuplicateStringLiteral', 'DuplicateNumberLiteral'])
    private static Map coerceLegacyAttributeFormats(Object attributes) {
        Map transformedMap = [:]
        switch (attributes) {
            case Map:
                transformedMap = attributes
                break
            case CharSequence:
                Transform.toList(attributes.replaceAll('([^\\\\]) ', '$1\0').replaceAll('\\\\ ', ' ').split('\0') as List) {
                    String[] split = it.split('=')
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

    @CompileDynamic
    @SuppressWarnings('CouldBeSwitchStatement')
    private static List stringifyList(List input) {
        Transform.toList(input) { element ->
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

    @CompileDynamic
    @SuppressWarnings('CouldBeSwitchStatement')
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

    private File outputBackendDir(final File outputDir, final String backend) {
        separateOutputDirs ? new File(outputDir, FileUtils.toSafeFileName(backend)) : outputDir
    }

    @CompileDynamic
    private WorkResult copyResources(File outputDir, CopySpec spec) {
        project.copy {
            into outputDir
            with spec
        }
    }

    private void migrationMessage(final String currentMethod, final String upgradeInstructions) {
        this.migrationMessages.add(
            "You have used '${currentMethod}'. When upgrading you will need to: ${upgradeInstructions}".toString()
        )
    }

    private void runJavaExec(File execConfigurationData, FileCollection javaExecClasspath) {
        project.javaexec { JavaExecSpec jes ->
            logger.debug "Running AsciidoctorJ instance with environment: ${jes.environment}"
            jes.main = AsciidoctorJavaExec.canonicalName
            jes.classpath = javaExecClasspath
            jes.args execConfigurationData.absolutePath
        }
    }

    private List<Object> dehydrateExtensions(final List<Object> exts) {
        Transform.toList(exts) {
            switch (it) {
                case Closure:
                    ((Closure) it).dehydrate()
                    break
                default:
                    it
            }
        }
    }
}

