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
package org.asciidoctor.gradle.model5.core.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.toolchains.ProcessingOptions
import org.asciidoctor.gradle.model5.core.SafeMode
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 * Implementation of {@link ProcessingOptions}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultProcessingOptions implements ProcessingOptions {

    private final Property<SafeMode> safeModeProperty
    private final Property<Boolean> logDocs

    @Inject
    DefaultProcessingOptions(Project project) {
        this.safeModeProperty = project.objects.property(SafeMode)
        this.safeModeProperty.set(SafeMode.UNSAFE)

        this.logDocs = project.objects.property(Boolean)
        this.logDocs.set(false)
    }

    /**
     * Returns the Asciidoctor SafeMode under which a conversion will be run.
     *
     * @return Asciidoctor Safe Mode
     */
    @Override
    Provider<SafeMode> getSafeMode() {
        this.safeModeProperty
    }

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode An instance of Asciidoctor SafeMode.
     */
    @Override
    void setSafeMode(SafeMode mode) {
        this.safeModeProperty.set(mode)
    }

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A valid integer representing a Safe Mode
     */
    @Override
    void setSafeMode(int mode) {
        this.safeModeProperty.set(SafeMode.from(mode))
    }

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A valid string representing a Safe Mode
     */
    @Override
    void setSafeMode(String mode) {
        this.safeModeProperty.set(SafeMode.valueOf(mode.toUpperCase(Locale.US)))
    }

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A provider representing a Safe Mode
     */
    @Override
    void setSafeMode(Provider<SafeMode> mode) {
        this.safeModeProperty.set(mode)
    }

    /**
     * Whether documents should be logged as they are processed.
     *
     * @param flag {@code true} when documents should be logged.
     */
    @Override
    void setLogDocuments(boolean flag) {
        this.logDocs.set(flag)
    }

    /**
     * Whether document should be logged
     *
     * @return {@code true} when documents should be logged.
     */
    @Override
    Provider<Boolean> isLogDocuments() {
        this.logDocs
    }
}
