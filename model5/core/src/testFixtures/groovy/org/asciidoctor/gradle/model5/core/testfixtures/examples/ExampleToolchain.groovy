package org.asciidoctor.gradle.model5.core.testfixtures.examples

// tag::minimal-toolchain[]
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AbstractAsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.AsciidoctorToolchain
import org.gradle.api.Project

import javax.inject.Inject

@CompileStatic
class ExampleToolchain extends AbstractAsciidoctorToolchain implements AsciidoctorToolchain {

    @Inject
    ExampleToolchain(String name, Project tempProjectRef) { // <.>
        super(name, tempProjectRef)

        registeredOutputFormatters.registerFactory(ExampleOutputFormatter) { // <.>
            objectFactory.newInstance(ExampleOutputFormatter, it, owner) // <.>
        }

        registeredOutputFormatters.create('text',ExampleOutputFormatter)  // <.>
    }

    @Override
    Class<?> getToolchainClass() {
        ExampleToolchain.class // <.>
    }
}
// end::minimal-toolchain[]
