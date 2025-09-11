/*
 * Copyright 2013 - 2025 the original author or authors.
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
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.SafeMode
import org.asciidoctor.gradle.model5.core.internal.DefaultAsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.internal.DefaultAsciidoctorExecutionSettings
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorOutputData
import org.asciidoctor.gradle.model5.core.publications.ProvidedExternalSourceSet
import org.asciidoctor.gradle.model5.core.publications.ProvidedExternalSources
import org.gradle.api.file.CopySpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import org.ysb33r.grolifant5.api.core.StringTools
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

import java.util.regex.Pattern

import static org.gradle.api.tasks.PathSensitivity.RELATIVE

/**
 * Base task for converting Asciidoc sources into content.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorTask extends GrolifantDefaultTask implements AsciidoctorTaskMethods {

    private static final String EVERYTHING = '**'

    private final Property<AsciidoctorLauncher> launcher
    private final DefaultAsciidoctorExecutionSettings exeSettings
    private final DefaultAsciidoctorConversionSettings conversionSettings
    private final DirectoryProperty sourceDir
    private final DirectoryProperty originalBaseDir
    private final DirectoryProperty useIntermediateWorkdir
    private final Property<PatternFilterable> resourcesCopySpec
    private final Property<ProvidedExternalSources> externalSources
    private final Provider<Boolean> hasExternalSources
    private final Provider<Boolean> needsIntermediateWorkdir
    private final Provider<Boolean> srcDirIsBaseDir
    private final Property<PatternFilterable> localSourcePatterns
    private final SetProperty<String> resolvedSourcePatterns
    private final ObjectFactory objectFactory

    AsciidoctorTask() {
        this.objectFactory = project.objects
        this.launcher = providerTools().property(AsciidoctorLauncher)
        this.exeSettings = project.objects.newInstance(DefaultAsciidoctorExecutionSettings)
        this.conversionSettings = project.objects.newInstance(DefaultAsciidoctorConversionSettings)
        this.sourceDir = project.objects.directoryProperty()
        this.originalBaseDir = project.objects.directoryProperty()
        this.resourcesCopySpec = project.objects.property(PatternFilterable)
        this.localSourcePatterns = project.objects.property(PatternFilterable)
        this.resolvedSourcePatterns = project.objects.setProperty(String)
        this.srcDirIsBaseDir = sourceDir.zip(this.originalBaseDir) { src, base -> src == base }

        // Work with external sources and determine whether there should be an intermediate workdir
        // Ordering in this block is important!!
        this.externalSources = project.objects.property(ProvidedExternalSources)
        this.hasExternalSources = determineHasExternalSources()
        this.needsIntermediateWorkdir = determineNeedsIntermediateWorkdir()
        this.useIntermediateWorkdir = project.objects.directoryProperty().value(determineIntermediateWorkdir())

        // Setup conversion settings
        this.resolvedSourcePatterns.set(determineSourceFilePatterns())
        this.conversionSettings.sourceRootDir.set(determineSourceRootDir())
        this.conversionSettings.baseDir.set(determineBaseDir())
        this.conversionSettings.sourceFiles.set(
            this.conversionSettings.sourceRootDir.zip(this.resolvedSourcePatterns) { dir, pats ->
                fsOperations().fileTree(dir).matching { include(pats) }.files
            }
        )

        inputs.property('doctype', conversionSettings.docType).optional(true)
        inputs.property('launcher', launcher.map { it.ecosystemSignature }).optional(true)
        inputs.dir(this.sourceDir)
        inputs.files(determineAllInputSources())
            .skipWhenEmpty(true)
            .withPathSensitivity(RELATIVE)

        inputs.files(conversionSettings.templates.map { it.templateDirs })
            .optional()
            .withPathSensitivity(RELATIVE)

        inputs.files(determineAllOtherSources())
            .optional()
            .withPathSensitivity(RELATIVE)

        inputs.files { -> exeSettings.additionalClasspath }.optional()
        outputs.files(fsOperations().fileTree(conversionSettings.destinationDir))
    }

    @Internal
    Provider<Directory> getOutputDir() {
        conversionSettings.destinationDir
    }

    /**
     * Sets the conversion launcher for this task.
     *
     * @param launcher Provider to launcher.
     */
    @Override
    void setLauncher(Provider<? extends AsciidoctorLauncher> launcher) {
        this.launcher.set(launcher)
    }

    /**
     * Configures output data for this task.
     *
     * @param outputData
     */
    @Override
    void setOutputData(final AsciidoctorOutputData outputData) {
        conversionSettings.tap {
            backend.set(outputData.backend)
            destinationDir.set(outputData.outputDir)
            embedded.set(outputData.embedded)
        }
        this.resourcesCopySpec.set(outputData.copyResources)
        this.conversionSettings.docType.set(outputData.docType)
        this.exeSettings.moduleRequires.set(outputData.moduleRequires)
        this.exeSettings.toolchainName.set(outputData.toolchainName)
        this.exeSettings.formatterName.set(outputData.formatterName)

        if (outputData.additionalClasspath != null) {
            this.exeSettings.additionalClasspath.from(outputData.additionalClasspath)
        }

        this.conversionSettings.templates.set(outputData.conversionTemplate)
    }

    /**
     * The safety mode the specific task will run conversions under.
     *
     * @param safeMode Provider of the safety mode.
     */
    @Override
    void setSafeMode(Provider<SafeMode> safeMode) {
        this.exeSettings.safeMode.set(safeMode)
    }

    /**
     * Sets the base directory for conversions.
     *
     * @param dir Provider to a directory.
     */
    @Override
    void setBaseDir(Provider<Directory> dir) {
//        this.conversionSettings.baseDir.set(dir)
        this.originalBaseDir.set(dir)
    }

    /**
     * Sets whether the base directory needs to be adjusted by file.
     *
     * <p>
     *     Note that there will probably be a performance penalty if this is {@code true}.
     * </p>
     *
     * @param flag Provider that will turn on adjustments if it contains Set {@code true}.
     */
    @Override
    void setAdjustBaseDirPerFile(Provider<Boolean> flag) {
        conversionSettings.adjustBaseDirPerFile.set(flag)
    }

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
     * The attributes the task will use.
     *
     * @param attrs Provider of attributes.
     */
    @Override
    void setAttributes(Provider<Map<String, String>> attrs) {
        conversionSettings.attributes.set(attrs)
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
    void setFatalWarnings(Provider<Set<Pattern>> patterns) {
        conversionSettings.fatalWarnings.set(patterns)
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
        if (needsIntermediateWorkdir.get()) {
            execWithIntermediateDir()
        } else {
            execWithOneSourceDir()
        }
    }

    private void execWithOneSourceDir() {
        launcher.get().run(exeSettings, conversionSettings)
        if (resourcesCopySpec.present) {
            fsOperations().copy {
                it.into(conversionSettings.destinationDir)
                it.from(fsOperations().fileTree(conversionSettings.sourceRootDir).matching(resourcesCopySpec.get()))
            }
        }
    }

    private void execWithIntermediateDir() {
        final intermediateWorkdir = useIntermediateWorkdir.get()
        final externals = externalSources.get()
        final duplicates = externals.duplicatesStrategy.get()
        final allExternalSources = externals.externalSources.get()

        intermediateWorkdir.asFile.mkdirs()
        fsOperations().sync { spec ->
            spec.duplicatesStrategy = duplicates
            spec.into(intermediateWorkdir)
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
                        it.into(conversionSettings.destinationDir.zip(src.into) { base, p -> base.dir(p) })
                    } else {
                        it.into(conversionSettings.destinationDir)
                    }

                    it.from(src.sourcesAndResources.asFileTree.matching(src.resourcesPatterns.get()))
                }
            }
        }
    }

    private Provider<Boolean> determineHasExternalSources() {
        this.externalSources.flatMap { it.externalSources }
            .map { !it.empty }.orElse(false) as Provider<Boolean>
    }

    private Provider<Boolean> determineNeedsIntermediateWorkdir() {
        hasExternalSources.orElse(false)
    }

    private Provider<Directory> determineIntermediateWorkdir() {
        final workdir = project.layout.buildDirectory.dir("tmp/asciidoc/${name}/workdir")
        needsIntermediateWorkdir.zip(workdir) { flag, dir -> flag ? dir : null }
    }

    private Provider<Directory> determineSourceRootDir() {
        this.useIntermediateWorkdir.orElse(this.sourceDir)
    }

    private Provider<Directory> determineBaseDir() {
        needsIntermediateWorkdir.zip(originalBaseDir) { flag, base ->
            final adjustBaseDir = srcDirIsBaseDir.get()
            if (flag && adjustBaseDir) {
                useIntermediateWorkdir.get()
            } else {
                base
            }
        }
    }

    private Provider<Set<String>> determineSourceFilePatterns() {
        needsIntermediateWorkdir.map { flag ->
            if (flag) {
                final locals = determineSourceFilePatterns(sourceDir, localSourcePatterns).get()
                final externals = externalSources.get().externalSources.get().collectMany {
                    determineSourceFilePatterns(it)
                }
                (locals + externals).toSet()
            } else {
                localSourcePatterns.get().includes
            }
        }
    }

    private Provider<Set<File>> determineAllOtherSources() {
        this.externalSources.flatMap { it.externalSources }
            .map { list ->
                list.collectMany { it.sourcesAndResources.asFileTree.files }.toSet()
            }.orElse([].toSet() as Set<File>)
    }

    private Provider<Set<File>> determineAllExternalSources() {
        this.externalSources.flatMap { it.externalSources }
            .map { list ->
                list.collectMany {
                    fsOperations()
                        .emptyFileCollection()
                        .from(it.sourcesAndResources)
                        .asFileTree
                        .matching(it.sourcePatterns.get())
                        .files
                }.toSet()
            }.orElse([].toSet() as Set<File>)
    }

    private FileCollection determineAllInputSources() {
        final localSources = sourceDir.zip(localSourcePatterns) { dir, pats ->
            fsOperations().fileTree(dir).matching(pats).files
        }
        final externalSources = determineAllExternalSources()
        fsOperations().emptyFileCollection().from(localSources).from(externalSources)
    }

    private Provider<List<String>> determineSourceFilePatterns(
        Provider<Directory> srcDir,
        Provider<PatternFilterable> srcPatterns
    ) {
        srcPatterns.zip(srcDir) { pats, dir ->
            fsOperations().fileTree(dir).matching(pats).files
                .collect { fsOperations().relativize(dir.asFile, it) }
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
}
