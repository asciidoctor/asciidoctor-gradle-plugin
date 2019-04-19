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
import org.ysb33r.gradle.nodejs.dependencies.npm.NpmSelfResolvingDependency

/**
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSExtension extends AbstractImplementationEngineExtension {
    public final static String NAME = 'asciidoctorjs'
    public final static String DEFAULT_ASCIIDOCTORJS_VERSION = '2.0.0'
    public final static String DEFAULT_DOCBOOK_VERSION = '2.0.0'

    private final static PackageDescriptor PACKAGE_ASCIIDOCTOR = PackageDescriptor.of('asciidoctor')
    private final static PackageDescriptor PACKAGE_DOCBOOK = PackageDescriptor.of('asciidoctor', 'docbook-converter')

    String version = DEFAULT_ASCIIDOCTORJS_VERSION
    String docbookVersion

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


    /** Returns the set of NPM packages to be included except for the asciidoctor.js package.
     *
     * @Return Set of strings in a format suitable for {@code npm --require}.
     */
    Set<String> getRequires() {
        Set<String> reqs = [].toSet()

        if (docbookVersion) {
            reqs.add(PACKAGE_DOCBOOK.toString())
        }
//        stringizeList(this.nodejsRequires, onlyTaskRequires) { AsciidoctorJSExtension it ->
//            it.requires.toList()
//        }.toSet()
        reqs
    }

    /** A configuration containing all required NPM packages.
     *
     * @return
     */
    Configuration getConfiguration() {
        List<SelfResolvingDependency> deps = [createDependency(PACKAGE_ASCIIDOCTOR, version)]

        if (docbookVersion) {
            deps.add(createDependency(PACKAGE_DOCBOOK, docbookVersion))
        }

        project.configurations.detachedConfiguration(
            deps.toArray() as Dependency[]
        )
    }

    private AsciidoctorJSNodeExtension getNodejs() {
        if (task) {
            task.project.extensions.getByType(AsciidoctorJSNodeExtension) // FIXME to point to task
        } else {
            project.extensions.getByType(AsciidoctorJSNodeExtension)
        }
    }

    private AsciidoctorJSNpmExtension getNpm() {
        if (task) {
            task.project.extensions.getByType(AsciidoctorJSNpmExtension) // FIXME to point to task
        } else {
            project.extensions.getByType(AsciidoctorJSNpmExtension)
        }
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
}
