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
package org.asciidoctor.gradle.js.nodejs.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.js.nodejs.internal.PackageDescriptor
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.ysb33r.gradle.nodejs.NpmDependencyGroup
import org.ysb33r.gradle.nodejs.NpmPackageDescriptor
import org.ysb33r.gradle.nodejs.SimpleNpmPackageDescriptor
import org.ysb33r.gradle.nodejs.utils.npm.NpmExecutor
import org.ysb33r.grolifant.api.core.OperatingSystem
import org.ysb33r.grolifant.api.core.ProjectOperations

import javax.inject.Inject
import java.util.concurrent.Callable

/**
 * A factory class for creating NPM dependencies in a Gradle context.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
abstract class NodeJSDependencyFactory {
    private final ProjectOperations projectOperations
    private final NpmExecutor npmExecutor

    /** Instantiates a factory for a specific context.
     *
     * @param po The Gradle project for which is is done.
     * @param nodejs The NodeJS extension which is being used for this operation.
     * @param npm The NPM extension that is being used for this operation.
     *
     * @since 4.0
     */
    @Inject
    NodeJSDependencyFactory(
            ProjectOperations po,
            AsciidoctorJSNodeExtension nodejs,
            AsciidoctorJSNpmExtension npm
    ) {
        this.projectOperations = po
        this.npmExecutor = new NpmExecutor(po, nodejs, npm)
    }

    @Inject
    protected abstract DependencyFactory getDependencyFactory()

    /** Create a NPM-based dependency.
     *
     * @param pkg Description of the NPM package
     * @param version Version (tag) of the NPM package.
     * @return A Gradle-style resolvable dependency.
     */
    @SuppressWarnings('FactoryMethodName')
    Dependency createDependency(final PackageDescriptor pkg, final String version) {
        createDependency(pkg, version, null)
    }

    /** Create a NPM-based dependency.
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
    Dependency createDependency(
            final PackageDescriptor pkg,
            final String version,
            final Set<File> withPaths
    ) {
        NpmPackageDescriptor descriptor = new SimpleNpmPackageDescriptor(pkg.scope, pkg.name, version)
        createDependency(descriptor, withPaths)
    }

    /** Create a NPM-based dependency.
     *
     * @param name Name of NPM package.
     * @param version Version (tag) of NPM package.
     * @param scope Scope of NPM package.
     * @param withPaths A set of paths to be added to the system search path.
     * @return A Gradle-style resolvable dependency.
     */
    @SuppressWarnings('FactoryMethodName')
    Dependency createDependency(
            final NpmPackageDescriptor descriptor,
            final Set<File> withPaths = null
    ) {
        // Wrap actual file collection in a Callable to prevent eager installation of package
        Callable<FileCollection> fileCollectionSupplier = () -> {
            getPackageFiles(descriptor, withPaths)
        }
        return dependencyFactory.create(
                projectOperations.fsOperations.emptyFileCollection().from(fileCollectionSupplier)
        )
    }

    private FileCollection getPackageFiles(
            final NpmPackageDescriptor descriptor,
            final Set<File> withPaths
    ) {
        File installDir = npmExecutor.getPackageInstallationFolder(descriptor)
        FileTree allFiles = projectOperations.fileTree(installDir)

        if (allFiles.isEmpty()) {
            installPackage(withPaths, descriptor)
        }

        allFiles
    }

    private void installPackage(Set<File> withPaths, NpmPackageDescriptor descriptor) {
        Map<String, Object> env = [:]
        if (withPaths) {
            String path = projectOperations.fsOperations.files(withPaths).asPath
            env[OperatingSystem.current().pathVar] = path
        }

        List<String> installArgs = projectOperations.stringTools.stringize(['--no-bin-links', '--no-package-lock', '--loglevel=error'])
        npmExecutor.installNpmPackage(descriptor, NpmDependencyGroup.DEVELOPMENT, installArgs, env).files
    }

}
