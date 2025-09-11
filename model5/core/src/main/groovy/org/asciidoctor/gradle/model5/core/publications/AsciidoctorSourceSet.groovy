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
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultAsciidoctorSource
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultExternalAsciidoctorSource
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultProvidedExternalSourceSet
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultProvidedExternalSources
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject
import java.util.regex.Pattern

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
    private final ObjectFactory objectFactory
    private final ProjectLayout layout
    private final Attributes attributes
    private final Property<DocType> doctype
    private final SetProperty<Pattern> fatalWarningPatterns
    private final Property<DuplicatesStrategy> duplicatesStrategy
    private final ListProperty<DefaultExternalAsciidoctorSource> externalSources
    private final Provider<List<Object>> externalBuiltBy
    private final Provider<? extends ProvidedExternalSources> externalSourcesTransformed

    @Delegate
    private final DefaultAsciidoctorSource localSource

    @Inject
    AsciidoctorSourceSet(String name, Project tempProjectReference) {
        final srcDirPath = PublicationUtils.sourcePathFor(name)
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.layout = tempProjectReference.layout
        this.objectFactory = tempProjectReference.objects
        this.localSource = objectFactory.newInstance(DefaultAsciidoctorSource)
        this.externalSources = objectFactory.listProperty(DefaultExternalAsciidoctorSource)
        this.baseDirConfiguration = tempProjectReference.objects.newInstance(DefaultBaseDirConfiguration)
        this.attributes = tempProjectReference.objects.newInstance(DefaultAttributes)
        this.doctype = tempProjectReference.objects.property(DocType)
        this.fatalWarningPatterns = tempProjectReference.objects.setProperty(Pattern)
        this.duplicatesStrategy = tempProjectReference.objects.property(DuplicatesStrategy)
            .convention(DuplicatesStrategy.FAIL)

        sourceDir = tempProjectReference.layout.projectDirectory.dir(srcDirPath)

        this.externalBuiltBy = externalSources.map { list ->
            list*.builtBy*.get().flatten() as List<Object>
        }

        final listProvider = externalSources.map { list ->
            list.collect {
                new DefaultProvidedExternalSourceSet(it)
            }
        }

        this.externalSourcesTransformed = ccso.providerTools().provider { ->
            new DefaultProvidedExternalSources(listProvider, owner.duplicatesStrategy)
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
    void baseDir(@DelegatesTo(BaseDirConfiguration) Closure<?> configurator) {
        ClosureUtils.configureItem(this.baseDirConfiguration, configurator)
    }

    /**
     * Adds an external source which is is a supplier of AsciiDoc source and
     * related resources
     *
     * <p>
     *     Once an external source is added, it will result in all local sources and external sources being copied to
     *     an intermediate folder before processing starts.
     * </p>
     * @param configurator Configure the external source.
     */
    void externalSource(Action<ExternalAsciidoctorSource> configurator) {
        final src = objectFactory.newInstance(DefaultExternalAsciidoctorSource)
        configurator.execute(src)
        externalSources.add(src)
    }

    /**
     * Defines how duplicates between local and external sources are managed.
     *
     * @param strategy Instance of {@link DuplicatesStrategy}.
     */
    void setDuplicatesStrategy(DuplicatesStrategy strategy) {
        this.duplicatesStrategy.set(strategy)
    }

    /**
     * A list of all the tasks that build the external sources.
     *
     * @return Provider to list. Can be empty, but never {@code null}.
     */
    Provider<List<Object>> getExternalSourcesBuiltBy() {
        this.externalBuiltBy
    }

    /**
     * All external sources.
     *
     * @return Provider to all defined external sources.
     */
    Provider<ProvidedExternalSources> getExternalSources() {
        this.externalSourcesTransformed
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
    void attributes(@DelegatesTo(Attributes) Closure configurator) {
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

    /**
     * Warnings from {@code asciidoctorj} log messages that should be treating as fatal errors.
     *
     * @param patterns Anything convertible to a pattern using
     * {@link org.ysb33r.grolifant5.api.core.StringTools#patternize}
     */
    void fatalWarnings(Object... patterns) {
        this.fatalWarningPatterns.addAll {
            ccso.providerTools().provider { ->
                ccso.stringTools().patternize(patterns.toList())
            }
        }
    }

    /**
     * Warnings from Asciidoctor log messages that should be treating as fatal errors.
     *
     * @param patterns Anything convertible to a pattern using
     * {@link org.ysb33r.grolifant5.api.core.StringTools#patternize}
     */
    void fatalWarnings(Iterable<?> patterns) {
        this.fatalWarningPatterns.addAll {
            ccso.providerTools().provider { ->
                ccso.stringTools().patternize(patterns.toList())
            }
        }
    }

    /**
     * Indicates that missing includes will fail the conversion process.
     */
    void missingIncludesAreFatal() {
        this.fatalWarningPatterns.add(~/include file not found/)
    }

    /**
     * List of patterns that will be checked against log messages.
     *
     * @return Provider to a set of patterns.
     */
    Provider<Set<Pattern>> getFatalWarnings() {
        this.fatalWarningPatterns
    }
}
