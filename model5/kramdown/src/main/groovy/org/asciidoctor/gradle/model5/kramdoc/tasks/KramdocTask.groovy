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
package org.asciidoctor.gradle.model5.kramdoc.tasks

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.kramdoc.KramdocLauncher
import org.asciidoctor.gradle.model5.kramdoc.internal.DefaultKramdocConversionSettings
import org.asciidoctor.gradle.model5.kramdoc.internal.DefaultKramdocExecutionSettings
import org.asciidoctor.gradle.model5.kramdoc.publications.KramdocOutputData
import org.asciidoctor.gradle.model5.core.BaseConversionSettings
import org.asciidoctor.gradle.model5.core.tasks.AbstractBaseConversionTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import java.util.regex.Pattern

/**
 * Base task for converting Asciidoc sources into content.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class KramdocTask extends AbstractBaseConversionTask implements KramdocTaskMethods {

    private final Property<KramdocLauncher> launcher
    private final DefaultKramdocExecutionSettings exeSettings
    private final DefaultKramdocConversionSettings conversionSettings

    KramdocTask() {
        super()
        this.launcher = providerTools().property(KramdocLauncher)
        this.exeSettings = project.objects.newInstance(DefaultKramdocExecutionSettings)
        this.conversionSettings = project.objects.newInstance(DefaultKramdocConversionSettings)

        this.conversionSettings.sourceRootDir.set(determineSourceRootDir())

        inputs.property('launcher', launcher.map { it.ecosystemSignature }).optional(true)
        inputs.property('exeSettings', exeSettings.inputProperties)
        inputs.property('conversionSettings', conversionSettings.inputProperties)

        outputs.files(fsOperations().fileTree(conversionSettings.destinationDir))
    }

    /**
     * Sets the conversion launcher for this task.
     *
     * @param launcher Provider to launcher.
     */
    @Override
    void setLauncher(Provider<? extends KramdocLauncher> launcher) {
        this.launcher.set(launcher)
    }

    /**
     * Configures output data for this task.
     *
     * @param outputData Output data related to the {@code kramdown-asciidoc} process
     */
    @Override
    void setOutputData(final KramdocOutputData outputData) {
        copyResources = outputData.copyResources
        conversionSettings.tap {
            destinationDir.set(outputData.outputDir)
            sourceFormat.set(outputData.sourceFormat)
            customProperties.set(outputData.customProperties)
        }

        conversionSettings.attributes.set(outputData.attributes)

        exeSettings.tap {
            toolchainName.set(outputData.toolchainName)
            formatterName.set(outputData.formatterName)
        }
    }

    @Override
    void setFatalWarnings(Provider<Set<Pattern>> patterns) {
        conversionSettings.fatalWarnings.set(patterns)
    }

    @Override
    Provider<Directory> getOutputDir() {
        conversionSettings.destinationDir
    }

    @Override
    protected BaseConversionSettings getBaseConversionSettings() {
        this.conversionSettings
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
}
