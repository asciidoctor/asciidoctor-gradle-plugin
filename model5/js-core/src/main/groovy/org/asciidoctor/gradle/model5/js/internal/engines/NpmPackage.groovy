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
package org.asciidoctor.gradle.model5.js.internal.engines

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.provider.Provider
import org.ysb33r.gradle.nodejs.NpmPackageDescriptor

/**
 * An internal implementation for managing NPM packages inside the engine.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class NpmPackage implements NpmPackageDescriptor {
    final String packageName
    final String scope
    private final Provider<String> versionProvider

    NpmPackage(final String scope, final String packageName, final Provider<String> ver) {
        if (packageName == null) {
            throw new GradleException('Cannot have a null package name')
        }
        this.packageName = packageName
        this.scope = scope
        this.versionProvider = ver.orElse('latest')
    }

    /**
     * Name of NPM tag
     *
     * @return NPM tag (or {@code null} if not defined).
     */
    @Override
    String getTagName() {
        this.versionProvider.get()
    }

    @Override
    String toString() {
        npmPackageCoordinates
    }

    /**
     * Returns a string that can be passed for installation purposes.
     *
     * @return Package name
     *
     * @since 2.2
     */
    @Override
    String getNpmPackageCoordinates() {
        scope ? "@${scope}/${packageName}@${tagName}" : "${packageName}@${tagName}"
    }
}
