/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.base.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorMultiLanguageException
import org.asciidoctor.gradle.base.AsciidoctorTaskFileOperations
import org.asciidoctor.gradle.base.AsciidoctorTaskTreeOperations
import org.asciidoctor.gradle.base.AsciidoctorUtils
import org.asciidoctor.gradle.base.BaseDirStrategy
import org.asciidoctor.gradle.base.OutputOptions
import org.asciidoctor.gradle.base.Transform
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.ysb33r.grolifant.api.core.ProjectOperations

import javax.annotation.Nullable
import java.nio.file.Path

import static org.asciidoctor.gradle.base.AsciidoctorUtils.UNDERSCORE_LED_FILES
import static org.asciidoctor.gradle.base.AsciidoctorUtils.createDirectoryProperty
import static org.asciidoctor.gradle.base.AsciidoctorUtils.executeDelegatingClosure
import static org.asciidoctor.gradle.base.AsciidoctorUtils.mapToDirectoryProvider
import static org.ysb33r.grolifant.api.core.TaskInputFileOptions.IGNORE_EMPTY_DIRECTORIES
import static org.ysb33r.grolifant.api.core.TaskInputFileOptions.SKIP_WHEN_EMPTY

/**
 * Implements Asciidoctor conversion task file operations.
 *
 * Instances of this is meant to be used via delegation.
 *
 * @since 4.0
 *
 * @author Schalk W. Cronjé
 * @author Lari Hotari
 * @author Gary Hale
 */
@CompileStatic
@SuppressWarnings('MethodCount')
class DefaultAsciidoctorFileOperations implements AsciidoctorTaskFileOperations, AsciidoctorTaskTreeOperations {

    private final Task task
    private final String projectName
    private final DirectoryProperty srcDir
    private final DirectoryProperty outDir
    private final ProjectOperations projectOperations
    private final List<String> languages = []
    private final Map<String, CopySpec> languageResources = [:]
    private final OutputOptions configuredOutputOptions = new OutputOptions()
    private final Provider<File> intermediateWorkDirProvider
    private final String taskName
    private final Property<PatternSet> intermediateArtifactPattern

    private PatternSet sourceDocumentPattern
    private PatternSet secondarySourceDocumentPattern
    private CopySpec resourceCopy
    private boolean withIntermediateWorkDir = false

    DefaultAsciidoctorFileOperations(Task task, String engineName) {
        this.task = task
        this.taskName = task.name
        this.projectName = task.project.name
        this.engineName = engineName
        this.projectOperations = ProjectOperations.find(project)
        this.intermediateArtifactPattern = task.project.objects.property(PatternSet)

        this.srcDir = createDirectoryProperty(project)
        this.outDir = createDirectoryProperty(project)
//        this.defaultRevNumber = projectOperations.projectTools.versionProvider.orElse(Project.DEFAULT_VERSION)
        this.intermediateWorkDirProvider = projectOperations.buildDirDescendant(
                "/tmp/${projectOperations.fsOperations.toSafeFileName(taskName)}.intermediate"
        )

        projectOperations.tasks.inputFiles(
                task.inputs,
                { projectOperations.fsOperations.resolveFilesFromCopySpec(getResourceCopySpec(Optional.empty())) },
                PathSensitivity.RELATIVE,
                IGNORE_EMPTY_DIRECTORIES
        )
        projectOperations.tasks.inputFiles(
                task.inputs,
                { sourceFileTree },
                PathSensitivity.RELATIVE,
                IGNORE_EMPTY_DIRECTORIES, SKIP_WHEN_EMPTY
        )
        projectOperations.tasks.inputFiles(
                task.inputs,
                { secondarySourceFileTree },
                PathSensitivity.RELATIVE,
                IGNORE_EMPTY_DIRECTORIES
        )
    }

    final String engineName

    /** Logs documents as they are converted
     *
     */
    boolean logDocuments = false

    /**
     * The default pattern set for secondary sources.
     *
     * @return By default all *.adoc,*.ad,*.asc,*.asciidoc is included.
     */
    @Override
    PatternSet getDefaultSecondarySourceDocumentPattern() {
        asciidocPatterns()
    }

    /**
     * The default {@link PatternSet} that will be used if {@code sources} was never called
     *
     * @return By default all *.adoc,*.ad,*.asc,*.asciidoc is included.
     *   Files beginning with underscore are excluded
     */
    @Override
    PatternSet getDefaultSourceDocumentPattern() {
        asciidocPatterns(UNDERSCORE_LED_FILES)
    }

    /**
     * A provider of patterns identifying intermediate artifacts.
     *
     * @return Provider to a {@link PatternSet}. Can be empty.
     */
    @Override
    Provider<PatternSet> getIntermediateArtifactPatternProvider() {
        this.intermediateArtifactPattern
    }

    /**
     * Gets the CopySpec for additional resources.
     *
     * If {@code resources} was never called, it will return a default CopySpec otherwise it will return the
     * one built up via successive calls to {@code resources}
     *
     * @param lang Language to to apply to or empty for no-language support.
     * @return A{@link CopySpec}. Never {@code null}.
     */
    @Override
    CopySpec getResourceCopySpec(Optional<String> lang) {
        this.resourceCopy ?: getDefaultResourceCopySpec(lang)
    }

    /**
     * The default CopySpec that will be used if {@code resources} was never called
     *
     * By default, anything below {@code $sourceDir/images} will be included.
     *
     * @param lang Language to use. Can be empty (not {@code null}) when not to use a language.
     * @return A{@link CopySpec}. Never {@code null}.
     */
    @Override
    CopySpec getDefaultResourceCopySpec(Optional<String> lang) {
        projectOperations.copySpec({ CopySpec cs ->
            cs.tap {
                from(lang.present ? new File(sourceDir, lang.get()) : sourceDir) {
                    include 'images/**'
                }
            }
        } as Action<CopySpec>)
    }

    /**
     * Returns the copy specification for the resources of a specific language.
     *
     * @param lang Language
     *
     * @return Copy specification. Can be {@code null}.
     */
    @Override
    CopySpec getLanguageResourceCopySpec(String lang) {
        languageResources[lang]
    }

    /**
     * Gets the language-specific secondary source tree.
     *
     * @param lang Language
     * @return Language-specific source tree.
     */
    @Override
    FileTree getLanguageSecondarySourceFileTree(String lang) {
        getSecondarySourceFileTreeFrom(new File(sourceDir, lang))
    }

    /**
     * Gets the language-specific source tree
     *
     * @param lang Language
     * @return Language-specific source tree.
     */
    @Override
    FileTree getLanguageSourceFileTree(String lang) {
        getSourceFileTreeFrom(new File(sourceDir, lang))
    }

    /**
     * Obtains a secondary source tree based on patterns.
     *
     * @param dir Toplevel source directory.
     * @return Source tree based upon configured pattern.
     */
    @Override
    FileTree getSecondarySourceFileTreeFrom(File dir) {
        Spec<FileTreeElement> primarySourceSpec = (this.sourceDocumentPattern ?: defaultSourceDocumentPattern).asSpec
        projectOperations.fileTree(dir)
                .matching(this.secondarySourceDocumentPattern ?: defaultSecondarySourceDocumentPattern)
                .matching { PatternFilterable target ->
                    target.exclude(primarySourceSpec)
                }
    }

    /**
     * Obtains a source tree based on patterns.
     *
     * @param dir Toplevel source directory.
     * @return Source tree based upon configured pattern.
     */
    @Override
    FileTree getSourceFileTreeFrom(File dir) {
        AsciidoctorUtils.getSourceFileTree(
                projectOperations,
                dir,
                this.sourceDocumentPattern ?: defaultSourceDocumentPattern
        )
    }

    /** Sets the new Asciidoctor parent source directory.
     *
     * @param f Any object convertible with {@code project.file}.
     */
    @Override
    void setSourceDir(Object f) {
        this.srcDir.set(mapToDirectoryProvider(project, f))
    }

    /** Sets the new Asciidoctor parent source directory in a declarative style.
     *
     * @param f Any object convertible with {@code project.file}.
     *
     * @since 3.0
     */
    @Override
    void sourceDir(Object f) {
        this.srcDir.set(mapToDirectoryProvider(project, f))
    }

    /**
     * Returns the parent directory for Asciidoctor source as a property object.
     */
    @Override
    DirectoryProperty getSourceDirProperty() {
        this.srcDir
    }

    /** Returns the current toplevel output directory
     *
     */
    @Override
    File getOutputDir() {
        this.outDir.asFile.get()
    }

    /** Sets the new Asciidoctor parent output directory.
     *
     * @param f An object convertible via {@code project.file}
     */
    @Override
    void setOutputDir(Object f) {
        this.outDir.set(project.file(f))
    }

    /**
     * Returns the current toplevel output directory as a property object.
     */
    @Override
    DirectoryProperty getOutputDirProperty() {
        this.outDir
    }

    /** Configures sources.
     *
     * @param cfg Configuration closure. Is passed a {@link org.gradle.api.tasks.util.PatternSet}.
     */
    @Override
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
    @Override
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
    @Override
    void sources(String... includePatterns) {
        sources(new Action<PatternSet>() {

            @Override
            void execute(PatternSet patternSet) {
                patternSet.include(includePatterns)
            }
        })
    }

    /** Clears existing sources patterns.
     */
    @Override
    void clearSources() {
        sourceDocumentPattern = null
    }

    /** Clears any of the existing secondary soruces patterns.
     *
     * This should be used if none of the default patterns should be monitored.
     */
    @Override
    void clearSecondarySources() {
        secondarySourceDocumentPattern = new PatternSet()
    }

    /** Configures secondary sources.
     *
     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
     */
    @Override
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
    @Override
    void secondarySources(final Action<? super PatternSet> cfg) {
        if (secondarySourceDocumentPattern == null) {
            secondarySourceDocumentPattern = defaultSecondarySourceDocumentPattern
        }
        cfg.execute(secondarySourceDocumentPattern)
    }

    /** Returns a FileTree containing all of the source documents
     *
     * If a filter with {@link #sources} was never set then all asciidoc source files
     * below {@link #setSourceDir} will be included. If multiple languages are used all
     * of the language source sets be will included.
     *
     * @return Applicable source trees.
     *
     * @since 1.5.1
     */
    @Override
    FileTree getSourceFileTree() {
        if (languages.empty) {
            getSourceFileTreeFrom(sourceDir)
        } else {
            languages.sum { lang ->
                getLanguageSourceFileTree(lang)
            } as FileTree
        }
    }

    /** Returns a FileTree containing all of the secondary source documents.
     *
     * If a filter with {@link #secondarySources} was never set then all asciidoc source files
     * below {@link #setSourceDir} will be included. If multiple languages are used all
     * of the language secondary source sets be will included.
     *
     * @return Collection of secondary source files
     *
     */
    @Override
    FileTree getSecondarySourceFileTree() {
        if (languages.empty) {
            getSecondarySourceFileTreeFrom(sourceDir)
        } else {
            languages.sum { lang ->
                getLanguageSecondarySourceFileTree(lang)
            } as FileTree
        }
    }

    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link org.gradle.api.file.CopySpec} runConfiguration closure
     * @since 1.5.1
     */
    @Override
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
     * @param cfg {@link org.gradle.api.file.CopySpec} runConfiguration {@link Action}
     */
    @Override
    void resources(Action<? super CopySpec> cfg) {
        if (this.resourceCopy == null) {
            this.resourceCopy = project.copySpec(cfg)
        } else {
            cfg.execute(this.resourceCopy)
        }
    }

    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * If not languages are set. these resources will be ignored.
     *
     * @param cfg {@link CopySpec} runConfiguration closure
     * @param lang Language to which these resources will be applied to.
     * @since 3.0.0
     */
    @Override
    void resources(final String lang, Closure cfg) {
        if (this.languageResources[lang] == null) {
            this.languageResources[lang] = project.copySpec(cfg)
        } else {
            Closure configuration = (Closure) cfg.clone()
            configuration.delegate = this.languageResources[lang]
            configuration()
        }
    }

    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * If not languages are set. these resources will be ignored.
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     * @param lang Language to which these resources will be applied to.
     * @since 3.0.0
     */
    @Override
    void resources(final String lang, Action<? super CopySpec> cfg) {
        if (this.languageResources[lang] == null) {
            this.languageResources[lang] = project.copySpec(cfg)
        } else {
            cfg.execute(this.languageResources[lang])
        }
    }

    /** Some extensions such as {@code ditaa} creates images in the source directory.
     *
     * Use this setting to copy all sources and resources to an intermediate work directory
     * before processing starts. This will keep the source directory pristine
     */
    @Override
    void useIntermediateWorkDir() {
        withIntermediateWorkDir = true
    }

    /**
     * Checks whether an intermediate workdir is required.
     *
     * @return {@code true} is there is an intermediate working directory.
     */
    @Override
    boolean hasIntermediateWorkDir() {
        this.withIntermediateWorkDir
    }

    /** The document conversion might generate additional artifacts that could
     * require copying to the final destination.
     *
     * An example is use of {@code ditaa} diagram blocks. These artifacts can be specified
     * in this block. Use of the option implies {@link #useIntermediateWorkDir}.
     *
     * @param cfg Configures a {@link PatternSet} with a base directory of the intermediate working
     * directory.
     */
    @Override
    void withIntermediateArtifacts(@DelegatesTo(PatternSet) Closure cfg) {
        useIntermediateWorkDir()
        if (!this.intermediateArtifactPattern.present) {
            this.intermediateArtifactPattern.set(new PatternSet())
        }
        executeDelegatingClosure(this.intermediateArtifactPattern.get(), cfg)
    }

    /**
     * Additional artifacts created by Asciidoctor that might require copying.
     *
     * @param cfg Action that configures a {@link PatternSet}.
     *
     * @see {@link #withIntermediateArtifacts(Closure cfg)}
     */
    @Override
    void withIntermediateArtifacts(final Action<PatternSet> cfg) {
        useIntermediateWorkDir()
        if (!this.intermediateArtifactPattern.present) {
            this.intermediateArtifactPattern.set(new PatternSet())
        }
        cfg.execute(this.intermediateArtifactPattern.get())
    }

    /**
     * The directory that will be the intermediate directory should one be required.
     *
     * @return Intermediate working directory
     */
    @Override
    Provider<File> getIntermediateWorkDirProvider() {
        this.intermediateWorkDirProvider
    }

    /**
     * Returns a list of all output directories by backend
     *
     * @since 1.5.1
     */
    @Override
    Set<File> getBackendOutputDirectories() {
        if (languages.empty) {
            Transform.toSet(configuredOutputOptions.backends) {
                String it -> getOutputDirFor(it)
            }
        } else {
            configuredOutputOptions.backends.collectMany { String backend ->
                Transform.toList(languages) { String lang ->
                    getOutputDirFor(backend, lang)
                }
            }.toSet()
        }
    }

    /** Obtain List of languages the sources documents are written in.
     *
     * @return List of languages. Can be empty, but never {@code null}.
     *
     * @since 3.0.0
     */
    @Override
    List<String> getLanguages() {
        this.languages
    }

    /** Reset current list of languages and replace with a new set.
     *
     * @param langs List of new languages
     *
     * @since 3.0.0
     */
    @Override
    void setLanguages(Iterable<String> langs) {
        this.languages.clear()
        this.languages.addAll(langs)
    }

    /** Add to list of languages to process.
     *
     * @param langs List of additional languages
     *
     * @since 3.0.0
     */
    @Override
    void languages(Iterable<String> langs) {
        this.languages.addAll(langs)
    }

    /** Add to list of languages to process.
     *
     * @param langs List of additional languages
     *
     * @since 3.0.0
     */
    @Override
    void languages(String... langs) {
        this.languages.addAll(langs)
    }

    /**
     * Validates that the path roots are sane.
     *
     * @param baseDir Base directory strategy. Can be {@code null}
     */
    @Override
    void checkForIncompatiblePathRoots(@Nullable BaseDirStrategy baseDir) {
        if (outputDir == null) {
            throw new InvalidUserDataException("outputDir has not been defined for task '${taskName}'")
        }

        Path baseRoot = languages.empty ?
                baseDir?.baseDir?.toPath()?.root :
                baseDir?.getBaseDir(languages[0])?.toPath()?.root
        if (baseRoot != null) {
            Path sourceRoot = sourceDir.toPath().root
            Path outputRoot = outputDir.toPath().root

            if (sourceRoot != baseRoot || outputRoot != baseRoot) {
                throw new InvalidUserDataException(
                        "sourceDir, outputDir and baseDir needs to have the same root filesystem for ${engineName} " +
                                'to function correctly. ' +
                                'This is typically caused on Windows where everything is not on the same drive letter.'
                )
            }
        }
    }

    /**
     * Validates the source tree
     */
    @Override
    void checkForInvalidSourceDocuments() {
        if (!sourceFileTree.filter { File f ->
            f.name.startsWith('_')
        }.empty) {
            throw new InvalidUserDataException('Source documents may not start with an underscore')
        }
    }

    /**
     * A task may add some default attributes.
     * If the user specifies any of these attributes, then those attributes will not be utilised.
     * The default implementation will add {@code includedir}, {@code revnumber}, {@code gradle-project-group},
     * {@code gradle-project-name}
     *
     * @param workingSourceDir Directory where source files are located.
     *
     * @return A collection of default attributes.
     */
    @Override
    Map<String, ?> getTaskSpecificDefaultAttributes(File workingSourceDir) {
        Provider<String> group = projectOperations.projectTools.groupProvider.orElse('')
        Provider<String> revnumber = projectOperations.projectTools.versionProvider.orElse(Project.DEFAULT_VERSION)
        [
                includedir              : workingSourceDir.absolutePath,
                'gradle-project-name'   : projectName,
                'gradle-project-group'  : group,
                'gradle-project-version': revnumber,
                revnumber               : revnumber
        ]
    }

    /** Get the output directory for a specific backend.
     *
     * @param backendName Name of backend
     * @return Output directory.
     */
    protected File getOutputDirFor(final String backendName) {
        if (outputDir == null) {
            throw new GradleException("outputDir has not been defined for task '${taskName}'")
        }
        if (!this.languages.empty) {
            throw new AsciidoctorMultiLanguageException('Use getOutputDir(backendname,language) instead.')
        }
        configuredOutputOptions.separateOutputDirs ? new File(outputDir, backendName) : outputDir
    }

    /** Get the output directory for a specific backend.
     *
     * @param backendName Name of backend
     * @param language Language for which sources are being generated.
     * @return Output directory.
     *
     * @since 3.0.0
     */
    protected File getOutputDirFor(final String backendName, final String language) {
        if (outputDir == null) {
            throw new GradleException("outputDir has not been defined for task '${taskName}'")
        }
        configuredOutputOptions.separateOutputDirs ?
                new File(outputDir, "${language}/${backendName}") :
                new File(outputDir, language)
    }

    private Project getProject() {
        task.project
    }

    private PatternSet asciidocPatterns(String... excluding) {
        PatternSet ps = new PatternSet()
        ps.include '**/*.adoc'
        ps.include '**/*.ad'
        ps.include '**/*.asc'
        ps.include '**/*.asciidoc'

        if (excluding.size()) {
            ps.exclude(excluding)
        }
        ps
    }
}
