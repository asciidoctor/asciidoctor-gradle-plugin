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
package org.asciidoctor.gradle.model5.js.internal.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.js.extensions.AsciidoctorjsGenericExtension
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * Implementation of {@link AsciidoctorjsGenericExtension}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjsGenericExtension implements AsciidoctorjsGenericExtension {

    static class Factory extends AbstractFactory implements NamedDomainObjectFactory<AsciidoctorjsGenericExtension> {

        @Inject
        Factory(AsciidoctorjsToolchain toolchain, Project project) {
            super(toolchain, project)
        }

        /**
         * Creates a new object with the given name.
         *
         * @param name The name
         * @return The object.
         */
        @Override
        AsciidoctorjsGenericExtension create(String name) {
            objectFactory.newInstance(DefaultAsciidoctorjsGenericExtension, name, toolchain)
        }
    }

    final String name
    final FileCollection classpath = null

    private final ConfigCacheSafeOperations ccso
    private final ObjectFactory objectFactory
    private final AsciidoctorjsToolchain toolchain
    private final MapProperty<String, Object> attributes
    private final SetProperty<String> requires

    @Inject
    DefaultAsciidoctorjsGenericExtension(String name, AsciidoctorjsToolchain tc, Project tempProjectReference) {
        this.name = name
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.objectFactory = tempProjectReference.objects
        this.toolchain = tc
        this.attributes = objectFactory.mapProperty(String, Object)
        this.requires = objectFactory.setProperty(String)
    }

    @Override
    void usePackage(String scope, String moduleName, Object version) {
        toolchain.usePackage(scope, moduleName, ccso.stringTools().provideString(version))
    }

    @Override
    void requires(String... reqs) {
        this.requires.addAll(reqs)
    }

    @Override
    void attributes(Map<String, ?> attrs) {
        this.attributes.putAll(attrs)
    }

    @Override
    String getDisplayType() {
        AsciidoctorjsGenericExtension.canonicalName
    }

    @Override
    Provider<Set<String>> getRequires() {
        this.requires
    }

    @Override
    Provider<Map<String, Object>> getAttributeProvider() {
        this.attributes
    }
}
