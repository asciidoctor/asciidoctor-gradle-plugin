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
package org.asciidoctor.gradle.js.nodejs

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.asciidoctor.gradle.js.base.AbstractAsciidoctorJSExtension
import org.asciidoctor.gradle.js.base.AsciidoctorJSModules
import org.asciidoctor.gradle.js.nodejs.core.AsciidoctorJSNodeExtension
import org.asciidoctor.gradle.js.nodejs.core.AsciidoctorJSNpmExtension
import org.asciidoctor.gradle.js.nodejs.core.NodeJSDependencyFactory
import org.asciidoctor.gradle.js.nodejs.internal.AsciidoctorNodeJSModules
import org.asciidoctor.gradle.js.nodejs.internal.PackageDescriptor
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolutionStrategy
import org.gradle.api.provider.Provider
import org.ysb33r.gradle.nodejs.NpmDependency
import org.ysb33r.gradle.nodejs.dependencies.npm.BaseNpmSelfResolvingDependency

import static org.asciidoctor.gradle.js.nodejs.internal.AsciidoctorNodeJSModules.SCOPE_ASCIIDOCTOR

/** Extension for configuring AsciidoctorJS.
 *
 * It can be used as both a project and a task extension.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSExtension extends AbstractAsciidoctorJSExtension {
    public final static String NAME = 'asciidoctorjs'
    public final static PackageDescriptor PACKAGE_ASCIIDOCTOR = PackageDescriptor.of(SCOPE_ASCIIDOCTOR, 'cli')
    private final static String CONFIGURATION_NAME = "__\$\$${NAME}\$\$__"

    private final List<NpmDependency> additionalRequires = []
    private final String projectName
    private boolean onlyTaskRequires = false
    private final AsciidoctorJSNodeExtension nodejs
    private final AsciidoctorJSNpmExtension npm
    private final NodeJSDependencyFactory dependencyFactory
    private final Configuration publicConfiguration
    private final Configuration privateConfiguration

    /** Attach extension to project.
     *
     * @param project Project to attach to.
     */
    AsciidoctorJSExtension(Project project) {
        super(project)

        String privateName = "${CONFIGURATION_NAME}_d"
        String publicName = "${CONFIGURATION_NAME}_r"
        projectOperations.configurations.createLocalRoleFocusedConfiguration(privateName, publicName)
        this.privateConfiguration = project.configurations.getByName(privateName)
        this.publicConfiguration = project.configurations.getByName(publicName)
        this.projectName = project.name
        this.nodejs = project.extensions.getByType(AsciidoctorJSNodeExtension)
        this.npm = project.extensions.getByType(AsciidoctorJSNpmExtension)
        this.dependencyFactory = new NodeJSDependencyFactory(
                projectOperations,
                this.nodejs,
                this.npm
        )
        loadDependencies()
    }

    /** Attach extension to a task.
     *
     * @param task Task to attach to
     */
    AsciidoctorJSExtension(AbstractAsciidoctorNodeJSTask task) {
        super(task, NAME)
        this.publicConfiguration = task.project.configurations.create("__\$\$${NAME}_${task.name}\$\$__")

        String privateName = "__\$\$${NAME}_${task.name}\$\$__d"
        String publicName = "__\$\$${NAME}_${task.name}\$\$__r"
        projectOperations.configurations.createLocalRoleFocusedConfiguration(privateName, publicName)
        this.privateConfiguration = task.project.configurations.getByName(privateName)
        this.publicConfiguration = task.project.configurations.getByName(publicName)
        this.projectName = task.project.name
        this.nodejs = task.extensions.getByType(AsciidoctorJSNodeExtension)
        this.npm = task.extensions.getByType(AsciidoctorJSNpmExtension)
        this.dependencyFactory = new NodeJSDependencyFactory(
                projectOperations,
                this.nodejs,
                this.npm
        )

        Configuration projectConfiguration = task.project.configurations.findByName("${CONFIGURATION_NAME}_d")
        if (projectConfiguration) {
            this.publicConfiguration.extendsFrom(projectConfiguration)
        }

        npm.homeDirectory = projectOperations.provider { ->
            if (versionsDifferFromGlobal()) {
                new File(npm.projectExtension.homeDirectory.parentFile, "${projectName}-${task.name}")
            } else {
                npm.projectExtension.homeDirectory
            }
        }
    }

    @SuppressWarnings('UnnecessarySetter')
    @Override
    void setVersion(Object v) {
        super.setVersion(v)
        setResolutionStrategy()
    }

    /** Adds an additional NPM package that is required
     *
     * @param name Name of package
     * @param tag Tag of package
     */
    void require(final String name, final String tag) {
        this.additionalRequires.add(new NpmDependency(name, tag))
    }

    /** Adds an additional NPM package that is required
     *
     * @param scope Scope of package
     * @param name Name of package
     * @param tag Tag of package
     */
    void require(final String scope, final String name, final String tag) {
        this.additionalRequires.add(new NpmDependency(scope, name, tag))
    }

    /** Removes any previous requires.
     *
     * If set on a task extension, no requires from global will be used.
     */
    void clearRequires() {
        if (task) {
            onlyTaskRequires = true
        }
        additionalRequires.clear()
    }

    /** Returns the set of NPM packages to be included except for the asciidoctor.js package.
     *
     * @Return Set of strings in a format suitable for {@code npm --require}.
     */
    Set<String> getRequires() {
        Set<String> reqs = (Set) [].toSet()

        final String docbook = moduleVersion(modules.docbook)
        if (docbook) {
            reqs.add(packageDescriptorFor(modules.docbook).toString())
        }

        reqs
    }

    /**
     * A configuration containing all required NPM packages.
     * The configuration will differ between global state and task state if the task has set
     * different requires or different docbook versions.
     *
     * @return All resolvable NPM dependencies including {@code asciidoctor.js}.
     */
    Configuration getConfiguration() {
        this.publicConfiguration
    }

    /** The suggested working directory that the implementation tool should use.
     *
     * For instance if the implementation tool is Node.js with NPM, the directory
     * will usually be the same for all tasks, but in cases where different
     * versions of NPM packages are used by tasks then the working directory will be different
     * for the specific task.
     *
     * @return Suggested working directory.
     */
    Provider<File> getToolingWorkDir() {
        npm.homeDirectoryProvider
    }

    /** Creates a new modules block.
     *
     * @return Modules block that can be attached to this extension.
     */
    @Override
    protected AsciidoctorJSModules createModulesConfiguration() {
        final newModules = new AsciidoctorNodeJSModules(projectOperations, defaultVersionMap)
        newModules.onUpdate {
            owner.loadDependencies()
            owner.setResolutionStrategy()
        }
        newModules
    }

    private List<NpmDependency> getAllAdditionalRequires() {
        if (task) {
            if (onlyTaskRequires) {
                this.additionalRequires
            } else {
                List<NpmDependency> reqs = []
                reqs.addAll(extFromProject.additionalRequires)
                reqs.addAll(this.additionalRequires)
                reqs
            }
        } else {
            this.additionalRequires
        }
    }

    private boolean versionsDifferFromGlobal() {
        if (task) {
            if (version != extFromProject.version ||
                    modules.isSetVersionsDifferentTo(extFromProject.modules)
            ) {
                true
            } else {
                List<NpmDependency> global = extFromProject.allAdditionalRequires
                this.additionalRequires.any { NpmDependency depLocal ->
                    global.any { NpmDependency depGlobal ->
                        depGlobal.scope == depLocal.scope && depGlobal.packageName == depLocal.packageName &&
                                depGlobal.tagName != depLocal.tagName
                    }
                }
            }
        } else {
            false
        }
    }

    private PackageDescriptor packageDescriptorFor(final AsciidoctorModuleDefinition module) {
        ((AsciidoctorNodeJSModules.Module) module).package
    }

    private AsciidoctorJSExtension getExtFromProject() {
        task ? (AsciidoctorJSExtension) projectExtension : this
    }

    private void loadDependencies() {
        this.privateConfiguration.dependencies.add(
                dependencyFactory.createDependency(PACKAGE_ASCIIDOCTOR, version)
        )

        if (moduleVersion(modules.docbook)) {
            this.privateConfiguration.dependencies.add(
                    dependencyFactory.createDependency(
                            packageDescriptorFor(modules.docbook),
                            moduleVersion(modules.docbook)
                    )
            )
        }
    }

    private void setResolutionStrategy() {
        final String docbook = moduleVersion(modules.docbook)
        this.publicConfiguration.resolutionStrategy { ResolutionStrategy rs ->
            rs.eachDependency { drd ->
                if (drd.requested.group.startsWith(BaseNpmSelfResolvingDependency.NPM_MAVEN_GROUP_PREFIX)) {
                    if (drd.requested.group.endsWith(PACKAGE_ASCIIDOCTOR.scope) &&
                            drd.requested.name == PACKAGE_ASCIIDOCTOR.name) {
                        drd.useVersion(version)
                    }
                    if (docbook) {
                        final pd = packageDescriptorFor(modules.docbook)
                        if (drd.requested.group.endsWith(pd.scope) &&
                                drd.requested.name == pd.name) {
                            drd.useVersion(docbook)
                        }
                    }
                }
            }
        }
    }
}
