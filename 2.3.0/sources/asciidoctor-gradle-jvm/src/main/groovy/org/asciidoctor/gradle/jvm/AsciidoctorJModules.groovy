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
package org.asciidoctor.gradle.jvm

import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.gradle.api.Action
import org.ysb33r.grolifant.api.StringUtils

import static org.ysb33r.grolifant.api.ClosureUtils.configureItem

/** Define versions for standard AsciidoctorJ modules.
 *
 * @since 2.2.0
 */
class AsciidoctorJModules {
    private final AsciidoctorModuleDefinition pdf
    private final AsciidoctorModuleDefinition epub
    private final AsciidoctorModuleDefinition diagram
    private final AsciidoctorModuleDefinition groovyDsl

    AsciidoctorJModules(AsciidoctorJExtension asciidoctorjs) {
        this.pdf = Module.of(AsciidoctorJExtension.DEFAULT_PDF_VERSION)
        this.epub = Module.of(AsciidoctorJExtension.DEFAULT_EPUB_VERSION)
        this.diagram = Module.of(AsciidoctorJExtension.DEFAULT_DIAGRAM_VERSION) { value ->
            if (value != null) {
                asciidoctorjs.requires('asciidoctor-diagram')
            }
        }
        this.groovyDsl = Module.of(AsciidoctorJExtension.DEFAULT_GROOVYDSL_VERSION)
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
        private Optional<Object> version = Optional.empty()
        private final Object defaultVersion
        private final Action<Object> setAction

        static Module of(final Object defaultVersion) {
            new Module(defaultVersion)
        }

        static Module of(final Object defaultVersion, Closure setAction) {
            new Module(defaultVersion, setAction as Action<Object>)
        }

        private Module(final Object defaultVersion, Action<Object> setAction = null) {
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
    }
}
