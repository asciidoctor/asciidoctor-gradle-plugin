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
package org.asciidoctor.gradle.jvm

import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.asciidoctor.gradle.base.ModuleNotFoundException
import org.gradle.api.Action
import org.ysb33r.grolifant.api.StringUtils

import static org.ysb33r.grolifant.api.ClosureUtils.configureItem

/** Define versions for standard AsciidoctorJ modules.
 *
 * @since 2.2.0
 */
class AsciidoctorJModules {

    private final Map<String, AsciidoctorModuleDefinition> index = new TreeMap<String, AsciidoctorModuleDefinition>()

    private final AsciidoctorModuleDefinition pdf
    private final AsciidoctorModuleDefinition epub
    private final AsciidoctorModuleDefinition diagram
    private final AsciidoctorModuleDefinition leanpub
    private final AsciidoctorModuleDefinition groovyDsl

    AsciidoctorJModules(AsciidoctorJExtension asciidoctorjs, Map<String, String> defaultVersions) {
        this.pdf = Module.of('pdf', defaultVersions['asciidoctorj.pdf'])
        this.epub = Module.of('epub', defaultVersions['asciidoctorj.epub'])
        this.leanpub = Module.of('leanpub', defaultVersions['asciidoctorj.leanpub'])
        this.diagram = Module.of('diagram', defaultVersions['asciidoctorj.diagram']) { value ->
            if (value != null) {
                asciidoctorjs.requires('asciidoctor-diagram')
            }
        }
        this.groovyDsl = Module.of('groovyDsl', defaultVersions['asciidoctorj.groovydsl'])

        [pdf, epub, diagram, groovyDsl, leanpub].each {
            index[it.name] = it
        }
    }

    void pdf(@DelegatesTo(AsciidoctorModuleDefinition) Closure cfg) {
        configureItem(this.pdf, cfg)
    }

    AsciidoctorModuleDefinition getPdf() {
        this.pdf
    }

    void epub(@DelegatesTo(AsciidoctorModuleDefinition) Closure cfg) {
        configureItem(this.epub, cfg)
    }

    AsciidoctorModuleDefinition getEpub() {
        this.epub
    }

    void leanpub(@DelegatesTo(AsciidoctorModuleDefinition) Closure cfg) {
        configureItem(this.leanpub, cfg)
    }

    AsciidoctorModuleDefinition getLeanpub() {
        this.leanpub
    }

    void diagram(@DelegatesTo(AsciidoctorModuleDefinition) Closure cfg) {
        configureItem(this.diagram, cfg)
    }

    AsciidoctorModuleDefinition getDiagram() {
        this.diagram
    }

    void groovyDsl(@DelegatesTo(AsciidoctorModuleDefinition) Closure cfg) {
        configureItem(this.groovyDsl, cfg)
    }

    AsciidoctorModuleDefinition getGroovyDsl() {
        this.groovyDsl
    }

    private static class Module implements AsciidoctorModuleDefinition {

        final String name
        private Optional<Object> version = Optional.empty()
        private final Object defaultVersion
        private final Action<Object> setAction

        static Module of(final String name, final Object defaultVersion) {
            new Module(name, defaultVersion)
        }

        static Module of(final String name, final Object defaultVersion, Closure setAction) {
            new Module(name, defaultVersion, setAction as Action<Object>)
        }

        private Module(final String name, final Object defaultVersion, Action<Object> setAction = null) {
            if (defaultVersion == null) {
                throw new ModuleNotFoundException("Default version for ${name} cannot be null")
            }
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
        void version(Object o) {
            setVersion(o)
        }

        @Override
        String getVersion() {
            this.version.present ? StringUtils.stringize(this.version.get()) : null
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
}
