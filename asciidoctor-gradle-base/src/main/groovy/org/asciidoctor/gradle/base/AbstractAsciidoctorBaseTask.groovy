package org.asciidoctor.gradle.base

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*
import org.gradle.api.tasks.util.PatternSet

import java.nio.file.Path
import java.util.Optional

import static org.asciidoctor.gradle.base.AsciidoctorUtils.*
import static org.gradle.api.tasks.PathSensitivity.RELATIVE

/** Abstract base task for Asciidoctor that can be shared between AsciidoctorJ and Asciidoctor.js.
 *
 * @since 3.0
 */
@CompileStatic
abstract class AbstractAsciidoctorBaseTask extends DefaultTask {

    private Object srcDir
    private Object outDir
    private Object baseDir

    private PatternSet sourceDocumentPattern
    private PatternSet secondarySourceDocumentPattern
    private CopySpec resourceCopy
    private List<String> copyResourcesForBackends = []

    @Nested
    protected final OutputOptions configuredOutputOptions = new OutputOptions()

    /** Logs documents as they are converted
     *
     */
    @Console
    boolean logDocuments = false


    /** Sets the new Asciidoctor parent source directory.
     *
     * @param f Any object convertible with {@code project.file}.
     */
    void setSourceDir(Object f) {
        this.srcDir = f
    }

    /** Returns the parent directory for Asciidoctor source.
     */
    @Internal
    File getSourceDir() {
        project.file(srcDir)
    }

    /** Returns the current toplevel output directory
     *
     */
    @OutputDirectory
    File getOutputDir() {
        this.outDir != null ? project.file(this.outDir) : null
    }

    /** Sets the new Asciidoctor parent output directory.
     *
     * @param f An object convertible via {@code project.file}
     */
    void setOutputDir(Object f) {
        this.outDir = f
    }

    /** Base directory (current working directory) for a conversion.
     *
     * @return Base directory.
     */
    // IMPORTANT: Do not change this to @InputDirectory as it can lead to file locking issues on
    // Windows. In reality we do not need to track contents of the directory
    // simply the value change - we achieve that via a normal property.
    @Internal
    File getBaseDir() {
        this.baseDir != null ? project.file(this.baseDir) : project.projectDir
    }

    /** Sets the base directory for a conversion.
     *
     * The base directory is used by AsciidoctorJ to set a current working directory for
     * a conversion.
     *
     * If never set, then {@code project.projectDir} will be assumed to be the base directory.
     *
     * @param f Base directory
     */
    void setBaseDir(Object f) {
        this.baseDir = f
    }

    /** Configures sources.
     *
     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
     */
    void sources(final Closure cfg) {
        if (sourceDocumentPattern == null) {
            sourceDocumentPattern = new PatternSet().exclude(UNDERSCORE_LED_FILES)
        }
        Closure configuration = (Closure) cfg.clone()
        configuration.delegate = sourceDocumentPattern
        configuration()
    }

    /** Configures sources.
     *
     * @param cfg Configuration {@link org.gradle.api.Action}. Is passed a {@link PatternSet}.
     */
    void sources(final Action<? super PatternSet> cfg) {
        if (sourceDocumentPattern == null) {
            sourceDocumentPattern = new PatternSet().exclude(UNDERSCORE_LED_FILES)
        }
        cfg.execute(sourceDocumentPattern)
    }

    /** Include source patterns.
     *
     * @param includePatterns ANT-style patterns for sources to include
     */
    void sources(String... includePatterns) {
        new Action<PatternSet>() {

            @Override
            void execute(PatternSet patternSet) {
                patternSet.include(includePatterns)
            }
        }
    }

    /** Clears any of the existing secondary soruces patterns.
     *
     * This should be used if none of the default patterns should be monitored.
     */
    void clearSecondarySources() {
        secondarySourceDocumentPattern = new PatternSet()
    }

    /** Configures secondary sources.
     *
     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
     */
    @CompileDynamic
    void secondarySources(final Closure cfg) {
        if (this.secondarySourceDocumentPattern == null) {
            this.secondarySourceDocumentPattern = defaultSecondarySourceDocumentPattern
        }
        executeDelegatingClosure(this.secondarySourceDocumentPattern, cfg)
    }

    /** Configures sources.
     *
     * @param cfg Configuration {@link Action}. Is passed a {@link PatternSet}.
     */
    void secondarySources(final Action<? super PatternSet> cfg) {
        if (secondarySourceDocumentPattern == null) {
            secondarySourceDocumentPattern = defaultSecondarySourceDocumentPattern
        }
        cfg.execute(secondarySourceDocumentPattern)
    }

    /** Returns a FileTree containing all of the source documents
     *
     * @return If{@code sources} was never called then all asciidoc source files below {@code sourceDir} will
     * be included.
     *
     * @since 1.5.1
     */
    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(RELATIVE)
    FileTree getSourceFileTree() {
        getSourceFileTreeFrom(sourceDir)
    }

    /** Returns a FileTree containing all of the secondary source documents.
     *
     * @return Collection of secondary files
     *
     */
    @InputFiles
    @PathSensitive(RELATIVE)
    FileTree getSecondarySourceFileTree() {
        getSecondarySourceFileTreeFrom(sourceDir)
    }

    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration closure
     * @since 1.5.1
     */
    void resources(Closure cfg) {
        if (this.resourceCopy == null) {
            this.resourceCopy = project.copySpec(cfg)
        } else {
            Closure configuration = (Closure) cfg.clone()
            configuration.delegate = this.resourceCopy
            configuration()
        }
    }

    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     */
    void resources(Action<? super CopySpec> cfg) {
        if (this.resourceCopy == null) {
            this.resourceCopy = project.copySpec(cfg)
        } else {
            cfg.execute(this.resourceCopy)
        }
    }

    /** Copies all resources to the output directory.
     *
     * Some backends (such as {@code html5}) require all resources to be copied to the output directory.
     * This is the default behaviour for this task.
     */
    void copyAllResources() {
        this.copyResourcesForBackends = []
    }

    /** Do not copy any resources to the output directory.
     *
     * Some backends (such as {@code pdf}) process all resources in place.
     *
     */
    void copyNoResources() {
        this.copyResourcesForBackends = null
    }

    /** Copy resources to the output directory only if the backend names matches any of the specified
     * names.
     *
     * @param backendNames List of names for which resources should be copied.
     *
     */
    void copyResourcesOnlyIf(String... backendNames) {
        this.copyResourcesForBackends = []
        this.copyResourcesForBackends.addAll(backendNames)
    }

    /** List of backends for which to copy resources.
     *
     * @return List of backends. Can be {@code null}.
     */
    @Internal
    Optional<List<String>> getCopyResourcesForBackends() {
        Optional.ofNullable(this.copyResourcesForBackends)
    }

    /** Gets the CopySpec for additional resources
     * If {@code resources} was never called, it will return a default CopySpec otherwise it will return the
     * one built up via successive calls to {@code resources}
     *
     * @return A{@link CopySpec}. Never {@code null}.
     */
    @Internal
    protected CopySpec getResourceCopySpec() {
        this.resourceCopy ?: defaultResourceCopySpec
    }

    /** The default CopySpec that will be used if {@code resources} was never called
     *
     * By default anything below {@code $sourceDir/images} will be included.
     *
     *
     * @return A{@link CopySpec}. Never {@code null}.
     */
    @CompileDynamic
    @Internal
    protected CopySpec getDefaultResourceCopySpec() {
        project.copySpec {
            from(sourceDir) {
                include 'images/**'
            }
        }
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    protected String getRelativePath(File target, File base) throws IOException {
        base.toPath().relativize(target.toPath()).toFile().toString()
    }

    /** Group the source files by relative path from the root source directory.
     *
     * @return Map of relative path to set of actual files.
     */
    @Internal
    protected Map<String, List<File>> getSourceFileGroupedByRelativePath() {
        File root = getSourceDir()
        sourceFileTree.files.groupBy { File f ->
            getRelativePath(f.parentFile, root)
        }
    }

    /** Obtains a source tree based on patterns.
     *
     * @param dir Toplevel source directory.
     * @return Source tree based upon configured pattern.
     */
    protected FileTree getSourceFileTreeFrom(File dir) {
        getSourceFileTree(project, dir, this.sourceDocumentPattern ?: defaultSourceDocumentPattern)
    }

    /** Obtains a secondary source tree based on patterns.
     *
     * @param dir Toplevel source directory.
     * @return Source tree based upon configured pattern.
     */
    protected FileTree getSecondarySourceFileTreeFrom(File dir) {
        project.fileTree(dir).
            matching(this.secondarySourceDocumentPattern ?: defaultSecondarySourceDocumentPattern)
    }

    /** The default PatternSet that will be used if {@code sources} was never called
     *
     * By default all *.adoc,*.ad,*.asc,*.asciidoc is included. Files beginning with underscore are excluded
     *
     * @since 1.5.1
     */
    @Internal
    protected PatternSet getDefaultSourceDocumentPattern() {
        PatternSet ps = new PatternSet()
        ps.include '**/*.adoc'
        ps.include '**/*.ad'
        ps.include '**/*.asc'
        ps.include '**/*.asciidoc'
        ps.exclude UNDERSCORE_LED_FILES
    }

    /** The default pattern set for secondary sources.
     *
     * @return {@link #getDefaultSourceDocumentPattern} + `*docinfo*`.
     */
    @Internal
    protected PatternSet getDefaultSecondarySourceDocumentPattern() {
        defaultSourceDocumentPattern
    }

    /** A task may add some default attributes.
     *
     * If the user specifies any of these attributes, then those attributes will not be utilised.
     *
     * The default implementation will add {@code includedir}, {@code revnumber}, {@code gradle-project-group}, {@code gradle-project-name}
     *
     * @param workingSourceDir Directory where source files are located.
     *
     * @return A collection of default attributes.
     */
    protected Map<String, Object> getTaskSpecificDefaultAttributes(File workingSourceDir) {
        Map<String, Object> attrs = [
            includedir           : (Object) workingSourceDir.absolutePath,
            'gradle-project-name': (Object) project.name
        ]

        if (project.version != null) {
            attrs.put('revnumber', (Object) project.version)
        }

        if (project.group != null) {
            attrs.put('gradle-project-group', (Object) project.group)
        }

        attrs
    }

    /** Get the output directory for a specific backend.
     *
     * @param backendName Name of backend
     * @return Output directory.
     */
    protected File getOutputDirFor(final String backendName) {
        if (outputDir == null) {
            throw new GradleException("outputDir has not been defined for task '${name}'")
        }
        configuredOutputOptions.separateOutputDirs ? new File(outputDir, backendName) : outputDir
    }

    /** Adds an input property.
     *
     * Serves as a proxy method in order to deal with the API differences between Gradle 4.0-4.2 and 4.3
     *
     * @param propName Name of property
     * @param value Value of the input property
     */
    @CompileDynamic
    protected void addInputProperty(String propName, Object value) {
        inputs.property propName, value
    }

    /** Name of the implementation engine.
     *
     * @return
     */
    abstract protected String getEngineName()

    /** Validates all preconditions prior to starting to run the conversion process.
     *
     */
    protected void validateConditions() {
        checkForInvalidSourceDocuments()
        checkForIncompatiblePathRoots()
    }

    private void checkForInvalidSourceDocuments() {
        if (!sourceFileTree.filter { File f ->
            f.name.startsWith('_')
        }.empty) {
            throw new InvalidUserDataException('Source documents may not start with an underscore')
        }
    }

    private void checkForIncompatiblePathRoots() {
        if (outputDir == null) {
            throw new GradleException("outputDir has not been defined for task '${name}'")
        }

        Path sourceRoot = sourceDir.toPath().root
        Path baseRoot = getBaseDir().toPath().root
        Path outputRoot = outputDir.toPath().root

        if (sourceRoot != baseRoot || outputRoot != baseRoot) {
            throw new AsciidoctorExecutionException("sourceDir, outputDir and baseDir needs to have the same root filesystem for ${engineName} to function correctly. This is typically caused on Windows where everything is not on the same drive letter.")
        }
    }


}
