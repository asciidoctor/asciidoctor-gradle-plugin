/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.asciidoctor.gradle.base.basedir.BaseDirIsNull;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The attribute methods all Asciidoctor conversion tasks will offer.
 *
 * @since 4.0
 *
 * @author Schalk W. Cronjé
 */
public interface AsciidoctorTaskAttributes {
     /**
     * Shortcut method for obtaining attributes.
     *
     * In most implementations this will just access the {@code getAttributes} method
     * on the appropriate task extension derived from {@link AbstractImplementationEngineExtension}
     *
     * @return Access to attributes hashmap
     */
    @Input
    Map<String, Object> getAttributes();

    /**
     * Shortcut method to apply a new set of Asciidoctor attributes, clearing any attributes previously set.
     *
     * In most implementations this will just access the {@code setAttributes} method
     * on the appropriate task extension derived from {@link AbstractImplementationEngineExtension}
     *
     * @param m Map with new options
     */
    void setAttributes(Map<String, Object> m);

    /**
     * Shortcut method to add additional asciidoctor attributes.
     *
     * In most implementations this will just access the {@code attributes} method
     * on the appropriate task extension derived from {@link AbstractImplementationEngineExtension}
     *
     * @param m Map with new options
     */
    void attributes(Map<String, Object> m);

    /**
     * Shortcut method to access additional providers of attributes.
     *
     * In most implementations this will just access the {@code getAttributeProviders} method
     * on the appropriate task extension derived from {@link AbstractImplementationEngineExtension}
     *
     * @return List of attribute providers.
     */
    @Internal
    List<AsciidoctorAttributeProvider> getAttributeProviders();
}
