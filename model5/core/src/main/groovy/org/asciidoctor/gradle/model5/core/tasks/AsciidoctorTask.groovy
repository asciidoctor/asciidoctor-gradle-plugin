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
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

import java.util.regex.Pattern

import static org.gradle.api.tasks.PathSensitivity.*

/**
 * Base task for converting Asciidoc sources into content.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorTask extends GrolifantDefaultTask implements AsciidoctorTaskMethods {

    private final Property<AsciidoctorLauncher> launcher
    private final DefaultAsciidoctorExecutionSettings exeSettings
    private final DefaultAsciidoctorConversionSettings conversionSettings
    private final DirectoryProperty sourceDir
    private final DirectoryProperty useIntermediateWorkdir
    private final Property<PatternFilterable> resourcesCopySpec

    AsciidoctorTask() {
        this.launcher = providerTools().property(AsciidoctorLauncher)
        this.exeSettings = project.objects.newInstance(DefaultAsciidoctorExecutionSettings)
        this.conversionSettings = project.objects.newInstance(DefaultAsciidoctorConversionSettings)
        this.sourceDir = project.objects.directoryProperty()
        this.useIntermediateWorkdir = project.objects.directoryProperty()
        this.resourcesCopySpec = project.objects.property(PatternFilterable)
        this.conversionSettings.sourceRootDir.set(this.useIntermediateWorkdir.orElse(this.sourceDir))

        inputs.property('doctype', conversionSettings.docType).optional(true)
        inputs.dir(this.sourceDir)
        inputs.files(conversionSettings.sourceFiles).skipWhenEmpty(true).withPathSensitivity(RELATIVE)

        outputs.files(fsOperations().fileTree(conversionSettings.destinationDir))
        // TODO: How do we know to use an intermediate workdir?
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

        if(outputData.additionalClasspath != null) {
            this.exeSettings.additionalClasspath.from(outputData.additionalClasspath)
        }
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
        this.conversionSettings.baseDir.set(dir)
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
        conversionSettings.sourceFiles.set(
                patterns.zip(conversionSettings.sourceRootDir) { pats, dir ->
                    fsOperations().fileTree(dir).matching(pats).files
                }
        )
    }

    @Override
    void setFatalWarnings(Provider<Set<Pattern>> patterns) {
        conversionSettings.fatalWarnings.set(patterns)
    }

    @TaskAction
    void exec() {
        // copyToIntermediateWorkdir
        launcher.get().run(exeSettings, conversionSettings)
        if (resourcesCopySpec.present) {
            fsOperations().copy {
                it.into(conversionSettings.destinationDir)
                it.from(fsOperations().fileTree(conversionSettings.sourceRootDir).matching(resourcesCopySpec.get()))
            }
        }
    }
}
