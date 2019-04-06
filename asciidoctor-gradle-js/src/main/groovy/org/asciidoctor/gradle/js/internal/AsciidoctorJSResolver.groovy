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

    AsciidoctorJSResolver(
        Project project,
        NodeJSExtension nodeExtension,
        NpmExtension npmExtension
    ) {
        packageResolver = new TagBasedPackageResolver(
            null,
            'asciidoctor',
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
