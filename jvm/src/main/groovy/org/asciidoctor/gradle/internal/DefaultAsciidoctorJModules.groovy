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
package org.asciidoctor.gradle.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.asciidoctor.gradle.base.ModuleNotFoundException
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.asciidoctor.gradle.jvm.AsciidoctorJModules
import org.gradle.api.Action
import org.ysb33r.grolifant.api.core.ProjectOperations

import static org.ysb33r.grolifant.api.core.ClosureUtils.configureItem

/**
 * Implementation of {@Link AsciidoctorJModules}.
 *
 * @since 4.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class DefaultAsciidoctorJModules implements AsciidoctorJModules {

    private final Map<String, AsciidoctorModuleDefinition> index = new TreeMap<String, AsciidoctorModuleDefinition>()

    private final AsciidoctorModuleDefinition pdf
    private final AsciidoctorModuleDefinition epub
    private final AsciidoctorModuleDefinition diagram
    private final AsciidoctorModuleDefinition leanpub
    private final AsciidoctorModuleDefinition groovyDsl
    private Action<AsciidoctorJModules> updater

    // TODO: Remove this warning - it is only there temporarily.
    @SuppressWarnings('UnusedMethodParameter')
    DefaultAsciidoctorJModules(
            ProjectOperations po,
            AsciidoctorJExtension asciidoctorjs,
            Map<String, String> defaultVersions
    ) {
        final defaultUpdater = { value -> owner.updateNow() }
        this.pdf = Module.of(po, 'pdf', defaultVersions['asciidoctorj.pdf'], defaultUpdater)
        this.epub = Module.of(po, 'epub', defaultVersions['asciidoctorj.epub'], defaultUpdater)
        this.leanpub = Module.of(po, 'leanpub', defaultVersions['asciidoctorj.leanpub'], defaultUpdater)
        this.diagram = Module.of(po, 'diagram', defaultVersions['asciidoctorj.diagram']) { value ->
            if (value != null) {
                owner.updateNow()
                asciidoctorjs.requires('asciidoctor-diagram')
            }
        }
        this.groovyDsl = Module.of(po, 'groovyDsl', defaultVersions['asciidoctorj.groovydsl'], defaultUpdater)

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

    /**
     * Registers a callback for when modules are activated or changed after initial creation.
     *
     * @param callback Callback
     */
    void onUpdate(Action<AsciidoctorJModules> callback) {
        this.updater = callback
    }

    void updateNow() {
        if (updater) {
            updater.execute(this)
        }
    }

    private static class Module implements AsciidoctorModuleDefinition {

        final String name
        private Optional<Object> version = Optional.empty()
        private final Object defaultVersion
        private final Action<Object> setAction
        private final ProjectOperations projectOperations

        static Module of(final ProjectOperations po, final String name, final Object defaultVersion) {
            new Module(po, name, defaultVersion)
        }

        static Module of(
                final ProjectOperations po,
                final String name,
                final Object defaultVersion,
                Closure setAction
        ) {
            new Module(po, name, defaultVersion, setAction as Action<Object>)
        }

        private Module(
                ProjectOperations po,
                final String name,
                final Object defaultVersion,
                Action<Object> setAction = null
        ) {
            if (defaultVersion == null) {
                throw new ModuleNotFoundException("Default version for ${name} cannot be null")
            }
            this.projectOperations = po
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
            this.version.present ? projectOperations.stringTools.stringize(this.version.get()) : null
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
