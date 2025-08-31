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
package org.asciidoctor.gradle.model5.core.internal.basedir

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.basedir.BaseDirConfiguration
import org.asciidoctor.gradle.model5.core.basedir.BaseDirStrategy
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.FileSystemOperations

import javax.inject.Inject

/**
 * The default implementation for base directory configuration.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultBaseDirConfiguration implements BaseDirConfiguration {
    private final Property<BaseDirStrategy> baseDirStrategy
    private final ObjectFactory objectFactory
    private final ProjectLayout layout
    private final FileSystemOperations fsOperations

    @Inject
    DefaultBaseDirConfiguration(Project project) {
        this.objectFactory = project.objects
        this.layout = project.layout
        this.baseDirStrategy = project.objects.property(BaseDirStrategy)
        this.baseDirStrategy.set(project.objects.newInstance(BaseDirFollowSourceDir))
        this.fsOperations = ConfigCacheSafeOperations.from(project).fsOperations()
    }

    @Override
    void baseDirFollowsSourceDir() {
        this.baseDirStrategy.set(objectFactory.newInstance(BaseDirFollowSourceDir))
    }

    @Override
    void baseDirIsProjectDir() {
        this.baseDirStrategy.set(objectFactory.newInstance(BaseDirFollowsProject))
    }

    @Override
    void baseDirIsRootProjectDir() {
        this.baseDirStrategy.set(objectFactory.newInstance(BaseDirFollowsRootProject))
    }

    /**
     * The base directory starts with the source directory but needs to adjust to the parent file of each file it
     * processes.
     *
     * <p>
     *     Keep in mind that this will probably invoke a performance penalty.
     * </p>
     */
    @Override
    void baseDirFollowSourceFiles() {
        this.baseDirStrategy.set(objectFactory.newInstance(BaseDirFollowSourceFiles))
    }

    @Override
    Provider<BaseDirStrategy> getBaseDirStrategy() {
        this.baseDirStrategy
    }

    @Override
    void setBaseDir(Object f) {
        switch (f) {
            case BaseDirStrategy:
                this.baseDirStrategy.set((BaseDirStrategy) f)
                break
            case null:
                baseDirFollowsSourceDir()
                break
            default:
                this.baseDirStrategy.set(
                    objectFactory.newInstance(BaseDirIsFixedPath, layout.dir(fsOperations.provideFile(f)))
                )
        }
    }
}
