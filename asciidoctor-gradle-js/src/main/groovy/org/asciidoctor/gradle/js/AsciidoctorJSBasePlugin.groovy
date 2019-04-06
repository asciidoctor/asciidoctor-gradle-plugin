package org.asciidoctor.gradle.js

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSBasePlugin implements Plugin<Project> {

    public static final String TASK_GROUP = 'Documentation'

    @Override
    void apply(Project project) {
        project.extensions.create( AsciidoctorJSExtension.NAME, AsciidoctorJSExtension, project )
        project.extensions.create( AsciidoctorJSNodeExtension.NAME, AsciidoctorJSNodeExtension, project )
        project.extensions.create( AsciidoctorJSNpmExtension.NAME, AsciidoctorJSNpmExtension, project )
    }
}
