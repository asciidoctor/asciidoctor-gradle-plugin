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
package org.asciidoctor.gradle.model5.core.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.DocType
import org.asciidoctor.gradle.model5.core.attributes.Attributes
import org.asciidoctor.gradle.model5.core.attributes.HasAsciidoctorAttributes
import org.asciidoctor.gradle.model5.core.basedir.BaseDirConfiguration
import org.asciidoctor.gradle.model5.core.basedir.HasBaseDirStrategy
import org.asciidoctor.gradle.model5.core.internal.attributes.DefaultAttributes
import org.asciidoctor.gradle.model5.core.internal.basedir.DefaultBaseDirConfiguration
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.ASCIIDOC_PATTERNS
import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.UNDERSCORE_LED_FILES
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Defines the sources of an Asciidoctor publication.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorSourceSet implements HasBaseDirStrategy, HasAsciidoctorAttributes, HasAsciidoctorSource,
        HasAsciidoctorResources {

    private final BaseDirConfiguration baseDirConfiguration
    private final ConfigCacheSafeOperations ccso
    private final ProjectLayout layout
    private final DirectoryProperty srcDir
    private final Attributes attributes
    private final Property<DocType> doctype
    private final PatternSet sourceDocumentPattern
    private final Provider<PatternFilterable> sourcePatternProvider
    private final PatternSet resourcePatterns
    private final Provider<PatternFilterable> resourcePatternProvider
//    private final CopySpec resourcesCopySpec
//    private final PatternSet secondarySourceDocumentPattern
//    private final Provider<PatternFilterable> secondarySourcePatternProvider

    @Inject
    AsciidoctorSourceSet(String name, Project tempProjectReference) {
        final srcDirPath = PublicationUtils.sourcePathFor(name)
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.layout = tempProjectReference.layout
        this.baseDirConfiguration = tempProjectReference.objects.newInstance(DefaultBaseDirConfiguration)
        this.attributes = tempProjectReference.objects.newInstance(DefaultAttributes)
        this.doctype = tempProjectReference.objects.property(DocType)
        this.sourceDocumentPattern = new PatternSet().exclude(UNDERSCORE_LED_FILES)
        this.resourcePatterns = new PatternSet()
//        this.resourcesCopySpec = ccso.fsOperations().copySpec()
//        this.secondarySourceDocumentPattern = new PatternSet()

        this.srcDir = tempProjectReference.objects.directoryProperty().convention(
                tempProjectReference.layout.projectDirectory.dir(srcDirPath)
        )

        this.sourcePatternProvider = ccso.providerTools().provider { ->
            final ret = owner.sourceDocumentPattern.includes.empty ?
                    new PatternSet().copyFrom(owner.sourceDocumentPattern).include(ASCIIDOC_PATTERNS) :
                    owner.sourceDocumentPattern
            (PatternFilterable) ret
        }

        this.resourcePatternProvider = ccso.providerTools().provider { ->
            (PatternFilterable)(owner.resourcePatterns.includes.empty ? null : owner.resourcePatterns)
        }

        this.attributes.add('gradle-project-name', ccso.projectTools().projectNameProvider)
        this.attributes.add('gradle-project-group', ccso.projectTools().groupProvider.orElse(EMPTY))
        this.attributes.add('gradle-project-version', ccso.projectTools().versionProvider.orElse(EMPTY))
        this.attributes.add('gradle-projectdir', tempProjectReference.projectDir)
        this.attributes.add('gradle-rootdir', tempProjectReference.rootDir)
    }

    /**
     * Direct access to basedir configuration.
     *
     * @return Base directory configuration.
     */
    @Override
    BaseDirConfiguration getBaseDir() {
        this.baseDirConfiguration
    }

    /**
     * Configure the base directory.
     *
     * @param configurator Configurator
     */
    @Override
    void baseDir(Action<BaseDirConfiguration> configurator) {
        configurator.execute(this.baseDirConfiguration)
    }

    /**
     * Configure the base directory.
     *
     * @param configurator Configurator
     */
    @Override
    void baseDir(@DelegatesTo(BaseDirConfiguration.class) Closure<?> configurator) {
        ClosureUtils.configureItem(this.baseDirConfiguration, configurator)
    }

    /**
     * The top directory for asciidoc sources for this publication.
     *
     * @return Provider to location.
     */
    @Override
    Provider<Directory> getSourceDir() {
        this.srcDir
    }

    /**
     * The directory where the root of the asciidoc documents will be found.
     *
     * <p>
     *   Usually this is {@code src/docs/asciidoc} or {@code src/docs/asciidoc<PUBLICATION>}.
     * </p>
     * @param dir New source directory
     */
    @Override
    void setSourceDir(Object dir) {
        this.srcDir.set(layout.dir(ccso.fsOperations().provideFile(dir)))
    }

    /**
     * Configures sources.
     *
     * @param cfg Configuration closure. Is passed a {@link org.gradle.api.tasks.util.PatternSet}.
     */
    @Override
    void sources(@DelegatesTo(PatternSet.class) Closure<?> cfg) {
        ClosureUtils.configureItem(this.sourceDocumentPattern, cfg)
    }

    /**
     * Configures sources.
     *
     * @param cfg Configuration {@link org.gradle.api.Action}. Is passed a {@link PatternSet}.
     */
    @Override
    void sources(final Action<? super PatternSet> cfg) {
        cfg.execute(this.sourceDocumentPattern)
    }

    /**
     * Include source patterns.
     *
     * @param includePatterns ANT-style patterns for sources to include
     */
    @Override
    void sources(String... includePatterns) {
        this.sourceDocumentPattern.include(includePatterns)
    }

    /**
     * Clears existing sources patterns.
     */
    @Override
    void clearSources() {
        sourceDocumentPattern.copyFrom(new PatternSet())
    }

    /**
     * A provider of source patterns.
     *
     * @return Patterns of files to include for Asciidoc sources.
     */
    @Override
    Provider<PatternFilterable> getSourcePatterns() {
        this.sourcePatternProvider
    }

//    /**
//     * Configures secondary sources.
//     *
//     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
//     */
//    @Override
//    void secondarySources(@DelegatesTo(PatternSet.class) Closure<?> cfg) {
//        ClosureUtils.configureItem(this.secondarySourceDocumentPattern, cfg)
//    }
//
//    /**
//     * Configures sources.
//     *
//     * @param cfg Configuration {@link Action}. Is passed a {@link PatternSet}.
//     */
//    @Override
//    void secondarySources(final Action<? super PatternSet> cfg) {
//        cfg.execute(secondarySourceDocumentPattern)
//    }
//
//    /**
//     * Clears any of the existing secondary source patterns.
//     *
//     * This should be used if none of the default patterns should be monitored.
//     */
//    @Override
//    void clearSecondarySources() {
//        secondarySourceDocumentPattern.copyFrom(new PatternSet())
//    }
//
//    /**
//     * A provider of secondary source patterns.
//     *
//     * @return Patterns of non-Asciidoc source files to monitor.
//     */
//    @Override
//    Provider<PatternFilterable> getSecondarySourcePatterns() {
//        this.sourcePatternProvider
//    }
//    /**
//     *  Add to the CopySpec for extra files.
//     *
//     * The destination of these files will always have a parent directory
//     * of {@code outputDir} or {@code outputDir + backend}
//     *
//     * @param cfg {@link CopySpec} runConfiguration {@link Action}
//     */
//    @Override
//    void resources(Action<? super CopySpec> cfg) {
//        final childSpec = ccso.fsOperations().copySpec()
//        cfg.execute(childSpec)
//        this.resourcesCopySpec.with(childSpec)
//    }
//
//    /**
//     *  Add to the CopySpec for extra files.
//     *
//     * The destination of these files will always have a parent directory
//     * of {@code outputDir} or {@code outputDir + backend}
//     *
//     * @param cfg {@link CopySpec} runConfiguration {@link Action}
//     */
//    @Override
//    void resources(@DelegatesTo(CopySpec.class) Closure<?> cfg) {
//        final childSpec = ccso.fsOperations().copySpec()
//        ClosureUtils.configureItem(childSpec, cfg)
//        this.resourcesCopySpec.with(childSpec)
//    }
    /**
     * Adds these patterns that are relative to the source directory or the intermediate source directory.
     *
     * <p>
     *     The {@code resources} method can be called many times, but at least one should have an {@code include}
     *     pattern, otherwise resources will not be copied.
     *
     *     If the output formatter does not require resources, then they will not be copied either.
     * </p>
     *
     * @param cfg {@link PatternFilterable} instance that can be configured.
     */
    @Override
    void resources(Action<? super PatternFilterable> cfg) {
        cfg.execute(this.resourcePatterns)
    }

    /**
     * Adds these patterns that are relative to the source directory or the intermediate source directory.
     *
     * <p>
     *     The {@code resources} method can be called many times, but at least one should have an {@code include}
     *     pattern, otherwise resources will not be coped.
     *
     *     If the output formatter does not require resources, then they will not be copied either.
     * </p>
     *
     * @param cfg A closure that can configure a {@link PatternFilterable} instance.
     */
    @Override
    void resources(@DelegatesTo(PatternFilterable.class) Closure<?> cfg) {
        ClosureUtils.configureItem(this.resourcePatterns, cfg)
    }

    /**
     * Patterns that can be added to a copy spec for copying resources to a target directory.
     *
     * @return Copy specification
     */
    Provider<PatternFilterable> getResourcesPatterns() {
        this.resourcePatternProvider
    }

    /**
     * Configures the attributes for this publication
     *
     * @param configurator Configurator
     */
    @Override
    void attributes(Action<Attributes> configurator) {
        configurator.execute(this.attributes)
    }

    @Override
    void attributes(@DelegatesTo(Attributes.class) Closure configurator) {
        ClosureUtils.configureItem(this.attributes, configurator)
    }

    /**
     * Direct access to the attributes for this publication.
     *
     * @return Attributes
     */
    @Override
    Attributes getAttributes() {
        this.attributes
    }

    /**
     * The document type.
     *
     * @return A predefined document type. Can be empty.
     */
    Provider<DocType> getDocType() {
        this.doctype
    }

    /**
     * Sets the document type.
     *
     * @param mode The document type as a case-insesnitive string
     */
    void setDocType(String mode) {
        docType = DocType.from(mode)
    }

    /**
     * Sets the document type.
     *
     * @param mode The document type as a case-insensitive string
     */
    void setDocType(DocType mode) {
        this.doctype.set(mode)
    }
}
