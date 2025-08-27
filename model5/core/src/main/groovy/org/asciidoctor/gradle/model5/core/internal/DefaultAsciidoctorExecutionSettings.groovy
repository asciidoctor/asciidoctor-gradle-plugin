package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorExecutionsSettings
import org.asciidoctor.gradle.model5.core.SafeMode
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

import javax.inject.Inject

/**
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorExecutionSettings implements AsciidoctorExecutionsSettings {

    final Property<SafeMode> safeMode

    @Inject
    DefaultAsciidoctorExecutionSettings(ObjectFactory objectFactory) {
        safeMode = objectFactory.property(SafeMode)
    }
}
