/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.js.nodejs.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSNodeExtension
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSNpmExtension
import org.asciidoctor.gradle.js.nodejs.internal.PackageDescriptor
import org.gradle.api.Project
import org.gradle.api.artifacts.SelfResolvingDependency
import org.ysb33r.gradle.nodejs.dependencies.npm.NpmSelfResolvingDependency

/** A factory class for creating NPM self resolving dependencies in a Gradle context.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
class NodeJSDependencyFactory {
    private final Project project
    private final AsciidoctorJSNodeExtension nodejs
    private final AsciidoctorJSNpmExtension npm

    /** Instantiates a factory for a specific context.
     *
     * @param project The GRadle project for which is is done.
     * @param nodejs The NodeJS extension which is being used for this operation.
     * @param npm The NPM extension that is being used for this operation.
     */
    NodeJSDependencyFactory(
            Project project,
            AsciidoctorJSNodeExtension nodejs,
            AsciidoctorJSNpmExtension npm
    ) {
        this.project = project
        this.nodejs = nodejs
        this.npm = npm
    }

    /** Create a NPM-based self-resolving dependency.
     *
     * @param pkg Description of the NPM package
     * @param version Version (tag) of the NPM package.
     * @return A Gradle-style resolvable dependency.
     */
    @SuppressWarnings('FactoryMethodName')
    SelfResolvingDependency createDependency(final PackageDescriptor pkg, final String version) {
        createDependency(pkg.name, version, pkg.scope)
    }

    /** Create a NPM-based self-resolving dependency.
     *
     * This version allows for additional system search paths to be added in case
     * the package will require it during the installation process.
     *
     * @param pkg Description of the NPM package
     * @param version Version (tag) of the NPM package.
     * @param withPaths A set paths to be added to the system search path.
     * @return A Gradle-style resolvable dependency.
     */
    @SuppressWarnings('FactoryMethodName')
    SelfResolvingDependency createDependency(
            final PackageDescriptor pkg,
            final String version,
            final Set<File> withPaths
    ) {
        createDependency(pkg.name, version, pkg.scope, withPaths)
    }

    /** Create a NPM-based self-resolving dependency.
     *
     * @param name Name of NPM package.
     * @param version Version (tag) of NPM package.
     * @param scope Scope of NPM package.
     * @param withPaths A set paths to be added to the system search path.
     * @return A Gradle-style resolvable dependency.
     */
    @SuppressWarnings('FactoryMethodName')
    SelfResolvingDependency createDependency(
            final String name,
            final String version,
            final String scope,
            final Set<File> withPaths = null
    ) {
        Map<String, Object> description = [
                name          : name,
                tag           : version,
                type          : 'dev',
                'install-args': ['--no-bin-links', '--no-package-lock', '--loglevel=error']
        ]

        if (scope) {
            description.put('scope', scope)
        }

        if (withPaths) {
            description.put('path', project.files(withPaths).asPath)
        }

        new NpmSelfResolvingDependency(project, nodejs, npm, description)
    }
}
