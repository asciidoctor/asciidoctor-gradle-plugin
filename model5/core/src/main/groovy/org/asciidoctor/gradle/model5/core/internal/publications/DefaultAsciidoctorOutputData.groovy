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
package org.asciidoctor.gradle.model5.core.internal.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.core.DocType
import org.asciidoctor.gradle.model5.core.formatters.AsciidoctorOutputFormatter
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorOutputData
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorSourceSet
import org.asciidoctor.gradle.model5.core.toolchains.AsciidoctorToolchain
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.util.PatternFilterable
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * Holds data regarding a publication output.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorOutputData implements AsciidoctorOutputData {
    final String name

    private final ConfigCacheSafeOperations ccso
    private final ProjectLayout layout
    private final DirectoryProperty outDir
    private final Property<AsciidoctorNamedBackend> backend
    private final Property<PatternFilterable> resourcesCopySpec
    private final Property<DocType> docType
    private final SetProperty<String> moduleRequires

    @Inject
    DefaultAsciidoctorOutputData(String name, Project tempProjectReference) {
        final objectFactory = tempProjectReference.objects

        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.layout = tempProjectReference.layout
        this.name = name
        this.outDir = objectFactory.directoryProperty()
        this.backend = objectFactory.property(AsciidoctorNamedBackend)
        this.resourcesCopySpec = objectFactory.property(PatternFilterable)
        this.docType = objectFactory.property(DocType)
        this.moduleRequires = objectFactory.setProperty(String)
    }

    @Override
    Provider<Directory> getOutputDir() {
        this.outDir
    }

    /**
     * The named backend for this formatter.
     *
     * @return Provider to the named backend.
     */
    @Override
    Provider<AsciidoctorNamedBackend> getBackend() {
        this.backend
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
     * Get the documentation type that needs to be based to the engine at run time.
     *
     * @return Documentation type. Can be empty.
     */
    @Override
    Provider<DocType> getDocType() {
        this.docType
    }

    /**
     * List modules which need to be explicitly called out as being required.
     *
     * @return Provider to a list. Can be empty, but never {@code null}.
     */
    @Override
    Provider<Set<String>> getModuleRequires() {
        this.moduleRequires
    }

    void configureFrom(
            String publicationName,
            AsciidoctorToolchain toolchain,
            AsciidoctorOutputFormatter formatter,
            AsciidoctorSourceSet sourceSet
    ) {
        final outSubdir = PublicationUtils.outputPathFor(ccso.fsOperations(), publicationName, name)
        this.outDir.set(layout.buildDirectory.map { it.dir(outSubdir) })
        this.backend.set(formatter.backend.map { AsciidoctorNamedBackend.of(owner.name, it.backend) })

        if (formatter.copyResources) {
            this.resourcesCopySpec.set(sourceSet.resourcesPatterns)
        }
        
        if(formatter.enforcedDocType.present) {
            this.docType.set(formatter.enforcedDocType.get())
        } else {
            this.docType.set(sourceSet.docType)
        }

        this.moduleRequires.set(formatter.requires)
    }
}
