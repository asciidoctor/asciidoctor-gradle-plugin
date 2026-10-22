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
package org.asciidoctor.gradle.model5.kramdoc.internal.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.kramdoc.MarkdownFormat
import org.asciidoctor.gradle.model5.kramdoc.formatters.KramdocOutputFormatter
import org.asciidoctor.gradle.model5.kramdoc.publications.KramdocOutputData
import org.asciidoctor.gradle.model5.kramdoc.publications.KramdocSourceSet
import org.asciidoctor.gradle.model5.kramdoc.toolchains.KramdocToolchain
import org.asciidoctor.gradle.model5.core.formatters.HasCustomProperties
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.util.PatternFilterable
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * Holds data regarding a publication output.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultKramdocOutputData implements KramdocOutputData {
    final String name

    private final ConfigCacheSafeOperations ccso
    private final ProjectLayout layout
    private final DirectoryProperty outDir
    private final Property<PatternFilterable> resourcesCopySpec
    private final Property<String> formatterName
    private final Property<String> toolchainName
    private final Property<MarkdownFormat> sourceFormat
    private final MapProperty<String, String> attributes
    private final MapProperty<String, String> customProperties

    @Inject
    DefaultKramdocOutputData(String name, Project tempProjectReference) {
        final objectFactory = tempProjectReference.objects

        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.layout = tempProjectReference.layout
        this.name = name
        this.outDir = objectFactory.directoryProperty()
        this.resourcesCopySpec = objectFactory.property(PatternFilterable)
        this.formatterName = objectFactory.property(String)
        this.toolchainName = objectFactory.property(String)
        this.sourceFormat = objectFactory.property(MarkdownFormat).convention(MarkdownFormat.GFM)
        this.attributes = objectFactory.mapProperty(String, String).convention([:])
        this.customProperties = objectFactory.mapProperty(String, String)
    }

    @Override
    Provider<Directory> getOutputDir() {
        this.outDir
    }

    /**
     * Whether resources should be copied.
     *
     * @return A provider to a copy spec. Can be empty if resources should not be copied to the output directory.
     */
    @Override
    Provider<PatternFilterable> getCopyResources() {
        this.resourcesCopySpec
    }

    /**
     * The actual name of the formatter, but not the alias name.
     * @return Provider to the name.
     */
    @Override
    Provider<String> getFormatterName() {
        this.formatterName
    }

    /**
     * The name of the toolchain.
     *
     * @return Provider to the name.
     */
    @Override
    Provider<String> getToolchainName() {
        this.toolchainName
    }

    @Override
    Provider<MarkdownFormat> getSourceFormat() {
        this.sourceFormat
    }

    @Override
    Provider<Map<String, String>> getCustomProperties() {
        this.customProperties
    }

    @Override
    Provider<Map<String, String>> getAttributes() {
        this.attributes
    }

    @SuppressWarnings('CouldBeSwitchStatement')
    void configureFrom(
        String publicationName,
        KramdocToolchain toolchain,
        KramdocOutputFormatter formatter,
        KramdocSourceSet sourceSet
    ) {
        final outSubdir = PublicationUtils.outputPathFor(ccso.fsOperations(), 'kramdown', publicationName, name)
        this.outDir.set(layout.buildDirectory.map { it.dir(outSubdir) })
        this.formatterName.set(formatter.name)
        this.toolchainName.set(toolchain.name)
        this.sourceFormat.set(toolchain.format)

        this.attributes.set(sourceSet.attributes.attributeResolver)

        if (formatter.copyResources) {
            this.resourcesCopySpec.set(sourceSet.resourcesPatterns)
        }

        if (formatter instanceof HasCustomProperties) {
            final templateFormatter = (HasCustomProperties) formatter
            this.customProperties.set(templateFormatter.customProperties)
        }
    }
}
