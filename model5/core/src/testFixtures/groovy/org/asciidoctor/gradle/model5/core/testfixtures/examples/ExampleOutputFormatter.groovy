package org.asciidoctor.gradle.model5.core.testfixtures.examples

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AbstractOutputFormatter
import org.asciidoctor.gradle.model5.core.AsciidoctorPublication
import org.asciidoctor.gradle.model5.core.AsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.AsciidoctorOutputFormatter
import org.asciidoctor.gradle.model5.core.ToolchainUtils
import org.gradle.api.Project

import javax.inject.Inject

@CompileStatic
class ExampleOutputFormatter extends AbstractOutputFormatter implements AsciidoctorOutputFormatter {

    private final ExampleToolchain toolchain

    @Inject
    ExampleOutputFormatter(String name, ExampleToolchain toolchain, Project tempProjectReference) {
        super(name, tempProjectReference)
        this.toolchain = toolchain
    }

    @Override
    void registerTasksIfAbsent(AsciidoctorPublication publication) {
        final taskName = getAsciidoctorTaskName(publication)
        ToolchainUtils.registerOnce(taskName, ExampleAsciidoctorTask, objectFactory) {
            it.group = 'Documentation'
            it.description = 'Example output generator'
            ToolchainUtils.configureFromStandardPublicationProviders(it, publication)
        }
    }

    @Override
    Class<?> getOutputFormatterClass() {
        ExampleOutputFormatter.class
    }

    @Override
    protected AsciidoctorToolchain getAsciidoctorToolchain() {
        this.toolchain
    }
}
