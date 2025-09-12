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
package org.asciidoctor.gradle.model5.js.internal.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.js.extensions.AsciidoctorjsExtensionVersioned
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

/**
 * Base class for implementing {@code asciidoctor.js} extensions where the version of a component can be set.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
abstract class AbstractAsciidoctorjsExtensionVersioned implements AsciidoctorjsExtensionVersioned {

    final String name
    final FileCollection classpath = null
    protected final Property<String> moduleVersion
    protected final ConfigCacheSafeOperations ccso
    protected final AsciidoctorjsToolchain toolchain

    /**
     * Can be modified by derived classes when additional requires are needed.
     */
    protected final SetProperty<String> packageRequires

    /**
     * Can be modified by derived classes when attributes need to be made available.
     */
    protected final MapProperty<String, Object> attributes

    @Override
    void useVersion(Object ver) {
        ccso.stringTools().updateStringProperty(this.moduleVersion, ver)
    }

    /**
     * A list of {@code requires} that a component places on the associated toolchain.
     *
     * @return List of {@code requires}. Can be empty, but never {@code null}.
     */
    @Override
    Provider<Set<String>> getRequires() {
        this.packageRequires
    }

    /**
     * C-tor for a versioned output formatter.
     *
     * @param name Name of the output formatter.
     * @param scope Scope of the component. (Can be null).
     * @param componentName Name of the component.
     * @param componentDefaultVersion THe default version of the component.
     * @param tc The toolchain the formatter is attached to.
     * @param project A temporary reference to a {@link Project} instance.
     */
    @SuppressWarnings('ParameterCount')
    protected AbstractAsciidoctorjsExtensionVersioned(
        String name,
        String scope,
        String componentName,
        Provider<String> componentDefaultVersion,
        AsciidoctorjsToolchain tc,
        Project project
    ) {
        this.name = name
        this.toolchain = tc
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.moduleVersion = project.objects.property(String).convention(componentDefaultVersion)
        this.packageRequires = project.objects.setProperty(String)
        this.attributes = project.objects.mapProperty(String, Object)
        packageRequires.add(scope ? "@${scope}/${componentName}".toString() : componentName)

        tc.usePackage(scope, componentName, moduleVersion)
    }
}
