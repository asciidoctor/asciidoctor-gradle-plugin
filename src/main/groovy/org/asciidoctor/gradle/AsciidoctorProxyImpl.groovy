/*
 * Copyright 2013-2018 the original author or authors.
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
package org.asciidoctor.gradle

/**
 * @author andres Almiray
 */
class AsciidoctorProxyImpl implements AsciidoctorProxy {
    def delegate
    def extensionRegistry

    @Override
    String renderFile(File filename, Map<String, Object> options) {
        delegate.renderFile(filename, options)
    }

    @Override
    void requireLibrary(String... requiredLibraries) {
        delegate.requireLibrary(requiredLibraries)
    }

    @Override
    void registerExtensions(List<Object> extensions) {
        extensions.each { extensionRegistry.extensions(it) }
        extensionRegistry.registerTo(delegate)
    }

    @Override
    void unregisterAllExtensions() {
        delegate.unregisterAllExtensions()
    }
}
