package org.asciidoctor.gradle.model5.core.internal.attributes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AttributeType
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Treat the attribute value as a formatted date string
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DateType implements AttributeType {

    private final Object value

    DateType(Object value) {
        this.value = value
    }

    /**
     * Render to a string value
     * @return String value. Can be {@code null.}
     */
    @Override
    String render() {
       AttributeUtils.resolveDateType(value)
    }
}
