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
package org.asciidoctor.gradle.js.base.internal

import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.gradle.api.Action

import static org.ysb33r.grolifant.api.StringUtils.stringize

/** A single configurable asciidoctor.js module.
 *
 * @author Schalk W. Cronjé
 * @since 3.0.0
 */
class AsciidoctorJSModule implements AsciidoctorModuleDefinition {

    final String name

    private Optional<Object> version = Optional.empty()
    private final Object defaultVersion
    private final Action<Object> setAction

    static AsciidoctorJSModule of(final String name, final Object defaultVersion) {
        new AsciidoctorJSModule(name, defaultVersion)
    }

    static AsciidoctorJSModule of(final String name, final Object defaultVersion, Closure setAction) {
        new AsciidoctorJSModule(name, defaultVersion, setAction as Action<Object>)
    }

    AsciidoctorJSModule(final String name, final Object defaultVersion, Action<Object> setAction = null) {
        this.name = name
        this.defaultVersion = defaultVersion
        this.setAction = setAction
    }

    @Override
    void use() {
        setVersion(this.defaultVersion)
    }

    @Override
    void setVersion(Object o) {
        this.version = Optional.of(o)
        if (setAction) {
            setAction.execute(o)
        }
    }

    @Override
    @SuppressWarnings('ConfusingMethodName')
    void version(Object o) {
        setVersion(o)
    }

    @Override
    String getVersion() {
        this.version.present ? stringize(this.version.get()) : null
    }

    /** Whether the component has been allocated a version.
     *
     * @return {@code true} if the component has been defined
     */
    @Override
    boolean isDefined() {
        version.present
    }
}