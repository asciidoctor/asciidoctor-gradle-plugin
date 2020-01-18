/*
 * Copyright 2013-2020 the original author or authors.
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
import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

import static org.ysb33r.grolifant.api.ClosureUtils.configureItem
import static org.ysb33r.grolifant.api.StringUtils.stringize

/**
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
abstract class AbstractAsciidoctorJSExtension extends AbstractImplementationEngineExtension {

    private Object version
    private final AsciidoctorJSModules modules

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
     * @param v New version to be used. Can be of anything that be resolved by
     * {@link org.ysb33r.grolifant.api.StringUtils.stringize}.
     */
    void setVersion(Object v) {
        this.version = v
    }

    /** Additional AsciidoctorJ modules to be configured.
     *
     * @return Module definition object.
     */
    AsciidoctorJSModules getModules() {
        this.modules
    }

    /** Configure modules via a closure.
     *
     * @param cfg Configurating closure
     */
    @SuppressWarnings('ConfusingMethodName')
    void modules(@DelegatesTo(AsciidoctorJSModules) Closure cfg) {
        configureItem(this.modules, cfg)
    }

    /** Configure modules via an {@code Action}.
     *
     * @param cfg Configurating {@code Action}
     */
    @SuppressWarnings('ConfusingMethodName')
    void modules(Action<AsciidoctorJSModules> cfg) {
        cfg.execute(this.modules)
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
        super(project, 'asciidoctorjs-extension')
        this.modules = createModulesConfiguration()
        this.version = defaultVersionMap['asciidoctorjs']
    }

    /** Attach extension to a task.
     *
     * @param task
     */
    protected AbstractAsciidoctorJSExtension(Task task, final String name) {
        super(task, name)
        this.modules = createModulesConfiguration()
    }

    /** Obtains the correct module version.
     *
     * If this extension is attached to a task, then check whether the local
     * module is defined first, otherwise defer to the global project extension.
     *
     * @param mod Module to interrogate
     *
     * @return Module version or {@code null} if the componenet is not required.
     */
    protected String moduleVersion(final AsciidoctorModuleDefinition mod) {
        if (task) {
            AsciidoctorModuleDefinition local = this.modules.getByName(mod.name)
            if (local.defined) {
                local.version
            } else {
                extFromProject.modules.getByName(mod.name).version
            }
        } else {
            extFromProject.modules.getByName(mod.name).version
        }
    }

    /** Creates a new modules block.
     *
     * @return Modules block that can be attached to this extension.
     */
    @SuppressWarnings('FactoryMethodName')
    abstract protected AsciidoctorJSModules createModulesConfiguration()

    private AbstractAsciidoctorJSExtension getExtFromProject() {
        task ? (AbstractAsciidoctorJSExtension) projectExtension : this
    }

}
