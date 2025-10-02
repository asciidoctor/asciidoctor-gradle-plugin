/*
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.model5.jvm.internal.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjExtension
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

/**
 * Base class for implementing {@code asciidoctorj} extensions.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
abstract class AbstractAsciidoctorjExtension implements AsciidoctorjExtension {

    final String name
    protected final ConfigCacheSafeOperations ccso
    protected final ObjectFactory objectFactory
    protected final AsciidoctorjToolchain toolchain
    protected final SetProperty<String> packageRequires
    protected final MapProperty<String, Object> attributes
    protected final ConfigurableFileCollection extensionClasspath

    @Override
    Provider<Set<String>> getRequires() {
        this.packageRequires
    }

    @Override
    Provider<Map<String, Object>> getAttributeProvider() {
        this.attributes
    }

    /**
     * Additional items to add to the classpath when a conversion involving the output formatter is executed.
     *
     * <p>
     *     The classpath is empty by default.
     * </p>
     *
     * @return Classpath. Can be {@code null} to indicate that the formatter does not support additional classpath.
     */
    @Override
    FileCollection getClasspath() {
        this.extensionClasspath
    }

    @Override
    String getDisplayType() {
        dslType.canonicalName
    }

    protected AbstractAsciidoctorjExtension(String name, AsciidoctorjToolchain tc, Project project) {
        this.name = name
        this.toolchain = tc
        this.objectFactory = project.objects
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.packageRequires = project.objects.setProperty(String)
        this.attributes = project.objects.mapProperty(String, Object)
        this.extensionClasspath = ccso.fsOperations().emptyFileCollection()
    }

    /**
     * The type that this implements and which should be displayed.
     *
     * @return A type that needs to be displayed.
     */
    abstract protected Class<?> getDslType()
}
