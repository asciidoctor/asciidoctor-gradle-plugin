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
import org.asciidoctor.gradle.model5.core.basedir.BaseDirStrategy
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 * The base directory is the source directory. WHere an intermediate work directory is used, it will the work directory.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class BaseDirFollowSourceDir implements BaseDirStrategy {

    protected final Property<Boolean> adjustBaseDir

    @Inject
    BaseDirFollowSourceDir(ObjectFactory objectFactory) {
        adjustBaseDir = objectFactory.property(Boolean).convention(false)
    }

    @Override
    Provider<Directory> getBaseDir(Provider<Directory> srcDir) {
        srcDir
    }

    @Override
    Provider<Directory> getBaseDir(Provider<Directory> srcDir, String lang) {
        srcDir
    }

    /**
     * Whether to adjust the base directory per file.
     *
     * @return A provider to the mode.
     */
    @Override
    Provider<Boolean> getAdjustBaseDirPerFile() {
        this.adjustBaseDir
    }
}
