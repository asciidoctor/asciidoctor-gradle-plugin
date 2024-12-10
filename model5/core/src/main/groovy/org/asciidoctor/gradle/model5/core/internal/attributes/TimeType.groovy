package org.asciidoctor.gradle.model5.core.internal.attributes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AttributeType

/**
 * Treat the attribute value as a formatted time string
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class TimeType implements AttributeType {
    private final Object value

    TimeType(Object value) {
        this.value = value
    }
    /**
     * Render to a string value
     * @return String value. Can be {@code null.}
     */
    @Override
    String render() {
        AttributeUtils.resolveTimeType(value)
    }
}
