package org.asciidoctor.gradle.model5.jvm.core.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.OutputFormatter
import org.asciidoctor.gradle.model5.jvm.core.AsciidoctorJHtml5
import org.asciidoctor.gradle.model5.jvm.core.AsciidoctorJToolchain

import javax.inject.Inject

@CompileStatic
class DefaultAsciidoctorJHtml5 extends AbstractAsciidoctorJFormatter implements AsciidoctorJHtml5 {

    @Inject
    DefaultAsciidoctorJHtml5(String name, AsciidoctorJToolchain tc) {
       super(name,tc)
    }
}
