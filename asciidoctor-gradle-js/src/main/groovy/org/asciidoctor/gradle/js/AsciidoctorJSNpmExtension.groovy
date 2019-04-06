package org.asciidoctor.gradle.js

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.ysb33r.gradle.nodejs.NpmExtension

/** An extension to configure Npm.
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSNpmExtension extends NpmExtension {
    public final static String NAME = 'asciidoctorNpm'

    AsciidoctorJSNpmExtension(Project project) {
        super(project)
        localConfig = {new File(project.buildDir,"/tmp/${NAME}/npmrc")}
        homeDirectory = {new File(project.buildDir,"/tmp/${NAME}")}
    }

    AsciidoctorJSNpmExtension(Task task) {
        super(task, NAME)
    }

    @Override
    protected String getNodeJsExtensionName() {
        AsciidoctorJSNodeExtension.NAME
    }

}
