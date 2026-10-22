/*
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.model5.core.tasks

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.BaseConversionSettings
import org.asciidoctor.gradle.model5.core.publications.ProvidedExternalSourceSet
import org.asciidoctor.gradle.model5.core.publications.ProvidedExternalSources
import org.gradle.api.file.CopySpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import org.ysb33r.grolifant5.api.core.StringTools
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

import static org.gradle.api.tasks.PathSensitivity.RELATIVE

/**
 * a base conversion task for building conversion tasks in this plugin suite.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
abstract class AbstractBaseConversionTask extends GrolifantDefaultTask implements ConversionTaskBaseMethods {
    private static final String EVERYTHING = '**'
    private final DirectoryProperty sourceDir
    private final Property<PatternFilterable> localSourcePatterns
    private final Property<PatternFilterable> localSecondarySourcePatterns
    private final Property<ProvidedExternalSources> externalSources
    private final Provider<Boolean> hasExternalSources
    private final DirectoryProperty useIntermediateWorkdir
    private final Property<PatternFilterable> resourcesCopySpec

    /**
     * Sets the source directory for actual sources.
     *
     * @param dir Provider to a directory.
     */
    @Override
    void setSourceDir(Provider<Directory> dir) {
        this.sourceDir.set(dir)
    }

    /**
     * The source patterns to look for in the source directory.
     *
     * @param patterns Provider of patterns
     */
    @Override
    void setSourcePatterns(Provider<PatternFilterable> patterns) {
        localSourcePatterns.set(patterns)
    }

    @Override
    void setSecondarySourcePatterns(Provider<PatternFilterable> patterns) {
        localSecondarySourcePatterns.set(patterns)
    }

    /**
     * External sources.
     *
     * @param externalSources Provider to external sources.
     */
    @Override
    void setExternalSources(Provider<? extends ProvidedExternalSources> externalSources) {
        this.externalSources.set(externalSources)
    }

    @TaskAction
    void exec() {
        if (mustUseIntermediateWorkdir()) {
            execWithIntermediateDir()
        } else {
            execWithOneSourceDir()
        }
    }

    protected AbstractBaseConversionTask() {
        this.sourceDir = project.objects.directoryProperty()
        this.localSourcePatterns = project.objects.property(PatternFilterable)
        this.localSecondarySourcePatterns = project.objects.property(PatternFilterable)
        this.externalSources = project.objects.property(ProvidedExternalSources)
        this.hasExternalSources = determineHasExternalSources()
        this.useIntermediateWorkdir = project.objects.directoryProperty().value(determineIntermediateWorkdir())
        this.resourcesCopySpec = project.objects.property(PatternFilterable)

        inputs.files(determineAllInputSources())
            .skipWhenEmpty(true)
            .withPathSensitivity(RELATIVE)
        inputs.files(determineAllOtherSources())
            .optional()
            .withPathSensitivity(RELATIVE)
    }

    /**
     * The root output directory of the conversion process
     *
     * @return Provider to a directory.
     */
    @Internal
    abstract Provider<Directory> getOutputDir()

    /**
     * Access to something that implements {@link BaseConversionSettings}
     * @return The conversion task's instance of BaseConversionSettings
     *
     * @since 5.1
     */
    @Internal
    abstract protected BaseConversionSettings getBaseConversionSettings()

    /**
     * Run the launcher with the provided set of source include and exclude patterns,
     *
     * This method is called from the main task action.
     *
     * @param includeSrcPatterns Set of source include patterns. Can be empty.
     * @param excludeSrcPatterns Set of source exclude patterns. Can be empty.
     *
     * @since 5.1
     * */
    abstract protected void runLauncher(Set<String> includeSrcPatterns, Set<String> excludeSrcPatterns)

    /**
     * Access to the source directory
     * @return Provider to the source directory.
     */
    @Internal
    protected Provider<Directory> getSourceDir() {
        this.sourceDir
    }

    /**
     * Whether an intermediate working directory must be used.
     *
     * @return Resolved version of {@link #determineMustUseIntermediateWorkdir()}
     *
     * @since 5.1
     */
    protected boolean mustUseIntermediateWorkdir() {
        hasExternalSources.get()
    }

    /**
     * Whether an intermediate working directory must be used.
     *
     * @return {@code true} if intermediate working directory must be used.
     *   The default implementation will return ture if there are external sources presents.
     *
     * @since 5.1
     */
    protected Provider<Boolean> determineMustUseIntermediateWorkdir() {
        hasExternalSources
    }

    /**
     * Configured the resources copy spec.
     *
     * @param spec Patterns of files to copy from the source directory.
     */
    protected void setCopyResources(Provider<PatternFilterable> spec) {
        this.resourcesCopySpec.set(spec)
    }

    /**
     * Determine which source directory to pass to the conversion process.
     *
     * @return Directory.
     */
    protected Provider<Directory> determineSourceRootDir() {
        this.useIntermediateWorkdir.orElse(this.sourceDir)
    }

    /**
     * The intermediate workdir in case one is needed.
     *
     * @return Intermediate working directory. Can be empty.
     */
    @Internal
    protected Provider<Directory> getIntermediateWorkdir() {
        this.useIntermediateWorkdir
    }

    /**
     * Access to the attributes utilised  by this task.
     *
     * @return Access to the attributes that are used within this task.
     */
    @Internal
    protected Provider<Map<String, String>> getAttributes() {
        baseConversionSettings.attributes
    }

    private void execWithOneSourceDir() {
        runLauncher(resolveSourceFileIncludePatterns(), resolveSourceFileExcludePatterns())
        if (resourcesCopySpec.present) {
            fsOperations().copy {
                it.into(outputDir)
                it.from(fsOperations().fileTree(determineSourceRootDir()).matching(resourcesCopySpec.get()))
            }
        }
    }

    private void execWithIntermediateDir() {
        final iwd = useIntermediateWorkdir.get()
        final externals = externalSources.get()
        final duplicates = externals.duplicatesStrategy.get()
        final allExternalSources = externals.externalSources.get()

        iwd.asFile.mkdirs()
        fsOperations().sync { spec ->
            spec.duplicatesStrategy = duplicates
            spec.into(useIntermediateWorkdir)
            spec.from(sourceDir).include(EVERYTHING)

            allExternalSources.each { esrc ->
                spec.from(esrc.sourcesAndResources) { CopySpec cs ->
                    cs.include(EVERYTHING)
                    if (esrc.into.present) {
                        cs.into(esrc.into.get())
                    }
                }
            }
        }

        execWithOneSourceDir()

        allExternalSources.each { src ->
            if (src.resourcesPatterns.present) {
                fsOperations().copy {
                    if (src.into.present) {
                        it.into(outputDir.zip(src.into) { base, p -> base.dir(p) })
                    } else {
                        it.into(outputDir)
                    }

                    it.from(src.sourcesAndResources.asFileTree.matching(src.resourcesPatterns.get()))
                }
            }
        }
    }

    private Set<String> resolveSourceFileExcludePatterns() {
        if (mustUseIntermediateWorkdir()) {
            [].toSet() as Set<String>
        } else {
            localSourcePatterns.get().excludes
        }
    }

    private Set<String> resolveSourceFileIncludePatterns() {
        if (mustUseIntermediateWorkdir()) {
            final locals = determineSourceFilePatterns(sourceDir, localSourcePatterns).get()
            final externals = externalSources.get().externalSources.get().collectMany {
                determineSourceFilePatterns(it)
            }
            (locals + externals).toSet()
        } else {
            localSourcePatterns.get().includes
        }
    }

    private Provider<Boolean> determineHasExternalSources() {
        this.externalSources.flatMap { it.externalSources }
            .map { !it.empty }.orElse(false) as Provider<Boolean>
    }

    private Provider<Directory> determineIntermediateWorkdir() {
        final workdir = project.layout.buildDirectory.dir(
            "tmp/asciidoc/${fsOperations().toSafeFileName(name)}/workdir"
        )
        hasExternalSources.zip(workdir) { flag, dir -> flag ? dir : null }
    }

    private Provider<List<String>> determineSourceFilePatterns(
        Provider<Directory> srcDir,
        Provider<PatternFilterable> srcPatterns
    ) {
        final fsOps = fsOperations()
        srcPatterns.zip(srcDir) { pats, dir ->
            fsOps.fileTree(dir).matching(pats).files
                .collect { fsOps.relativize(dir.asFile, it) }
        }
    }

    private List<String> determineSourceFilePatterns(ProvidedExternalSourceSet src) {
        final intoPrefix = src.into.map { "${it}/".toString() }.getOrElse(StringTools.EMPTY)
        List<String> patterns = []
        final originals = src.sourcePatterns.get()

        src.sourcesAndResources.asFileTree.matching(originals).visit { FileVisitDetails fvd ->
            patterns.add(intoPrefix + fvd.relativePath.pathString)
        }
        patterns
    }

    private Provider<List<FileTree>> determineAllOtherSources() {
        this.externalSources.flatMap { it.externalSources }
            .map { list ->
                list.collect { it.sourcesAndResources.asFileTree }
            }.orElse(Collections.<FileTree> emptyList())
    }

    private Provider<List<FileTree>> determineAllExternalSources() {
        final fsOps = fsOperations()
        this.externalSources.flatMap { it.externalSources }
            .map { list ->
                list.collect {
                    fsOps
                        .emptyFileCollection()
                        .from(it.sourcesAndResources)
                        .asFileTree
                        .matching(it.sourcePatterns.get())
                }
            }.orElse(Collections.<FileTree> emptyList())
    }

    private FileCollection determineAllInputSources() {
        // Keep this a FileTree rather than a Set<File>, so the configuration cache stores the directory and
        // patterns and the tree is walked when the task executes.
        final fsOps = fsOperations()
        final localSources = sourceDir.zip(localSourcePatterns) { dir, pats ->
            fsOps.fileTree(dir).matching(pats)
        }
        final externalSources = determineAllExternalSources()
        final secondary = sourceDir.zip(localSecondarySourcePatterns) { dir, pats ->
            if (pats.includes.empty && pats.excludes.empty) {
                fsOperations().emptyFileCollection()
            } else {
                fsOps.fileTree(dir).matching(pats)
            }
        }
        fsOperations().emptyFileCollection().from(localSources).from(externalSources).from(secondary)
    }
}
