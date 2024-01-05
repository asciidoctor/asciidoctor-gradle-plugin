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
package org.asciidoctor.gradle.js.base.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.asciidoctor.gradle.base.ModuleNotFoundException
import org.asciidoctor.gradle.js.base.AsciidoctorJSModules
import org.gradle.api.Action
import org.ysb33r.grolifant.api.core.ProjectOperations

import static org.ysb33r.grolifant.api.core.ClosureUtils.configureItem

/** Define versions for standard AsciidoctorJS modules.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@SuppressWarnings(['ConfusingMethodName', 'ClassName'])
@CompileStatic
class BaseAsciidoctorJSModules implements AsciidoctorJSModules {
    private final ProjectOperations projectOperations
    private final AsciidoctorModuleDefinition docbook
    private final Map<String, AsciidoctorModuleDefinition> index = new TreeMap<String, AsciidoctorModuleDefinition>()
    private Action<AsciidoctorJSModules> updater

    /**
     * Creates a module definition that is attached to a specific asciidoctorjs
     * extension.
     *
     * @param asciidoctorjs Extension that this module is attached to.
     *
     * @since 4.0
     */
    @SuppressWarnings('UnnecessarySetter')
    BaseAsciidoctorJSModules(
            ProjectOperations projectOperations,
            AsciidoctorModuleDefinition docbook
    ) {
        this.projectOperations = projectOperations
        this.docbook = docbook
        index.put(docbook.name, docbook)

        if (docbook instanceof VersionUpdateTrigger) {
            docbook.setUpdateAction { owner.updateNow() }
        }
    }

    /**
     * Configure docbook via closure.
     *
     * @param cfg Configurating closure
     */
    @Override
    void docbook(@DelegatesTo(AsciidoctorModuleDefinition) Closure cfg) {
        configureItem(this.docbook, cfg)
    }

    /**
     * Configure docbook via an action.
     *
     * @param cfg Configurator
     */
    @Override
    void docbook(Action<AsciidoctorModuleDefinition> cfg) {
        cfg.execute(this.docbook)
    }

    /**
     * The Docbook module
     *
     * @return Access to the Docbook module. Never {@code null}.
     */
    @Override
    AsciidoctorModuleDefinition getDocbook() {
        this.docbook
    }

    /**
     * For the modules that are configured in both module sets,
     * compare to see if the versions are the same
     *
     * @param other Other module set to compare.
     */
    @Override
    boolean isSetVersionsDifferentTo(AsciidoctorJSModules other) {
        different(docbook, other.docbook)
    }

    /**
     * Returns a module by name
     *
     * @param name Name of module
     * @throws {@link org.asciidoctor.gradle.base.ModuleNotFoundException} when the module is not registered.
     * @return Module
     */
    @Override
    AsciidoctorModuleDefinition getByName(String name) {
        if (index.containsKey(name)) {
            index[name]
        } else {
            throw new ModuleNotFoundException("${name} is not a valid module")
        }
    }

    void onUpdate(Action<AsciidoctorJSModules> callback) {
        this.updater = callback
    }

    protected void updateNow() {
        if (updater) {
            updater.execute(this)
        }
    }

    private boolean different(AsciidoctorModuleDefinition lhs, AsciidoctorModuleDefinition rhs) {
        lhs.version != null && rhs.version != null && stringize(lhs.version) != stringize(rhs.version)
    }

    private String stringize(Object stringy) {
        projectOperations.stringTools.stringize(stringy)
    }
}
