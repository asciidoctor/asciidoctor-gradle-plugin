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
import org.asciidoctor.gradle.model5.core.pdfthemes.LocalThemeCollection
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.FileSystemOperations

import javax.inject.Inject

/**
 * Implementation of a local PDF theme.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultLocalThemeCollection implements LocalThemeCollection {
    final String name
    private final DirectoryProperty dir
    private final FileSystemOperations fsOperations

    @Inject
    DefaultLocalThemeCollection(String name, Project tempProjectReference) {
        this.name = name
        this.fsOperations = ConfigCacheSafeOperations.from(tempProjectReference).fsOperations()
        this.dir = tempProjectReference.objects.directoryProperty()
    }
    /**
     * Define the directory where the theme is located.
     *
     * @param dir Anything convertible to a file.
     */
    @Override
    void setThemeDir(Object dir) {
        fsOperations.updateDirectoryProperty(this.dir, dir)
    }

    /**
     * Provider to where the theme is located.
     *
     * @return Provider to a {@link Directory}
     */
    @Override
    Provider<Directory> getThemeDir() {
        this.dir
    }

    static class Factory implements NamedDomainObjectFactory<LocalThemeCollection> {
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
        LocalThemeCollection create(String name) {
            objectFactory.newInstance(DefaultLocalThemeCollection, name)
        }
    }
}
