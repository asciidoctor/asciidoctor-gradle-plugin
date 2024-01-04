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
package org.asciidoctor.gradle.js.nodejs.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.js.base.AsciidoctorJSModules
import org.asciidoctor.gradle.js.base.internal.AsciidoctorJSModule
import org.asciidoctor.gradle.js.base.internal.BaseAsciidoctorJSModules
import org.gradle.api.Action
import org.ysb33r.grolifant.api.core.ProjectOperations

/** Define versions for standard AsciidoctorJS modules.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@SuppressWarnings(['ConfusingMethodName', 'ClassName'])
@CompileStatic
class AsciidoctorNodeJSModules extends BaseAsciidoctorJSModules implements AsciidoctorJSModules {

    public final static String SCOPE_ASCIIDOCTOR = 'asciidoctor'

    /**
     * Standard modules that support asciidoctor.js development.
     *
     * @param po {@link ProjectOperations} instance
     * @param versionMap Map containing Asciidoctor.js versions.
     * @since 4.0
     */
    AsciidoctorNodeJSModules(
            ProjectOperations po,
            Map<String, String> versionMap
    ) {
        super(
                po,
                Module.of(
                        po,
                        'docbook',
                        versionMap['asciidoctorjs.docbook'],
                        PackageDescriptor.of(SCOPE_ASCIIDOCTOR, 'docbook-converter'),
                )
        )
    }

    /**
     * Adds a package descriptor for a module.
     */
    static class Module extends AsciidoctorJSModule {

        private final PackageDescriptor packageIdentifier

        static Module of(
                final ProjectOperations po,
                final String name,
                final Object defaultVersion,
                final PackageDescriptor packageIdentifier
        ) {
            new Module(po, name, defaultVersion, packageIdentifier)
        }

        static Module of(
                ProjectOperations po,
                final String name,
                final Object defaultVersion,
                final PackageDescriptor packageIdentifier,
                Closure setAction
        ) {
            new Module(po, name, defaultVersion, packageIdentifier, setAction as Action<Object>)
        }

        private Module(
                final ProjectOperations po,
                final String name,
                final Object defaultVersion,
                final PackageDescriptor packageIdentifier,
                Action<Object> setAction = null
        ) {
            super(po, name, defaultVersion, setAction)
            this.packageIdentifier = packageIdentifier
        }

        /** Gets the NodeJs package descriptor.
         *
         * @return Package descriptor
         */
        PackageDescriptor getPackage() {
            this.packageIdentifier
        }
    }
}
