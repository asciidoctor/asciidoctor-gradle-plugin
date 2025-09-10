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
import org.asciidoctor.gradle.model5.core.publications.ExternalAsciidoctorSource
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * Implementation of {@link ExternalAsciidoctorSource}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultExternalAsciidoctorSource implements ExternalAsciidoctorSource {

    private final ListProperty<Object> tasks
    private final ConfigCacheSafeOperations ccso
    private final ConfigurableFileCollection allExternalSources
    private final Property<String> into

    @Delegate
    private final DefaultAsciidoctorSource externalSource

    @Inject
    DefaultExternalAsciidoctorSource(Project project) {
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.tasks = project.objects.listProperty(Object)
        this.into = project.objects.property(String)
        this.allExternalSources = ccso.fsOperations().emptyFileCollection()
        this.externalSource = project.objects.newInstance(DefaultAsciidoctorSource)

        allExternalSources.from(
            this.externalSource.sourceDir.map {
                ccso.fsOperations().emptyFileCollection().from(it)
            }.orElse(ccso.fsOperations().emptyFileCollection())
        )
    }

    /**
     * Tasks that are responsible for building providing external sources and resources.
     *
     * @param tasks Evaluated as per normal {@code dependsOn} rules.
     */
    @Override
    void builtBy(Object... tasks) {
        this.tasks.addAll(tasks)
    }

    /**
     * Tasks that build this external source.
     *
     * @return Provider to the list of tasks. Can be empty, but not {@code null}.
     */
    @Override
    Provider<List<Object>> getBuiltBy() {
        this.tasks
    }

    /**
     * Instead of a source directory, you can provide a file collection. This is useful when taking a configuration
     * from another subproject.
     *
     * @param fc Collection of files.
     */
    @Override
    void from(FileCollection fc) {
        this.allExternalSources.from(fc)
    }

    /**
     * Returns all of the resolved sources.
     *
     * @return Collection of external sources,
     */
    FileCollection getResolvedSources() {
        this.allExternalSources
    }

    /**
     * The subpath into which sources and resources should be copied.
     *
     * @return Optional subpath. Can be empty.
     */
    Provider<String> getInto() {
        this.into
    }

    /**
     * If the eternal source needs to be placed in a subfolder, then specify that here.
     *
     * @param path sub path in intermediate workdir and destination dir.
     */
    @Override
    void into(Object path) {
        ccso.stringTools().updateStringProperty(this.into, path)
    }
}
