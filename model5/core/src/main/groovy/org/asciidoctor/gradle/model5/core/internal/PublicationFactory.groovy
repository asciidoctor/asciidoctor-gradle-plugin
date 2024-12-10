package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorCoreExtension
import org.asciidoctor.gradle.model5.core.AsciidoctorPublication
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

/**
 * Creates publications.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class PublicationFactory implements NamedDomainObjectFactory<AsciidoctorPublication> {

    private final ObjectFactory objectFactory
    private final AsciidoctorCoreExtension parent

    PublicationFactory(Project project, AsciidoctorCoreExtension parent) {
        this.objectFactory = project.objects
        this.parent = parent
    }

    @Override
    AsciidoctorPublication create(String name) {
        objectFactory.newInstance(AsciidoctorPublication, name, parent)
    }
}
