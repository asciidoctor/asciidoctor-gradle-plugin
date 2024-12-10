package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic

/**
 * Holds information about a toolchain for display purposes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class ToolchainInformation implements Serializable {

    final String name
    final String className
    final Map<String,String> formatters

    ToolchainInformation(
            String name,
            String className,
            Map<String,String> formatters
    ) {
        this.name = name
        this.className = className
        this.formatters = formatters.asImmutable()
    }
}
