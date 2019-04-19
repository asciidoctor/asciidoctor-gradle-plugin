/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.js.base

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AbstractImplementationEngineExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

import static org.ysb33r.grolifant.api.StringUtils.stringize

/**
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
abstract class AbstractAsciidoctorJSExtension extends AbstractImplementationEngineExtension {

    public final static String DEFAULT_ASCIIDOCTORJS_VERSION = '2.0.2'
    public final static String DEFAULT_DOCBOOK_VERSION = '2.0.0'

    private Object version = DEFAULT_ASCIIDOCTORJS_VERSION
    private Optional<Object> docbookVersion

    /** Version of AsciidoctorJS that should be used.
     *
     */
    String getVersion() {
        if (task) {
            this.version ? stringize(this.version) : extFromProject.getVersion()
        } else {
            stringize(this.version)
        }
    }

    /** Set a new version to use.
     *
     * @param v New version to be used. Can be of anything that be resolved by {@link org.ysb33r.grolifant.api.StringUtils.stringize}.
     */
    void setVersion(Object v) {
        this.version = v
    }

    /** Version of the Docbook converter that should be used.
     *
     * @return Version of extension DSL or {@code null} if extensions will not be used.
     */
    String getDocbookVersion() {
        if (task) {
            if (this.docbookVersion != null && this.docbookVersion.present) {
                stringize(this.docbookVersion.get())
            } else {
                extFromProject.docbookVersion
            }
        } else {
            this.docbookVersion?.present ? stringize(this.docbookVersion.get()) : null
        }
    }

    /** Set a new Docbook converter version to use.
     *
     * Implies {@link #useDocbook}, but sets a custom version rather than a default.
     *
     * @param v Groovy DSL version.
     */
    void setDocbookVersion(Object v) {
        this.docbookVersion = Optional.of(v)
    }

    /** Enables Docbook conversion with whichever docbook version is the default.
     *
     */
    void useDocbook() {
        setDocbookVersion(DEFAULT_DOCBOOK_VERSION)
    }

    /** A configuration of packages related to asciidoctor.js
     *
     * @return Configuration of resolvable packages.
     */
    abstract Configuration getConfiguration()

    /** Extension is attached to a project.
     *
     * @param project
     */
    protected AbstractAsciidoctorJSExtension(Project project) {
        super(project)
    }

    /** Attach extension to a task.
     *
     * @param task
     */
    protected AbstractAsciidoctorJSExtension(Task task, final String name) {
        super(task, name)
    }

    private AbstractAsciidoctorJSExtension getExtFromProject() {
        task ? (AbstractAsciidoctorJSExtension) projectExtension : this
    }
}
