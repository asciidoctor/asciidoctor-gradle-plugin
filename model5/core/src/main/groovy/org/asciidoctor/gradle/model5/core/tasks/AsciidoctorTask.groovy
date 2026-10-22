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
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.BaseConversionSettings
import org.asciidoctor.gradle.model5.core.SafeMode
import org.asciidoctor.gradle.model5.core.ScriptCollection
import org.asciidoctor.gradle.model5.core.attributes.IncludeDirOutputSettings
import org.asciidoctor.gradle.model5.core.internal.DefaultAsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.internal.DefaultAsciidoctorExecutionSettings
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorOutputData
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal

import java.util.regex.Pattern

import static org.gradle.api.tasks.PathSensitivity.RELATIVE

/**
 * Base task for converting Asciidoc sources into content.
 *
 * @author Schalk W. Cronjé
 * @author Laura Kassovic
 * @author Mattias Reichel
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorTask extends AbstractBaseConversionTask implements AsciidoctorTaskMethods {
    private final Property<AsciidoctorLauncher> launcher
    private final DefaultAsciidoctorExecutionSettings exeSettings
    private final DefaultAsciidoctorConversionSettings conversionSettings

    private final DirectoryProperty originalBaseDir
    private final Provider<Boolean> srcDirIsBaseDir

    AsciidoctorTask() {
        super()
        this.launcher = providerTools().property(AsciidoctorLauncher)
        this.exeSettings = project.objects.newInstance(DefaultAsciidoctorExecutionSettings)
        this.conversionSettings = project.objects.newInstance(DefaultAsciidoctorConversionSettings)
        this.originalBaseDir = project.objects.directoryProperty()
        this.srcDirIsBaseDir = sourceDir.zip(this.originalBaseDir) { src, base -> src == base }

        this.conversionSettings.sourceRootDir.set(determineSourceRootDir())
        this.conversionSettings.baseDir.set(determineBaseDir())

        inputs.property('launcher', launcher.map { it.ecosystemSignature }).optional(true)
        inputs.property('baseDir', originalBaseDir.map { it.asFile.absolutePath }).optional(true)
        inputs.property('exeSettings', exeSettings.inputProperties)
        inputs.property('conversionSettings', conversionSettings.inputProperties)

        inputs.files(determineScriptFiles())
            .optional()
            .withPropertyName('scriptFiles')
            .withPathSensitivity(RELATIVE)

        inputs.files(conversionSettings.templates.map { it.templateDirs })
            .optional()
            .withPathSensitivity(RELATIVE)

        inputs.files { -> exeSettings.additionalClasspath }.optional()
        outputs.files(fsOperations().fileTree(conversionSettings.destinationDir))
    }

    @Internal
    @Override
    Provider<Directory> getOutputDir() {
        conversionSettings.destinationDir
    }

    @Override
    void setLauncher(Provider<? extends AsciidoctorLauncher> launcher) {
        this.launcher.set(launcher)
    }

    @Override
    void setFatalWarnings(Provider<Set<Pattern>> patterns) {
        conversionSettings.fatalWarnings.set(patterns)
    }

    /**
     * Configures output data for this task.
     *
     * @param outputData
     */
    @Override
    void setOutputData(final AsciidoctorOutputData outputData) {
        copyResources = outputData.copyResources
        conversionSettings.tap {
            backend.set(outputData.backend)
            destinationDir.set(outputData.outputDir)
            embedded.set(outputData.embedded)
            customProperties.set(outputData.customProperties)
            docType.set(outputData.docType)
            templates.set(outputData.conversionTemplate)
            attributes.set(adjustAttributesForIncludeDir(outputData.includeDirOutputSettings, outputData.attributes))
        }

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

    @Override
    void setScriptExtensions(Provider<Map<String, ? extends ScriptCollection>> scripts) {
        this.conversionSettings.scriptCollections.set(scripts)
    }

    @Override
    protected void runLauncher(Set<String> includeSrcPatterns, Set<String> excludeSrcPatterns) {
        conversionSettings.sourceFiles.set(
            fsOperations().fileTree(conversionSettings.sourceRootDir.get()).matching {
                exclude(excludeSrcPatterns)
                include(includeSrcPatterns)
            }.files
        )
        launcher.get().run(exeSettings, conversionSettings)
    }

    @Override
    protected BaseConversionSettings getBaseConversionSettings() {
        this.conversionSettings
    }

    private Provider<Directory> determineBaseDir() {
        determineMustUseIntermediateWorkdir().zip(originalBaseDir) { flag, base ->
            final adjustBaseDir = srcDirIsBaseDir.get()
            if (flag && adjustBaseDir) {
                intermediateWorkdir.get()
            } else {
                base
            }
        }
    }

    private Provider<List<File>> determineScriptFiles() {
        conversionSettings.scriptCollections.map { collections ->
            collections.values().collectMany { it.scriptFiles.get() }
        }
    }

    private Provider<Map<String, String>> adjustAttributesForIncludeDir(
        Provider<IncludeDirOutputSettings> includeDir,
        Provider<Map<String, String>> attrs
    ) {
        providerTools().zip3(
            includeDir,
            attrs,
            conversionSettings.sourceRootDir
        ) { idos, a, dir ->
            if (idos.useSourceDirOverPath.getOrElse(false)) {
                a + [(idos.attributeName.get()): dir.asFile.absolutePath]
            } else if (idos.path.present) {
                a + [(idos.attributeName.get()): idos.path.get()]
            } else {
                a
            }
        }.orElse(attrs)
    }
}
