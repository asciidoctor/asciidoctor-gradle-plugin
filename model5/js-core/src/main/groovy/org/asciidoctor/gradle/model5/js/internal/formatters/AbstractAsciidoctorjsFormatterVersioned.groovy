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
package org.asciidoctor.gradle.model5.js.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsOutputFormatterVersioned
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty

@CompileStatic
abstract class AbstractAsciidoctorjsFormatterVersioned extends AbstractAsciidoctorjsFormatter
        implements AsciidoctorjsOutputFormatterVersioned {

    protected final Property<String> moduleVersion

    @Override
    void useVersion(Object ver) {
        ccso.stringTools().updateStringProperty(this.moduleVersion, ver)
    }

    /**
     *
     * @param name Name of the output formatter.
     * @param backendName Name of the backend.
     * @param scope Scope of the component. (Can be null).
     * @param componentName Name of the component.
     * @param componentDefaultVersion THe default version of the component.
     * @param tc The toolchain the formatter is attached to.
     * @param tempProjectReference A temporary reference to a {@link Project} instance.
     */
    protected AbstractAsciidoctorjsFormatterVersioned(
            String name,
            String backendName,
            String scope,
            String componentName,
            Provider<String> componentDefaultVersion,
            AsciidoctorjsToolchain tc,
            Project tempProjectReference
    ) {
        super(name, backendName, tc, tempProjectReference)
        this.moduleVersion = tempProjectReference.objects.property(String).convention(componentDefaultVersion)
        packageRequires.add(scope ? "@${scope}/${componentName}".toString() : componentName)
        tc.usePackage(scope, componentName, moduleVersion)
    }
}
