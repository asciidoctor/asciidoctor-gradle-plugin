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
package org.asciidoctor.gradle.kramdown.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.kramdown.internal.publications.DefaultKramdownSource
import org.asciidoctor.gradle.model5.core.attributes.Attributes
import org.asciidoctor.gradle.model5.core.attributes.HasAsciidoctorAttributes
import org.asciidoctor.gradle.model5.core.internal.attributes.DefaultAttributes
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultExternalSource
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultProvidedExternalSourceSet
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultProvidedExternalSources
import org.asciidoctor.gradle.model5.core.publications.ExternalSource
import org.asciidoctor.gradle.model5.core.publications.HasExternalSources
import org.asciidoctor.gradle.model5.core.publications.HasSourceFiles
import org.asciidoctor.gradle.model5.core.publications.ProvidedExternalSources
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * Defines a Markdown source set.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class KramdownSourceSet implements HasSourceFiles, HasExternalSources, HasAsciidoctorAttributes {
    public static final String ATTR_IMAGES_DIR = 'imagesdir'
    final String name
    private final ConfigCacheSafeOperations ccso
    private final ListProperty<DefaultExternalSource> externalSources
    private final Property<DuplicatesStrategy> duplicatesStrategy
    private final Provider<List<Object>> externalBuiltBy
    private final Provider<? extends ProvidedExternalSources> externalSourcesTransformed
    private final Attributes attributes

    @Delegate
    private final DefaultKramdownSource localSource

    @Inject
    KramdownSourceSet(String name, Project tempProjectReference) {
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.name = name
        this.localSource = ccso.providerTools().newInstance(DefaultKramdownSource)
        this.externalSources = ccso.providerTools().listProperty(DefaultExternalSource)
        this.attributes = ccso.providerTools().newInstance(DefaultAttributes)
        this.duplicatesStrategy = ccso.providerTools().property(DuplicatesStrategy)
            .convention(DuplicatesStrategy.FAIL)
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
    }

    /**
     * Sets the {@code imagesdir} attribute in the AsciiDoc document header and remove the value from the start of
     * image paths.
     *
     * @param path Anything convertible to a string.
     */
    void setImagesDir(Object path) {
        attributes.add(ATTR_IMAGES_DIR, ccso.stringTools().provideString(path))
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
    void externalSource(Action<ExternalSource> configurator) {
        final src = ccso.providerTools().newInstance(DefaultExternalSource)
        configurator.execute(src)
        externalSources.add(src)
    }

    @Override
    void setDuplicatesStrategy(DuplicatesStrategy strategy) {
        this.duplicatesStrategy.set(strategy)
    }

    @Override
    Provider<List<Object>> getExternalSourcesBuiltBy() {
        this.externalBuiltBy
    }

    @Override
    Provider<ProvidedExternalSources> getExternalSources() {
        this.externalSourcesTransformed
    }

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
}
