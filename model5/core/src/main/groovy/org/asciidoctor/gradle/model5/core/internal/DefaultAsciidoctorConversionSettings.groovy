package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.core.DocType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

import javax.inject.Inject

/**
 * Default implementation of conversion settings used by a an
 * {@link org.asciidoctor.gradle.model5.core.AsciidoctorLauncher}
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorConversionSettings implements AsciidoctorConversionSettings {

    final SetProperty<File> sourceFiles
    final Property<AsciidoctorNamedBackend> backend
    final Property<File> baseDir
    final Property<File> destinationDir
    final MapProperty<String, String> attributes
    final Property<DocType> docType

    @Inject
    DefaultAsciidoctorConversionSettings(ObjectFactory objectFactory) {
        this.sourceFiles = objectFactory.setProperty(File)
        this.backend = objectFactory.property(AsciidoctorNamedBackend)
        this.baseDir = objectFactory.property(File)
        this.destinationDir = objectFactory.property(File)
        this.attributes = objectFactory.mapProperty(String, String)
        this.docType = objectFactory.property(DocType)
    }
}
