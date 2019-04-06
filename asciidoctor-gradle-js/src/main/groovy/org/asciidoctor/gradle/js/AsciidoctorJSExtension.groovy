package org.asciidoctor.gradle.js

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AbstractImplementationEngineExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.ysb33r.grolifant.api.AbstractCombinedProjectTaskExtension

/**
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSExtension extends AbstractImplementationEngineExtension {
    public final static String NAME = 'asciidoctorjs'
    public final static String DEFAULT_ASCIIDOCTORJS_VERSION = '2.0.0-rc.3'

    String version = DEFAULT_ASCIIDOCTORJS_VERSION

    AsciidoctorJSExtension(Project project) {
        super(project)
    }

    /** Attach extension to a task.
     *
     * @param task
     */
    AsciidoctorJSExtension(Task task) {
        super(task, NAME)
    }


    /** Returns the set of NPM packages to be included.
     */
    Set<String> getRequires() {
//        stringizeList(this.nodejsRequires, onlyTaskRequires) { AsciidoctorJSExtension it ->
//            it.requires.toList()
//        }.toSet()
        [] as Set<String>
    }

}
