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
package org.asciidoctor.gradle.js.nodejs

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.asciidoctor.gradle.js.base.AbstractAsciidoctorJSExtension
import org.asciidoctor.gradle.js.base.AsciidoctorJSModules
import org.asciidoctor.gradle.js.nodejs.internal.AsciidoctorNodeJSModules
import org.asciidoctor.gradle.js.nodejs.core.NodeJSDependencyFactory
import org.asciidoctor.gradle.js.nodejs.internal.PackageDescriptor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.SelfResolvingDependency
import org.ysb33r.gradle.nodejs.NpmDependency

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
    public final static PackageDescriptor PACKAGE_ASCIIDOCTOR = PackageDescriptor.of('asciidoctor')

    private final List<NpmDependency> additionalRequires = []
    private boolean onlyTaskRequires = false

    /** Attach extension to project.
     *
     * @param project Project to attach to.
     */
    AsciidoctorJSExtension(Project project) {
        super(project)
    }

    /** Attach extension to a task.
     *
     * @param task Task to attach to
     */
    AsciidoctorJSExtension(Task task) {
        super(task, NAME)
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
        Set<String> reqs = [].toSet()

        final String docbook = moduleVersion(modules.docbook)
        if (docbook) {
            reqs.add(packageDescriptorFor(modules.docbook).toString())
        }

        reqs
    }

    /** A configuration containing all required NPM packages.
     * The configuration will differ between global state and task state if the task has set
     * different requires or different docbook versions.
     *
     * @return All resolvable NPM dependencies including {@code asciidoctor.js}.
     */
    @SuppressWarnings('UnnecessaryGetter')
    Configuration getConfiguration() {
        final String docbook = moduleVersion(modules.docbook)
        final NodeJSDependencyFactory factory = new NodeJSDependencyFactory(project, nodejs, npm)
        final List<SelfResolvingDependency> deps = [factory.createDependency(PACKAGE_ASCIIDOCTOR, getVersion())]

        if (docbook) {
            deps.add(factory.createDependency(packageDescriptorFor(modules.docbook), docbook))
        }

        project.configurations.detachedConfiguration(
                deps.toArray() as Dependency[]
        )
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
    File getToolingWorkDir() {
        if (versionsDifferFromGlobal()) {
            new File(npm.homeDirectory.parentFile, "${project.name}-${task.name}")
        } else {
            npm.homeDirectory
        }
    }

    /** Creates a new modules block.
     *
     * @return Modules block that can be attached to this extension.
     */
    @Override
    protected AsciidoctorJSModules createModulesConfiguration() {
        new AsciidoctorNodeJSModules(this, defaultVersionMap)
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

    private AsciidoctorJSNodeExtension getNodejs() {
        project.extensions.getByType(AsciidoctorJSNodeExtension)
    }

    private AsciidoctorJSNpmExtension getNpm() {
        project.extensions.getByType(AsciidoctorJSNpmExtension)
    }

    private PackageDescriptor packageDescriptorFor(final AsciidoctorModuleDefinition module) {
        ((AsciidoctorNodeJSModules.Module) module).package
    }

    private AsciidoctorJSExtension getExtFromProject() {
        task ? (AsciidoctorJSExtension) projectExtension : this
    }
}
