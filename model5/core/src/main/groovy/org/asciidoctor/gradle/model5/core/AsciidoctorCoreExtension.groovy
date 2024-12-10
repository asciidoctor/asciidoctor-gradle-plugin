package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.PublicationFactory
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Provider

@CompileStatic
class AsciidoctorCoreExtension {
    public static final String NAME = 'asciidoc'

    final NamedDomainObjectContainer<AsciidoctorPublication> publications
    final ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains

    final Provider<List<ToolchainInformation>> registeredToolchains

    AsciidoctorCoreExtension(Project project) {
        final publicationFactory = new PublicationFactory(project, this)
        this.publications = project.objects.domainObjectContainer(
                AsciidoctorPublication, publicationFactory
        )

        this.toolchains = project.objects.polymorphicDomainObjectContainer(AsciidoctorToolchain)

        this.registeredToolchains = project.provider { ->
            toolchains.collect { tc  ->

                new ToolchainInformation(
                        tc.name,
                        tc.toolchainClass.canonicalName,
                        tc.registeredOutputFormatters.collectEntries { fmt ->
                            [fmt.name,fmt.outputFormatterClass.canonicalName]
                        }
                )
            }
        }
    }
}
