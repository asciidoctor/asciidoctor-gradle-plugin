package org.asciidoctor.gradle.model5.jvm.core.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.core.AsciidoctorJDocbook

@CompileStatic
class DefaultAsciidoctorJDocbook implements AsciidoctorJDocbook {
    final String name

    DefaultAsciidoctorJDocbook(String name) {
        this.name = name
    }
}
