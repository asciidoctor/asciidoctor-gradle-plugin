/*
 * Copyright 2013 - 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.gradle.js.nodejs

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.OutputOptions
import org.asciidoctor.gradle.base.ProblemReports
import org.gradle.api.Action
import org.gradle.api.tasks.CacheableTask
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

import static groovy.lang.Closure.DELEGATE_FIRST
import static org.asciidoctor.gradle.base.AsciidoctorUtils.setConvention
import static org.asciidoctor.gradle.base.ProblemReports.ASCIIDOCTOR_JS_PROBLEM_ID
import static org.asciidoctor.gradle.base.ProblemReports.TOOLCHAIN_JS
import static org.asciidoctor.gradle.base.ProblemReports.publicationName

/** Build using {@code asciidoctor.js}.
 *
 * @author Schalk W. Cronjé
 * @author Gary Hale
 *
 * @since 3.0
 */
@CompileStatic
@CacheableTask
class AsciidoctorTask extends AbstractAsciidoctorNodeJSTask {

    @Inject
    AsciidoctorTask(WorkerExecutor we) {
        super(we)
        final String taskPrefix = 'asciidoctor'
        String folderName
        if (name.startsWith(taskPrefix)) {
            folderName = name.replaceFirst(taskPrefix, 'asciidoc')
        } else {
            folderName = "asciidoc${name.capitalize()}"
        }
        final String safeFolderName = projectOperations.fsOperations.toSafeFileName(folderName)
        setConvention(project, sourceDirProperty, project.layout.projectDirectory.dir("src/docs/${folderName}"))
        setConvention(outputDirProperty, project.layout.buildDirectory.dir("docs/${safeFolderName}"))

        ProblemReports.report(
            problemReporter(),
            ASCIIDOCTOR_JS_PROBLEM_ID,
            ProblemReports.taskProblemDetail(name, 'asciidoctor.js'),
            ProblemReports.replacePlugin(
                project, name,
                'js.classic',
                'js',
                TOOLCHAIN_JS
            )
        )
    }

    /** Configures output options for this task.
     *
     * @param cfg Closure which will delegate to a {@link org.asciidoctor.gradle.base.OutputOptions} instance.
     */
    void outputOptions(Closure cfg) {
        Closure configurator = (Closure) cfg.clone()
        configurator.delegate = this.configuredOutputOptions
        configurator.resolveStrategy = DELEGATE_FIRST
        configurator.call()
        reportOutputOptions()
    }

    /** Configures output options for this task.
     *
     * @param cfg Action which will be passed an instances of {@link org.asciidoctor.gradle.base.OutputOptions}
     *   to configure.
     */
    void outputOptions(Action<OutputOptions> cfg) {
        cfg.execute(this.configuredOutputOptions)
        reportOutputOptions()
    }

    private void reportOutputOptions() {
        final details = 'Output options need to be migrated to the new model5'
        final solution = """
        In the classic model, output options could be set on the task.
        It now needs to migrate to the model definition.

        'setSeparateOutputDirs' is no longer supported. The new model uses the concept of output formatters
        and each of them outputs to a separate directory.

        'backends' are now handled as an `output` definitions where the first parameter is the name of the
        toolchain and the second is the name of the output formatter. In most cases the name of the backend
        will match the name of the output formatter.

        asciidoc {
            publications {
                ${publicationName(name)} {
                    sourceSet {
                        // Configure source details here
                    }
                    // The backend strings now become the 2nd parameter
                    output( '${TOOLCHAIN_JS}', 'html' )
                    output( '${TOOLCHAIN_JS}', 'docbook' )
                }
            }
        }
        """.stripIndent()

        ProblemReports.report(problemReporter(), ASCIIDOCTOR_JS_PROBLEM_ID, details, solution)
    }
}
