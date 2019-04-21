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
package org.asciidoctor.gradle.js.base.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition
import org.asciidoctor.gradle.js.base.AsciidoctorJSModules

import static org.ysb33r.grolifant.api.ClosureUtils.configureItem
import static org.ysb33r.grolifant.api.StringUtils.stringize

/** Define versions for standard AsciidoctorJS modules.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@SuppressWarnings(['ConfusingMethodName', 'ClassName'])
@CompileStatic
class BaseAsciidoctorJSModules implements AsciidoctorJSModules {
    private final AsciidoctorModuleDefinition docbook

    /** Creates a module definition that is attached to a specific asciidoctorjs
     * extension.
     *
     * @param asciidoctorjs Extension that this module is attached to.
     */
    BaseAsciidoctorJSModules(
        AsciidoctorModuleDefinition docbook
    ) {
        this.docbook = docbook
    }

    /** Configure docbook via closure.
     *
     * @param cfg Configurating closure
     */
    @Override
    void docbook(@DelegatesTo(AsciidoctorModuleDefinition) Closure cfg) {
        configureItem(this.docbook, cfg)
    }

    /** The Docbook module
     *
     * @return Acess to the Docbook module. Never {@code null}.
     */
    @Override
    AsciidoctorModuleDefinition getDocbook() {
        this.docbook
    }

    /** For the module that are configured in both module sets,
     * compare to see if the versions are the same
     *
     * @param other Other module set to compare.
     */
    @Override
    boolean isSetVersionsDifferentTo(AsciidoctorJSModules other) {
        different(docbook, other.docbook)
    }

    private boolean different(AsciidoctorModuleDefinition lhs, AsciidoctorModuleDefinition rhs) {
        lhs.version != null && rhs.version != null && stringize(lhs.version) != stringize(rhs.version)
    }
}
