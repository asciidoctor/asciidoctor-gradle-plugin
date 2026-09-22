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
package org.asciidoctor.gradle.model5.downdoc.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.core.engines.AsciidoctorEngine
import org.asciidoctor.gradle.model5.downdoc.DowndocModel
import org.asciidoctor.gradle.model5.downdoc.internal.engines.DefaultLauncher
import org.asciidoctor.gradle.model5.downdoc.internal.engines.PnpmToolchainLocater
import org.asciidoctor.gradle.model5.downdoc.toolchains.CoreVersions
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.gradle.jse.pnpm.tasks.PnpmPrepareTask
import org.ysb33r.gradle.jse.pnpm.toolchains.JsePnpmExecSpec
import org.ysb33r.gradle.jsecosystem.packages.PackageDescriptor
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreBasePlugin.INTERMEDIATE_RESOURCE_PATH

/**
 * The core engine for running {@code downdoc}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DowndocNodeEngine implements AsciidoctorEngine, CoreVersions {

    public static final String DOWNDOC_PROP = 'downdoc'
    public static final String DOWNDOC_PACKAGE = DOWNDOC_PROP

    final String name
    private final ConfigCacheSafeOperations ccso
    private final Property<String> downdocVersion
    private final ListProperty<PackageDescriptor> packages
    private final DirectoryProperty homeDir
    private final Provider<DefaultLauncher> launcherProvider
    private final Property<JsePnpmExecSpec> execSpec

    @Inject
    DowndocNodeEngine(String name, Project tempProjectReference) {
        this.name = name
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.packages = tempProjectReference.objects.listProperty(PackageDescriptor)
        this.execSpec = tempProjectReference.objects.property(JsePnpmExecSpec)
        this.homeDir = tempProjectReference.objects.directoryProperty().value(
            tempProjectReference.layout.buildDirectory.dir(
                "tmp/downdoc-engine/${ccso.fsOperations().toSafeFileName(name)}"
            )
        )

        final props = ccso.fsOperations().loadPropertiesFromResource(
            "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-downdoc-plugin.properties",
            this.class.classLoader
        )

        this.downdocVersion = tempProjectReference.objects.property(String)
            .convention(props[DOWNDOC_PROP].toString())
        usePackage(null, DOWNDOC_PACKAGE, this.downdocVersion)

        createToolchainPrepareTask(tempProjectReference)
        this.launcherProvider = createLauncher(tempProjectReference)
    }

    @Override
    Provider<? extends AsciidoctorLauncher> getLauncher() {
        this.launcherProvider
    }

    /**
     * Sets the version of {@code downdoc} to use.
     *
     * @param ver New version to be used. Can be of anything that can be resolved by
     * {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    void useDowndoc(Object ver) {
        ccso.stringTools().updateStringProperty(this.downdocVersion, ver)
    }

    @Override
    void usePnpmToolchain(String tcName) {
        final locator = ccso.providerTools().newInstance(PnpmToolchainLocater)
        final tc = locator.get(tcName)
        this.execSpec.set(tc.get().createExecSpec {
            it.entrypoint.workingDir = homeDir
        })
        locator.configurePrepareTask(tcName, DowndocModel.toolchainPrepareTaskName(name))
    }

    /**
     * Allows output formatters and extensions to registers additional packages.
     *
     * @param scope Scope
     * @param pkgName Name
     * @param ver Lazy-evaluated version
     */
    void usePackage(String scope, String pkgName, Object ver) {
        this.packages.add(ccso.stringTools().provideString(ver).map { v ->
            PackageDescriptor.of(scope, pkgName, v)
        })
    }

    private Provider<DefaultLauncher> createLauncher(Project tempProjectReference) {
        final jsLauncher = tempProjectReference.objects.newInstance(
            DefaultLauncher,
            execSpec
        )
        jsLauncher.packages = this.packages
        tempProjectReference.provider { -> jsLauncher }
    }

    private void createToolchainPrepareTask(Project project) {
        final task = project.tasks.register(
            DowndocModel.toolchainPrepareTaskName(name),
            PnpmPrepareTask
        )
        task.configure {
            it.packages = packages
            it.workdir = homeDir
        }
    }
}
