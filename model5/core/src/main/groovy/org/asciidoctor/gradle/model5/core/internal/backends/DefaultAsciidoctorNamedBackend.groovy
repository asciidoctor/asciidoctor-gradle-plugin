package org.asciidoctor.gradle.model5.core.internal.backends

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend

/**
 * A default implementation of a named backend.
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorNamedBackend implements AsciidoctorNamedBackend {
    final String name
    final String backend

    DefaultAsciidoctorNamedBackend(String name, String backend) {
        this.name = name
        this.backend = backend
    }
}
