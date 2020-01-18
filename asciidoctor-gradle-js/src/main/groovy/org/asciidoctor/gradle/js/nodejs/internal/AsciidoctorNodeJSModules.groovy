/*
 * Copyright 2013-2019 the original author or authors.
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
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSExtension
import org.gradle.api.Action

import static org.asciidoctor.gradle.js.base.AbstractAsciidoctorJSExtension.DEFAULT_DOCBOOK_VERSION
import static org.asciidoctor.gradle.js.nodejs.AsciidoctorJSExtension.PACKAGE_DOCBOOK

/** Define versions for standard AsciidoctorJS modules.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@SuppressWarnings(['ConfusingMethodName', 'ClassName'])
@CompileStatic
class AsciidoctorNodeJSModules extends BaseAsciidoctorJSModules implements AsciidoctorJSModules {

    /** Creates a module definition that is attached to a specific asciidoctorjs
     * extension.
     *
     * @param asciidoctorjs Extension that this module is attached to.
     */
    AsciidoctorNodeJSModules(AsciidoctorJSExtension asciidoctorjs) {
        super(
            Module.of(DEFAULT_DOCBOOK_VERSION, PACKAGE_DOCBOOK)
        )
    }

    /** Adds a package descriptor for a module
     *
     */
    static class Module extends AsciidoctorJSModule {

        private final PackageDescriptor packageIdentifier

        static Module of(final Object defaultVersion, final PackageDescriptor packageIdentifier) {
            new Module(defaultVersion, packageIdentifier)
        }

        static Module of(final Object defaultVersion, final PackageDescriptor packageIdentifier, Closure setAction) {
            new Module(defaultVersion, packageIdentifier, setAction as Action<Object>)
        }

        private Module(
            final Object defaultVersion,
            final PackageDescriptor packageIdentifier,
            Action<Object> setAction = null
        ) {
            super(defaultVersion, setAction)
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
