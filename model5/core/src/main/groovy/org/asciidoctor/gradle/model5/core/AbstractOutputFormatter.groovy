package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

@CompileStatic
abstract class AbstractOutputFormatter implements AsciidoctorOutputFormatter {

    final String name
    protected final ObjectFactory objectFactory

    @Override
    String getAsciidoctorTaskName(AsciidoctorPublication publication) {
        ToolchainUtils.asciidoctorTaskName(asciidoctorToolchain, this, publication)
    }

    protected AbstractOutputFormatter(String name, Project tempProjectReference) {
        this.name = name
        this.objectFactory = tempProjectReference.objects
    }

    /**
     * Obtain the tool chain that this formatter is attached to.
     *
     * @return Toolchain instance.
     */
    protected abstract AsciidoctorToolchain getAsciidoctorToolchain()
}
