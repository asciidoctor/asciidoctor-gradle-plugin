package org.asciidoctor.gradle.js

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.ysb33r.gradle.nodejs.NodeJSExtension

/** An extension to configure Node.js.
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSNodeExtension extends NodeJSExtension {
    public final static String NAME = 'asciidoctorNodejs'

    AsciidoctorJSNodeExtension(Project project) {
        super(project)
    }

    AsciidoctorJSNodeExtension(Task task) {
        super(task, NAME)
    }
}
