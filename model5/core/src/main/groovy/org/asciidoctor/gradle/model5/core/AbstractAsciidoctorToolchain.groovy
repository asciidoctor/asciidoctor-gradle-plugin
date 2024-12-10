package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

@CompileStatic
abstract class AbstractAsciidoctorToolchain implements AsciidoctorToolchain {

    final ExtensiblePolymorphicDomainObjectContainer<AsciidoctorOutputFormatter> registeredOutputFormatters
    final String name

    protected final ObjectFactory objectFactory

    protected AbstractAsciidoctorToolchain(String name, Project tempProjectReference) {
        this.name = name
        this.registeredOutputFormatters = tempProjectReference.objects.polymorphicDomainObjectContainer(AsciidoctorOutputFormatter)
        this.objectFactory = tempProjectReference.objects
    }
}
