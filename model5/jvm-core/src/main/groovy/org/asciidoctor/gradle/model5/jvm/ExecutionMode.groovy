package org.asciidoctor.gradle.model5.jvm

import groovy.transform.CompileStatic

/**
 * Worker execution modes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
enum ExecutionMode {

    /**
     * Executes in the Gradle process, but with classpath isolation
     */
    IN_PROCESS,

    /**
     * Executes outside of the Gradle process.
     */
    OUT_OF_PROCESS

    static ExecutionMode of(String ver) {
        valueOf(ver.replaceAll(~/-/,'_').toUpperCase(Locale.US))
    }
}