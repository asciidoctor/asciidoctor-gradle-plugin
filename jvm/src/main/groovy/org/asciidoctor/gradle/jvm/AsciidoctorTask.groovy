/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.jvm

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.OutputOptions
import org.gradle.api.Action
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.util.PatternSet
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.core.ClosureUtils

import javax.inject.Inject

import static org.asciidoctor.gradle.base.AsciidoctorUtils.setConvention

/** Standard generic task for converting Asciidoctor documents.
 *
 * For convention and cmopatibility with older releases of this pugin, the default source directory
 * is {@code "src/docs/asciidoc"} if the task is named {@code aciidoctor}. For all other instances
 * of this task type the default source directory is {@code "src/docs/${task.name.capitalize()}"}.
 *
 * In a similar fasion the default output directory is either {@code "${buildDir}/asciidoc"} or
 * {@code "${buildDir}/asciidoc${task.name.capitalize()}"}.
 *
 * @author Noam Tenne
 * @author Andres Almiray
 * @author Tom Bujok
 * @author Lukasz Pielak
 * @author Dmitri Vyazelenko
 * @author Benjamin Muschko
 * @author Dan Allen
 * @author Rob Winch
 * @author Stefan Schlott
 * @author Stephan Classen
 * @author Marcus Fihlon
 * @author Schalk W. Cronjé
 * @author Robert Panzer
 * @author Gary Hale
 */
@SuppressWarnings(['MethodCount', 'Instanceof'])
@CompileStatic
@CacheableTask
class AsciidoctorTask extends AbstractAsciidoctorTask {

    /**
     * Configures output options for this task.
     *
     * @param cfg Closure which will delegate to a {@link org.asciidoctor.gradle.base.OutputOptions} instance.
     */
    void outputOptions(@DelegatesTo(OutputOptions) Closure cfg) {
        ClosureUtils.configureItem(outputOptions, cfg)
    }

    /** Configures output options for this task.
     *
     * @param cfg Action which will be passed an instances of {@link org.asciidoctor.gradle.base.OutputOptions}
     *   to configure.
     */
    void outputOptions(Action<OutputOptions> cfg) {
        cfg.execute(outputOptions)
    }

    @Inject
    AsciidoctorTask(WorkerExecutor we) {
        super(we)
        final String taskPrefix = 'asciidoctor'
        executionMode = JAVA_EXEC // TODO: Remove this as it is just for testing
        String folderName
        if (name.startsWith(taskPrefix)) {
            folderName = name.replaceFirst(taskPrefix, 'asciidoc')
        } else {
            folderName = "asciidoc${name.capitalize()}"
        }
        final String safeFolderName = projectOperations.fsOperations.toSafeFileName(folderName)
        setConvention(project, sourceDirProperty, project.layout.projectDirectory.dir("src/docs/${folderName}"))
        setConvention(outputDirProperty, project.layout.buildDirectory.dir("docs/${safeFolderName}"))
    }

    @Override
    void exec() {
        if (!baseDirConfigured) {
            if (attributes.keySet() in ['docinfo', 'docinfo1', 'docinfo2']) {
                logger.warn('You are using docinfo attributes, but a base directory strategy has not been configured.' +
                        'It is recommended that you set baseDirFollowsSourceDir() in your task.')
            }
        }
        super.exec()
    }

    /** The default pattern set for secondary sources baced upon the configured backends.
     *
     * If the backends contain {@code docbook} then {@code *docbook*.xml} is added.
     * If the backend contain {@code html5} then {@code *docbook*.html} is added.
     *
     * @return Default pattern set.
     */
    @Override
    PatternSet getDefaultSecondarySourceDocumentPattern() {
        PatternSet ps = super.defaultSecondarySourceDocumentPattern

        if (backends().find { it.startsWith('html') }) {
            ps.include '*docinfo*.html'
        }
        if (backends().find { it.startsWith('docbook') }) {
            ps.include '*docinfo*.xml'
        }

        ps
    }
}

