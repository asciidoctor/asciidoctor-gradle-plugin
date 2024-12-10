package org.asciidoctor.gradle.model5.core;

/**
 * Flag attributes as certain type.
 *
 * <p>
 *     For instance it can be a flag, a time or a date.
 * </p>
 */
public interface AttributeType {
    /**
     * Render to a string value
     * @return String value. Can be {@code null.}
     */
    String render();
}
