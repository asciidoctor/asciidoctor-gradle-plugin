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
package org.asciidoctor.gradle.base

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.ysb33r.grolifant.api.AbstractCombinedProjectTaskExtension

import static org.ysb33r.grolifant.api.StringUtils.stringize

/** Base class for implementing extensions in the Asciidoctor Gradle suite.
 *
 * This class is engine agnostic.
 *
 * @since 3.0
 */
@CompileStatic
abstract class AbstractImplementationEngineExtension extends AbstractCombinedProjectTaskExtension {

    private SafeMode safeMode
    private final Map<String, String> defaultVersionMap
    private final List<AsciidoctorAttributeProvider> attributeProviders = []
    private final Map<String, Object> attributes = [:]
    private boolean onlyTaskAttributes = false

    /** Returns the Asciidoctor SafeMode under which a conversion will be run.
     *
     * @return Asciidoctor Safe Mode
     */
    SafeMode getSafeMode() {
        (task && this.safeMode || !task) ? this.safeMode : extFromProject.safeMode
    }

    /** Set Asciidoctor safe mode.
     *
     * @param mode An instance of Asciidoctor SafeMode.
     */
    void setSafeMode(SafeMode mode) {
        this.safeMode = mode
    }

    /** Set Asciidoctor safe mode.
     *
     * @param mode A valid integer representing a Safe Mode
     */
    void setSafeMode(int mode) {
        this.safeMode = SafeMode.safeMode(mode)
    }

    /** Set Asciidoctor safe mode.
     *
     * @param mode A valid string representing a Safe Mode
     */
    void setSafeMode(String mode) {
        this.safeMode = SafeMode.valueOf(mode.toUpperCase())
    }

    /** Returns a list of additional attribute providers.
     *
     * @return List of providers. Can be empty. Never {@code null}.
     */
    List<AsciidoctorAttributeProvider> getAttributeProviders() {
        if (task) {
            this.attributeProviders.empty ? extFromProject.attributeProviders : this.attributeProviders
        } else {
            this.attributeProviders
        }
    }

    /** Returns all of the Asciidoctor options.
     *
     */
    Map<String, Object> getAttributes() {
        stringizeMapRecursive(this.attributes, onlyTaskAttributes) { AbstractImplementationEngineExtension it ->
            it.attributes
        }
    }

    /** Apply a new set of Asciidoctor attributes, clearing any attributes previously set.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will override the global attributes.
     *
     * @param m Map with new options
     */
    void setAttributes(Map m) {
        this.attributes.clear()
        this.attributes.putAll(m)

        if (task) {
            onlyTaskAttributes = true
        }
    }

    /** Add additional Asciidoctor attributes.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will use this attributes in the task in addition to any global attributes.
     *
     * @param m Map with new options
     */
    @SuppressWarnings('ConfusingMethodName')
    void attributes(Map m) {
        this.attributes.putAll(m)
    }

    /** Adds an additional attribute provider.
     *
     * @param provider
     */
    void attributeProvider(AsciidoctorAttributeProvider provider) {
        this.attributeProviders.add(provider)
    }

    /** Adds a closure as an additional attribute provider.
     *
     * @param provider A closure must return a Map<String,Object>
     */
    void attributeProvider(Closure provider) {
        attributeProvider(provider as AsciidoctorAttributeProvider)
    }

    protected AbstractImplementationEngineExtension(Project project, String moduleResourceName) {
        super(project)
        this.safeMode = SafeMode.UNSAFE
        this.attributes['gradle-project-name'] = project.name
        this.attributes['gradle-project-group'] = { project.group ?: '' }
        this.attributes['gradle-project-version'] = { project.version ?: '' }
        this.defaultVersionMap = ModuleVersionLoader.load(moduleResourceName)
    }

    protected AbstractImplementationEngineExtension(Task task, final String name) {
        super(task, name)
    }

    protected Map<String, String> getDefaultVersionMap() {
        if (task) {
            extFromProject.defaultVersionMap
        } else {
            this.defaultVersionMap
        }
    }

    protected Map<String, Object> stringizeMapRecursive(
        Map<String, Object> map,
        boolean fromTaskOnly,
        Closure<Map<String, Object>> other
    ) {
        if (!task || fromTaskOnly) {
            stringizeScalarMapItems(map)
        } else if (map.isEmpty()) {
            other.call(extFromProject)
        } else {
            Map<String, Object> newOptions = [:]
            newOptions.putAll(other.call(extFromProject))
            newOptions.putAll(map)
            stringizeScalarMapItems(newOptions)
        }
    }

    protected Collection<String> stringizeList(
        Collection<Object> list,
        boolean fromTaskOnly,
        Closure<Collection<String>> other
    ) {
        if (!task || fromTaskOnly) {
            stringize(list)
        } else if (list.isEmpty()) {
            other.call(extFromProject)
        } else {
            List<Object> newOptions = []
            newOptions.addAll(other.call(extFromProject))
            newOptions.addAll(list)
            stringize(newOptions)
        }
    }

    protected List<Object> stringizeScalarListItems(List<Object> list) {
        Transform.toList(list) { item ->
            switch (item) {
                case List:
                    return stringizeScalarListItems((List) item)
                case Map:
                    return stringizeScalarMapItems((Map) item)
                case boolean:
                case Boolean:
                    return (Boolean) item
                case File:
                    return ((File) item).absolutePath
                default:
                    return stringize(item)
            }
        }
    }

    protected Map<String, Object> stringizeScalarMapItems(Map<String, Object> map) {
        map.collectEntries { String key, Object item ->
            switch (item) {
                case List:
                    return [key, stringizeScalarListItems((List) item)]
                case Map:
                    return [key, stringizeScalarMapItems((Map) item)]
                case boolean:
                case Boolean:
                    return [key, ((Boolean) item)]
                case File:
                    return [key, ((File) item).absolutePath]
                default:
                    return [key, stringize(item)]
            }
        } as Map<String, Object>
    }

    private AbstractImplementationEngineExtension getExtFromProject() {
        task ? (AbstractImplementationEngineExtension) projectExtension : this
    }

}
