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
package org.asciidoctor.gradle.model5.js.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.engines.AsciidoctorEngine
import org.asciidoctor.gradle.model5.js.JsModel
import org.asciidoctor.gradle.model5.js.internal.engines.DefaultLauncher
import org.asciidoctor.gradle.model5.js.internal.engines.NpmPackage

import org.asciidoctor.gradle.model5.js.toolchains.CoreVersions
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.gradle.nodejs.NodeJSConfigCacheSafeOperations
import org.ysb33r.gradle.nodejs.NodeJSExecSpec
import org.ysb33r.gradle.nodejs.NodeJSExtension
import org.ysb33r.gradle.nodejs.NpmConfigCacheSafeOperations
import org.ysb33r.gradle.nodejs.NpmExtension
import org.ysb33r.gradle.nodejs.NpmPackageDescriptor
import org.ysb33r.gradle.nodejs.tasks.NodeNpmPrepareTask
import org.ysb33r.gradle.nodejs.utils.npm.NpmExecutor
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreBasePlugin.INTERMEDIATE_RESOURCE_PATH

/**
 * The core engine for running {@code asciidoctor.js}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjsNodeEngine implements AsciidoctorEngine, CoreVersions {

    final String name
    final NodeJSExtension nodejs
    final NpmExtension npm
    private final ConfigCacheSafeOperations ccso
    private final Property<String> asciidoctorjsVersion
    private final Property<String> asciidoctorjsCliVersion
    private final ListProperty<NpmPackageDescriptor> packages

    private final Provider<DefaultLauncher> launcherProvider

    @Inject
    AsciidoctorjsNodeEngine(String name, Project tempProjectReference) {
        this.name = name
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.nodejs = new NodeJSExtension(tempProjectReference)
        this.packages = tempProjectReference.objects.listProperty(NpmPackageDescriptor)
        this.npm = new NpmExtension(tempProjectReference, this.nodejs).tap {
            homeDirectory = ccso.fsOperations().buildDirDescendant(
                    "tmp/asciidoctorjs-engine/${ccso.fsOperations().toSafeFileName(name)}")
        }

        final props = ccso.fsOperations().loadPropertiesFromResource(
                "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-js-core-plugin.properties",
                this.class.classLoader
        )

        this.asciidoctorjsVersion = tempProjectReference.objects.property(String)
                .convention(props['asciidoctorjs'].toString())
        this.asciidoctorjsCliVersion = tempProjectReference.objects.property(String)
                .convention(props['asciidoctorjs.cli'].toString())

        usePackage('asciidoctor', 'core', this.asciidoctorjsVersion)
        usePackage('asciidoctor', 'cli', this.asciidoctorjsCliVersion)

        createToolchainPrepareTask(tempProjectReference)
        this.launcherProvider = createLauncher(tempProjectReference)
    }

    /**
     * Something that can execute Asciidoctor conversions.
     *
     * @return Provider to a runnable Asciidoctor engine.
     */
    @Override
    Provider<? extends AsciidoctorLauncher> getLauncher() {
        this.launcherProvider
    }

    /**
     * Sets the version of {@code asciidoctor.js} to use.
     *
     * @param ver New version to be used. Can be of anything that can be resolved by
     *          {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    void useAsciidoctorjs(Object ver) {
        ccso.stringTools().updateStringProperty(this.asciidoctorjsVersion, ver)
    }

    /**
     * Sets the version of {@code asciidoctor.js cli} to use.
     *
     * @param ver New version to be used. Can be of anything that can be resolved by
     *          {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    void useAsciidoctorjsCli(Object ver) {
        ccso.stringTools().updateStringProperty(this.asciidoctorjsCliVersion, ver)
    }

    /**
     * Change the version of Node to use.
     *
     * @param ver Node version
     */
    void useNode(Object ver) {
        nodejs.executableByVersion(ver)
    }

    /**
     * Allows output formatters and extensions to registers additional packages.
     *
     * @param scope Scope
     * @param pkgName Name
     * @param ver Lazy-evaluated version
     */
    void usePackage(String scope,String pkgName, Object ver) {
        this.packages.add(new NpmPackage(
                scope,
                pkgName,
                ccso.stringTools().provideString(ver)
        ))
    }

    private NodeJSExecSpec createExecSpec() {
        final env = NpmExecutor.environmentFromExtensions(nodejs,npm)
        nodejs.createExecSpec().tap { spec ->
            entrypoint {
                workingDir(this.npm.homeDirectoryProvider)
                environment(env)
            }
            runnerSpec {
                args('node_modules/@asciidoctor/cli/bin/asciidoctor')
            }
        }
    }

    private Provider<DefaultLauncher> createLauncher(Project tempProjectReference) {
        final execSpec = createExecSpec()
        final jsLauncher = tempProjectReference.objects.newInstance(
                DefaultLauncher,
                execSpec,
                NodeJSConfigCacheSafeOperations.from(nodejs),
                NpmConfigCacheSafeOperations.from(npm)
        )
        jsLauncher.packages = this.packages
        tempProjectReference.provider { -> jsLauncher }
    }

    private void createToolchainPrepareTask(Project project) {
        final task = project.tasks.register(
                JsModel.toolchainPrepareTaskName(name),
                NodeNpmPrepareTask,
                NodeJSConfigCacheSafeOperations.from(this.nodejs),
                NpmConfigCacheSafeOperations.from(this.npm)
        )
        task.configure {
            it.packages = packages
        }
    }
}
