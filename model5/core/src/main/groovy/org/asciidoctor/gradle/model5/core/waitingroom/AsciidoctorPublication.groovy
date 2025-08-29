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
package org.asciidoctor.gradle.model5.core.waitingroom


import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorExtension
import org.asciidoctor.gradle.model5.core.attributes.Attributes
import org.asciidoctor.gradle.model5.core.basedir.HasBaseDirStrategy
import org.asciidoctor.gradle.model5.core.internal.toolchains.DefaultProcessingOptions
import org.asciidoctor.gradle.model5.core.internal.LanguageFactory
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.core.internal.attributes.DefaultAttributes
import org.asciidoctor.gradle.model5.core.internal.basedir.DefaultBaseDirConfiguration
import org.asciidoctor.gradle.model5.core.basedir.BaseDirConfiguration
import org.asciidoctor.gradle.model5.core.attributes.HasAsciidoctorAttributes
import org.asciidoctor.gradle.model5.core.publications.HasAsciidoctorResources
import org.asciidoctor.gradle.model5.core.toolchains.AsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.ASCIIDOC_PATTERNS
import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.UNDERSCORE_LED_FILES

/**
 * Implementation of an asciidoc publication.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorPublication implements HasAsciidoctorAttributes, HasAsciidoctorResources,
        HasBaseDirStrategy, Named {

    final String name
    final NamedDomainObjectContainer<Language> languages

    private final ProcessingOptions processingOptions
    private final Attributes attributes
    private final ConfigCacheSafeOperations ccso
    private final Property<File> srcDir
    private final PatternSet sourceDocumentPattern
    private final PatternSet secondarySourceDocumentPattern
    private final CopySpec resourcesCopySpec
    private final BaseDirConfiguration baseDirConfiguration
    private final ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains
    private final Provider<PatternFilterable> sourcePatternProvider
    private final Provider<PatternFilterable> secondarySourcePatternProvider

    @Inject
    AsciidoctorPublication(String name, AsciidoctorExtension parent, Project project) {
        this.name = name
        this.toolchains = parent.toolchains
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.processingOptions = project.objects.newInstance(DefaultProcessingOptions)
        this.attributes = project.objects.newInstance(DefaultAttributes)
        this.srcDir = project.objects.property(File)
        this.sourceDocumentPattern = new PatternSet().exclude(UNDERSCORE_LED_FILES)
        this.secondarySourceDocumentPattern = new PatternSet()
        this.resourcesCopySpec = project.copySpec()
        this.baseDirConfiguration = project.objects.newInstance(DefaultBaseDirConfiguration)
        this.secondarySourcePatternProvider = project.provider { ->
            (PatternFilterable)(owner.secondarySourceDocumentPattern)
        }

        this.sourcePatternProvider = project.provider { ->
           final ret = owner.sourceDocumentPattern.includes.empty ?
               new PatternSet().copyFrom(owner.sourceDocumentPattern).include(ASCIIDOC_PATTERNS) :
               owner.sourceDocumentPattern
            (PatternFilterable)ret
        }

        final langFactory = new LanguageFactory(project)
        this.languages = project.objects.domainObjectContainer(
                Language, langFactory
        )
        this.languages.whenObjectAdded { Language testSet ->
            // Not sure whether further configuration would be required.
        }

        this.srcDir.set(
                ccso.fsOperations().provideProjectDir()
                        .map { new File(it, PublicationUtils.sourcePathFor(name)) }
        )

        this.attributes.add('gradle-project-name', ccso.projectTools().projectNameProvider)
        this.attributes.add('gradle-project-group', ccso.projectTools().groupProvider.orElse(''))
        this.attributes.add('gradle-project-version', ccso.projectTools().versionProvider.orElse(''))
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
     * Creates a provider for a specific language
     *
     * @param language Name of language as defined in the {@link #languages} block.
     *
     * @return A provider customised for the specific language.
     *   If the named langauge was not customised, then this is the equivalent
     *   of {@code getAttributes().getAttributeResolver()}.
     */
    Provider<Map<String, String>> attributesForLanguageResolver(String language) {
        this.attributes.attributeResolver.flatMap {
            final lang = owner.languages.findByName(language)
            lang ? lang.attributes.attributeResolver : ccso.providerTools().provider { ->
                Collections.EMPTY_MAP as Map<String, String>
            }
        }
    }

    /**
     * Direct access to processing options.
     *
     * @return Configurable {@link ProcessingOptions}.
     */
    ProcessingOptions getProcessingOptions() {
        this.processingOptions
    }

    /**
     * Configures processing options for the publication.
     *
     * @param configurator Configurator.
     */
    void processingOptions(Action<ProcessingOptions> configurator) {
        configurator.execute(this.processingOptions)
    }

    /**
     * Configures processing options for the publication.
     *
     * @param configurator Configurator.
     */
    void processingOptions(@DelegatesTo(ProcessingOptions.class) Closure<?> configurator) {
        ClosureUtils.configureItem(this.processingOptions, configurator)
    }

    /**
     * The top directory for asciidoc sources for this publication.
     *
     * @return Provider to location.
     */
    Provider<File> getSourceDir() {
        this.srcDir
    }

    /**
     * The directory where the root of the asciidoc documents will be found.
     *
     * <p>
     *   Usually this is {@code src/docs/asciidoc} or {@code src/docs/asciidoc<PUBLICATION>}.
     * </p>
     * @param srcDir New source directory
     */
    void setSourceDir(Object srcDir) {
        ccso.fsOperations().updateFileProperty(this.srcDir, srcDir)
    }

    /**
     * Configures sources.
     *
     * @param cfg Configuration closure. Is passed a {@link org.gradle.api.tasks.util.PatternSet}.
     */
    void sources(@DelegatesTo(PatternSet) Closure<?> cfg) {
        ClosureUtils.configureItem(this.sourceDocumentPattern, cfg)
    }

    /**
     * Configures sources.
     *
     * @param cfg Configuration {@link org.gradle.api.Action}. Is passed a {@link PatternSet}.
     */
    void sources(final Action<? super PatternSet> cfg) {
        cfg.execute(this.sourceDocumentPattern)
    }

    /**
     * Include source patterns.
     *
     * @param includePatterns ANT-style patterns for sources to include
     */
    void sources(String... includePatterns) {
        this.sourceDocumentPattern.include(includePatterns)
    }

    /**
     * Clears existing sources patterns.
     */
    void clearSources() {
        sourceDocumentPattern.copyFrom(new PatternSet())
    }

    /**
     * A provider of source patterns.
     *
     * @return Patterns of files to include for Asciidoc sources.
     */
    Provider<PatternFilterable> getSourcePatterns() {
        this.sourcePatternProvider
    }

    /**
     * Configures secondary sources.
     *
     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
     */
    void secondarySources(@DelegatesTo(PatternSet) Closure<?> cfg) {
        ClosureUtils.configureItem(this.secondarySourceDocumentPattern, cfg)
    }

    /**
     * Configures sources.
     *
     * @param cfg Configuration {@link Action}. Is passed a {@link PatternSet}.
     */
    void secondarySources(final Action<? super PatternSet> cfg) {
        cfg.execute(secondarySourceDocumentPattern)
    }

    /**
     * Clears any of the existing secondary soruces patterns.
     *
     * This should be used if none of the default patterns should be monitored.
     */
    void clearSecondarySources() {
        secondarySourceDocumentPattern.copyFrom(new PatternSet())
    }

    /**
     * A provider of secondary source patterns.
     *
     * @return Patterns of non-Asciidoc source files to monitor.
     */
    Provider<PatternFilterable> getSecondarySourcePatterns() {
        this.sourcePatternProvider
    }

    /**
     *  Add to the CopySpec for extra files.
     *
     * The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     */
    @Override
    void resources(Action<? super CopySpec> cfg) {
        final childSpec = ccso.fsOperations().copySpec()
        cfg.execute(childSpec)
        this.resourcesCopySpec.with(childSpec)
    }

    /**
     *  Add to the CopySpec for extra files.
     *
     * The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     */
    @Override
    void resources(@DelegatesTo(CopySpec) Closure<?> cfg) {
        final childSpec = ccso.fsOperations().copySpec()
        ClosureUtils.configureItem(childSpec, cfg)
        this.resourcesCopySpec.with(childSpec)
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
     * Provide a list of output formats along with the toolchain to produce the output.
     * <p>
     *     Referenced toolchains along with output format should be registered before calling this otherwise
     *     exceptions may be raised. A number of plugins can add toolchains and formats, so ensure that the correct
     *     plugins are applied.
     * </p>
     * @param registeredFormats Names in the format {@code ToolchainName.FormatName} i.e. {@code asciidoctorj.html5}
     */
    void outputFormats(String... registeredFormats) {
//        final pub = this
//        registeredFormats.each { fmt ->
//            final naming =  fmt.split('\\.', 2)
//            if(naming.size() != 2) {
//                throw new IncorrectOutputFormatException("${fmt} is not in the form '<ToolChain>.<OutputFormat>.")
//            }
//            try {
//                toolchains.getByName(naming[0])
//                        .registeredOutputFormatters
//                        .getByName(naming[1])
//                        .registerTasksIfAbsent(pub)
//            } catch(UnknownDomainObjectException e) {
//                throw new IncorrectOutputFormatException(
//                        "Unknown toolchain or output formatter for '${fmt}'. \n" +
//                                "Run the '${TOOLCHAIN_DISPLAY_TASK}' task to see current registrations.",
//                        e
//                )
//            }
//
//        }
    }

//    public <T extends AsciidoctorOutputFormatter> void outputFormat(
//            String format,
//            Class<T> type,
//            Action<T> configurator
//    ) {
//        outputFormats(format)
//        final naming =  format.split('\\.', 2)
//        toolchains.getByName(naming[0]).registeredOutputFormatters.getByName(naming[1])
//    }

//    public <T extends AsciidoctorOutputFormatter> void outputFormat(
//            String format,
//            Class<T> type,
//            @De
//    ) {
//
//    }

//    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
//     * of {@code outputDir} or {@code outputDir + backend}
//     *
//     * If not languages are set. these resources will be ignored.
//     *
//     * @param cfg {@link CopySpec} runConfiguration closure
//     * @param lang Language to which these resources will be applied to.
//     * @since 3.0.0
//     */
//    void resources(final String lang, Closure cfg) {
//        {
//            Closure configuration = (Closure) cfg.clone()
//            configuration.delegate = this.languageResources[lang]
//            configuration()
//        }
//    }

//    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
//     * of {@code outputDir} or {@code outputDir + backend}
//     *
//     * If not languages are set. these resources will be ignored.
//     *
//     * @param cfg {@link CopySpec} runConfiguration {@link Action}
//     * @param lang Language to which these resources will be applied to.
//     * @since 3.0.0
//     */
//    void resources(final String lang, Action<? super CopySpec> cfg) {
//        if (this.languageResources[lang] == null) {
//            this.languageResources[lang] = project.copySpec(cfg)
//        } else {
//            cfg.execute(this.languageResources[lang])
//        }
//    }

}
