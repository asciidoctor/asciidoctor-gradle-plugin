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
package org.asciidoctor.gradle.js

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AbstractImplementationEngineExtension
import org.asciidoctor.gradle.js.internal.PackageDescriptor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.SelfResolvingDependency
import org.ysb33r.gradle.nodejs.NpmDependency
import org.ysb33r.gradle.nodejs.dependencies.npm.NpmSelfResolvingDependency

import static org.ysb33r.grolifant.api.StringUtils.stringize

/**
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSExtension extends AbstractImplementationEngineExtension {
    public final static String NAME = 'asciidoctorjs'
    public final static String DEFAULT_ASCIIDOCTORJS_VERSION = '2.0.2'
    public final static String DEFAULT_DOCBOOK_VERSION = '2.0.0'

    private final static PackageDescriptor PACKAGE_ASCIIDOCTOR = PackageDescriptor.of('asciidoctor')
    private final static PackageDescriptor PACKAGE_DOCBOOK = PackageDescriptor.of('asciidoctor', 'docbook-converter')

    private Object version = DEFAULT_ASCIIDOCTORJS_VERSION
    private Optional<Object> docbookVersion
    private List<NpmDependency> additionalRequires = []

    private boolean onlyTaskRequires = false

    AsciidoctorJSExtension(Project project) {
        super(project)
    }

    /** Attach extension to a task.
     *
     * @param task
     */
    AsciidoctorJSExtension(Task task) {
        super(task, NAME)
    }

    /** Version of AsciidoctorJS that should be used.
     *
     */
    String getVersion() {
        if (task) {
            this.version ? stringize(this.version) : extFromProject.getVersion()
        } else {
            stringize(this.version)
        }
    }

    /** Set a new version to use.
     *
     * @param v New version to be used. Can be of anything that be resolved by {@link org.ysb33r.grolifant.api.StringUtils.stringize}.
     */
    void setVersion(Object v) {
        this.version = v
    }


    /** Version of the Docbook converter that should be used.
     *
     * @return Version of extension DSL or {@code null} if extensions will not be used.
     */
    String getDocbookVersion() {
        if (task) {
            if (this.docbookVersion != null && this.docbookVersion.present) {
                stringize(this.docbookVersion.get())
            } else {
                extFromProject.docbookVersion
            }
        } else {
            this.docbookVersion?.present ? stringize(this.docbookVersion.get()) : null
        }
    }

    /** Set a new Docbook converter version to use.
     *
     * Implies {@link #useDocbook}, but sets a custom version rather than a default.
     *
     * @param v Groovy DSL version.
     */
    void setDocbookVersion(Object v) {
        this.docbookVersion = Optional.of(v)
    }

    /** Enables Docbook conversion with whichever docbook version is the default.
     *
     */
    void useDocbook() {
        setDocbookVersion(DEFAULT_DOCBOOK_VERSION)
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

        final String docbook = getDocbookVersion()
        if (docbook) {
            reqs.add(PACKAGE_DOCBOOK.toString())
        }

        reqs
    }

    /** A configuration containing all required NPM packages.
     * The configuration will differ between global state and task state if the task has set
     * different requires or different docbook versions.
     *
     * @return All resolvable NPM dependencies including {@code asciidoctor.js}.
     */
    Configuration getConfiguration() {
        final String docbook = getDocbookVersion()
        final List<SelfResolvingDependency> deps = [createDependency(PACKAGE_ASCIIDOCTOR, getVersion())]

        if (docbook) {
            deps.add(createDependency(PACKAGE_DOCBOOK, docbook))
        }

        project.configurations.detachedConfiguration(
            deps.toArray() as Dependency[]
        )
    }

    /** The suggested working directory that the implementation tool should use.
     *
     * For instance if the implementaiotn tool is Node.js with NPM, the directory
     * will usually be the same for all tasks, but in cases where different
     * versions of NPM packages are used by tasks then the working directory will be different
     * for the specific task.
     *
     * @return Suggested working directory.
     */
    File getToolingWorkDir() {
        versionsDifferFromGlobal() ? new File(npm.homeDirectory.parentFile, "${project.name}-${task.name}") : npm.homeDirectory
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
            if (getVersion() != extFromProject.getVersion() ||
                getDocbookVersion() != extFromProject.getDocbookVersion()
            ) {
                true
            } else {
                List<NpmDependency> global = extFromProject.allAdditionalRequires
                this.additionalRequires.find { NpmDependency depLocal ->
                    global.find { NpmDependency depGlobal ->
                        depGlobal.scope == depLocal.scope && depGlobal.packageName == depLocal.packageName &&
                            depGlobal.tagName != depLocal.tagName
                    }
                } != null
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

    private SelfResolvingDependency createDependency(final PackageDescriptor pkg, final String version) {
        createDependency(pkg.name, version, pkg.scope)
    }

    private SelfResolvingDependency createDependency(final String name, final String version, final String scope) {

        Map<String, Object> description = [
            name          : name,
            tag           : version,
            type          : 'dev',
            'install-args': ['--no-bin-links', '--no-package-lock', '--loglevel=error']
        ] as Map<String, Object>

        if (scope) {
            description.put('scope', scope)
        }

        new NpmSelfResolvingDependency(project, nodejs, npm, description)
    }

    private AsciidoctorJSExtension getExtFromProject() {
        task ? (AsciidoctorJSExtension) projectExtension : this
    }


}
