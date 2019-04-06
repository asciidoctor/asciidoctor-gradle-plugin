package org.asciidoctor.gradle.js

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.grolifant.api.TaskProvider

import static org.asciidoctor.gradle.js.AsciidoctorJSBasePlugin.TASK_GROUP

/** Adds a task called asciidoctor.
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.with {
            apply plugin: 'org.asciidoctor.js.base'

            Action<AsciidoctorTask> asciidoctorDefaults = new Action<AsciidoctorTask>() {
                @Override
                void execute(AsciidoctorTask asciidoctorTask) {
                    asciidoctorTask.with {
                        group = TASK_GROUP
                        description = 'Generic task to convert AsciiDoc files and copy related resources'
                    }
                }
            }

            TaskProvider.registerTask(project,'asciidoctor', AsciidoctorTask).configure((Action)asciidoctorDefaults)
        }
    }
}