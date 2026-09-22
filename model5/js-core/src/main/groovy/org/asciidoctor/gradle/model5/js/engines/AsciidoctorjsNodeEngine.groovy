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
package org.asciidoctor.gradle.model5.js.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.engines.AsciidoctorEngine
import org.asciidoctor.gradle.model5.js.JsModel
import org.asciidoctor.gradle.model5.js.internal.engines.DefaultLauncher
import org.asciidoctor.gradle.model5.js.internal.toolchains.AbstractAsciidoctorjsToolchain
import org.asciidoctor.gradle.model5.js.toolchains.CoreVersions
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.gradle.jse.pnpm.tasks.PnpmPrepareTask
import org.ysb33r.gradle.jse.pnpm.toolchains.JsePnpmToolchain
import org.ysb33r.gradle.jsecosystem.JsEcosystemExtension
import org.ysb33r.gradle.jsecosystem.packages.PackageDescriptor
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * The core engine for running {@code asciidoctor.js}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorjsNodeEngine implements AsciidoctorEngine, CoreVersions {

    public static final String ASCIIDOCTOR_SCOPE = 'asciidoctor'
    private static final String ASCIIDOCTOR_SCRIPT = ASCIIDOCTOR_SCOPE

    final String name
    private final Provider<Directory> workingDir
    private final ConfigCacheSafeOperations ccso
    private final Property<String> asciidoctorjsVersion
    private final Property<String> asciidoctorjsCliVersion
    private final ListProperty<PackageDescriptor> packages
    private final NamedDomainObjectProvider<JsePnpmToolchain> pnpmToolchain

    private final Provider<DefaultLauncher> launcherProvider

    @Inject
    AsciidoctorjsNodeEngine(
        String name,
        String propAsciidoctorVer,
        String propAsciidoctorCliVer,
        Project tempProjectReference
    ) {
        this.name = name
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.packages = tempProjectReference.objects.listProperty(PackageDescriptor)
        this.pnpmToolchain = tempProjectReference.extensions.getByType(JsEcosystemExtension).toolchains.register(
            "asciidoctorJs${name.capitalize()}",
            JsePnpmToolchain
        ) {
            it.withPnpmNode()
        }
        this.workingDir = ccso.fsOperations().buildDirDirectory(
            "tmp/asciidoctorjs-engine/${ccso.fsOperations().toSafeFileName(name)}")

        final props = ccso.fsOperations().loadPropertiesFromResource(
            AbstractAsciidoctorjsToolchain.PROPS_RESOURCE,
            this.class.classLoader
        )
        this.asciidoctorjsVersion = tempProjectReference.objects.property(String)
            .convention(props[propAsciidoctorVer].toString())
        this.asciidoctorjsCliVersion = tempProjectReference.objects.property(String)
            .convention(props[propAsciidoctorCliVer].toString())

        usePackage(ASCIIDOCTOR_SCOPE, 'core', this.asciidoctorjsVersion)
        usePackage(ASCIIDOCTOR_SCOPE, 'cli', this.asciidoctorjsCliVersion)

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
     * {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    void useAsciidoctorjs(Object ver) {
        ccso.stringTools().updateStringProperty(this.asciidoctorjsVersion, ver)
    }

    /**
     * Sets the version of {@code asciidoctor.js cli} to use.
     *
     * @param ver New version to be used. Can be of anything that can be resolved by
     * {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    void useAsciidoctorjsCli(Object ver) {
        ccso.stringTools().updateStringProperty(this.asciidoctorjsCliVersion, ver)
    }

    /**
     * Change the version of {@code node} to use.
     *
     * @param ver {@code node} version
     */
    @Override
    void useNode(Object ver) {
        pnpmToolchain.configure {
            it.withPnpmNode(ver)
        }
    }

    /**
     * Overrides the default version of {code pnpm}.
     *
     * @param ver {@code pnpm} version
     */
    @Override
    void usePnpm(Object ver) {
        pnpmToolchain.configure {
            it.executableByVersion(ver)
        }
    }

    /**
     * Allows output formatters and extensions to registers additional packages.
     *
     * @param scope Scope
     * @param pkgName Name
     * @param ver Lazy-evaluated version
     */
    void usePackage(String scope, String pkgName, Object ver) {
        this.packages.add(
            ccso.stringTools().provideString(ver).map {
                PackageDescriptor.of(scope, pkgName, it)
            }
        )
    }

    private Provider<DefaultLauncher> createLauncher(Project tempProjectReference) {
        final wd = this.workingDir
        final jsLauncher = tempProjectReference.objects.newInstance(
            DefaultLauncher,
            pnpmToolchain.map { tc ->
                tc.createExecSpec().tap {
                    runnerSpec.args 'exec', ASCIIDOCTOR_SCRIPT
                    entrypoint.workingDir(wd)
                }
            }
        )
        jsLauncher.packages = this.packages
        tempProjectReference.provider { -> jsLauncher }
    }

    private void createToolchainPrepareTask(Project project) {
        project.tasks.register(
            JsModel.toolchainPrepareTaskName(name),
            PnpmPrepareTask
        ) {
            it.packages = packages
            it.toolchain = pnpmToolchain
            it.workdir = workingDir
        }
    }

}
