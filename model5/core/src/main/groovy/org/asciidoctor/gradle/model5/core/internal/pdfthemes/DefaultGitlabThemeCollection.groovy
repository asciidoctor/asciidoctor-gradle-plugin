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
package org.asciidoctor.gradle.model5.core.internal.pdfthemes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.pdfthemes.GitlabThemeCollection
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.StringTools
import org.ysb33r.grolifant5.api.core.git.GitLabArchive
import org.ysb33r.grolifant5.api.core.git.GitRepoArchiveDownloader

import javax.inject.Inject

import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Implementation of a PDF theme residing on Gitlab.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultGitlabThemeCollection implements GitlabThemeCollection {
    final String name
    final Provider<Directory> themeDir
    private final GitLabArchive archive
    private final GitRepoArchiveDownloader downloader
    private final Property<String> relativePath
    private final StringTools stringTools

    @Inject
    DefaultGitlabThemeCollection(String name, Project tempProjectReference) {
        this.name = name
        this.stringTools = ConfigCacheSafeOperations.from(tempProjectReference).stringTools()
        this.archive = new GitLabArchive(tempProjectReference)
        this.downloader = tempProjectReference.objects.newInstance(GitRepoArchiveDownloader, this.archive)
        this.relativePath = tempProjectReference.objects.property(String).convention(EMPTY)
        this.themeDir = ConfigCacheSafeOperations.from(tempProjectReference).fsOperations()
                .provideDirectory(this.downloader.archiveRootProvider).zip(relativePath) { dir, path ->
            path ? dir.dir(path) : dir
        }
    }

    /**
     * The relative path to the themes directory inside the downloaded package.
     *
     * @param pathInRepo Relative path. Anything convertible to a string.
     */
    @Override
    void setPathInRepo(Object pathInRepo) {
        stringTools.updateStringProperty(this.relativePath, pathInRepo)
    }

    /**
     * Configure a location to download the theme from.
     *
     * @param configurator Configurator.
     */
    @Override
    void from(Action<GitLabArchive> configurator) {
        configurator.execute(this.archive)
    }

    /**
     * Configure a location to download the theme from.
     *
     * @param configurator Configurator.
     */
    @Override
    void from(@DelegatesTo(GitLabArchive.class) Closure<?> configurator) {
        ClosureUtils.configureItem(this.archive, configurator)
    }

    static class Factory implements NamedDomainObjectFactory<GitlabThemeCollection> {
        private final ObjectFactory objectFactory

        @Inject
        Factory(ObjectFactory objectFactory) {
            this.objectFactory = objectFactory
        }

        /**
         * Creates a new object with the given name.
         *
         * @param name The name
         * @return The object.
         */
        @Override
        GitlabThemeCollection create(String name) {
            objectFactory.newInstance(DefaultGitlabThemeCollection, name)
        }
    }
}
