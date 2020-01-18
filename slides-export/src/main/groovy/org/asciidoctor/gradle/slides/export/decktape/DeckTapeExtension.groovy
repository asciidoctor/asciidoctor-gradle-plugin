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
package org.asciidoctor.gradle.slides.export.decktape

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.ModuleVersionLoader
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSNodeExtension
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSNpmExtension
import org.asciidoctor.gradle.js.nodejs.core.NodeJSDependencyFactory
import org.asciidoctor.gradle.js.nodejs.internal.PackageDescriptor
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.SelfResolvingDependency
import org.ysb33r.gradle.nodejs.NpmPackageDescriptor

import static org.ysb33r.gradle.nodejs.utils.npm.NpmExecutor.getPackageInstallationFolder

/** Extension to configure <a href="https://github.com/astefanutti/decktape">decktape</a> and
 * <a href="https://github.com/GoogleChrome/puppeteer">puppeteer</a>.
 */
@CompileStatic
class DeckTapeExtension {

    public static final String NAME = 'decktape'
    private static final String MODULE_VERSION = 'slides2pdf'

    private final static PackageDescriptor PACKAGE_DECKTAPE = PackageDescriptor.of(NAME)
    private final static PackageDescriptor PACKAGE_PUPPETEER = PackageDescriptor.of('puppeteer')
    private final static PackageDescriptor PACKAGE_PREGYP = PackageDescriptor.of('node-pre-gyp')

    String version
    String puppeteerVersion
    String pregypVersion

    private final Project project

    DeckTapeExtension(Project project) {
        this.project = project
        version = ModuleVersionLoader.load(MODULE_VERSION)['npm.decktape']
        puppeteerVersion = ModuleVersionLoader.load(MODULE_VERSION)['npm.puppeteer']
        pregypVersion = ModuleVersionLoader.load(MODULE_VERSION)['npm.node-pre-gyp']
    }

    /** A configuration containing all required NPM packages.
     *
     * @return All resolvable NPM dependencies required for running decktape.
     */
    @SuppressWarnings('UnnecessaryGetter')
    Configuration getConfiguration() {
        final NodeJSDependencyFactory factory = new NodeJSDependencyFactory(
                project,
                nodejs,
                npm
        )

        // Ensure puppeteer is installed first
        final List<SelfResolvingDependency> deps = [
                factory.createDependency(PACKAGE_PUPPETEER, puppeteerVersion),
                factory.createDependency(PACKAGE_PREGYP, pregypVersion)
        ]

        File preGypBinPath = new File(
                getPackageInstallationFolder(project, nodejs, npm, (NpmPackageDescriptor) deps[1]),
                'bin'
        )

        deps.add(factory.createDependency(
                PACKAGE_DECKTAPE,
                version,
                [preGypBinPath].toSet()
        ))

        project.configurations.detachedConfiguration(
                deps.toArray() as Dependency[]
        )
    }

    File getToolingWorkDir() {
        npm.homeDirectory
    }

    private AsciidoctorJSNodeExtension getNodejs() {
        project.extensions.getByType(AsciidoctorJSNodeExtension)
    }

    private AsciidoctorJSNpmExtension getNpm() {
        project.extensions.getByType(AsciidoctorJSNpmExtension)
    }

}
