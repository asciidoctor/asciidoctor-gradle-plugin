package org.asciidoctor.gradle.model5.jvm.testfixtures

import org.asciidoctor.gradle.model5.jvm.internal.formatters.DefaultAsciidoctorjHtml5
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN

class AsciidoctorjHtmlIntegrationSpecification extends IntegrationSpecification {

    File outputDir
    String taskName = 'asciidoctorHtml'
    void setup() {
        outputDir = new File(buildDir, 'docs/asciidoc/html')
    }

    void writeHtmlBasedBuildFile() {
        writeBasicBuildFileGroovy(['org.asciidoctor.jvm'])
        addOutputToSourceSetGroovy(DEFAULT_TOOLCHAIN, DefaultAsciidoctorjHtml5.DEFAULT_NAME, DEFAULT_PUBLICATION)
    }
}