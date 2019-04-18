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
package org.asciidoctor.gradle.js.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.ysb33r.gradle.nodejs.NodeJSExtension
import org.ysb33r.gradle.nodejs.NpmExtension
import org.ysb33r.gradle.nodejs.pkgwrapper.TagBasedPackageResolver
import org.ysb33r.grolifant.api.exec.ResolvableExecutable

import static org.ysb33r.gradle.nodejs.NpmDependencyGroup.DEVELOPMENT

/** Resolves the asciidoctor
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSResolver {

    public final static String PACKAGE_ASCIIDOCTOR = 'asciidoctor'

    AsciidoctorJSResolver(
        Project project,
        NodeJSExtension nodeExtension,
        NpmExtension npmExtension
    ) {
        packageResolver = new TagBasedPackageResolver(
            null,
            PACKAGE_ASCIIDOCTOR,
            project,
            nodeExtension,
            npmExtension,
            { DEVELOPMENT } as TagBasedPackageResolver.InstallGroup,
            { 'index.js' } as TagBasedPackageResolver.EntryPoint
        )
    }

    ResolvableExecutable getExecutable(final String version) {
        Closure resolver = { ResolvableExecutable asciidoctor ->
            new File(asciidoctor.executable,'../../@asciidoctor/cli/bin/asciidoctor').canonicalFile
        }.curry(packageResolver.build([:], version))
        resolver as ResolvableExecutable
    }

    private TagBasedPackageResolver packageResolver
}
