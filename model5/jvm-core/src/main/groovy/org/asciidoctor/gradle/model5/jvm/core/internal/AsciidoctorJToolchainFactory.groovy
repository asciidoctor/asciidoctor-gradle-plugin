package org.asciidoctor.gradle.model5.jvm.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.core.AsciidoctorJToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

@CompileStatic
class AsciidoctorJToolchainFactory implements NamedDomainObjectFactory<AsciidoctorJToolchain> {

    private final ObjectFactory objectFactory

    @Inject
    AsciidoctorJToolchainFactory(Project project) {
        this.objectFactory = project.objects
    }

    @Override
    AsciidoctorJToolchain create(String name) {
        objectFactory.newInstance(DefaultAsciidoctorJToolchain,name).tap {

        }
    }
}
