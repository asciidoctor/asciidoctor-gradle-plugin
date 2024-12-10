package org.asciidoctor.gradle.model5.core.errors

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * Thrown when a specific type cannot be supported as an attribute.
 */
@CompileStatic
@InheritConstructors
class UnsupportedAttributeType extends RuntimeException {
}
