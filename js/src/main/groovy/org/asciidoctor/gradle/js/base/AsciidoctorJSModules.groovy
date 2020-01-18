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
import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition

/** Define versions for standard AsciidoctorJS modules.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@SuppressWarnings(['ConfusingMethodName', 'ClassName'])
@CompileStatic
interface AsciidoctorJSModules {

    /** Configure docbook via closure.
     *
     * @param cfg Configurating closure
     */
    void docbook(@DelegatesTo(AsciidoctorModuleDefinition) Closure cfg)

    /** The Docbook module
     *
     * @return Acess to the Docbook module. Never {@code null}.
     */
    AsciidoctorModuleDefinition getDocbook()

    /** For the module that are configured in both module sets,
     * compare to see if the versions are the same
     *
     * @param other Other module set to compare.
     *
     * @return {@code true} if modules of the same kind were found, but with different versions.
     */
    boolean isSetVersionsDifferentTo(AsciidoctorJSModules other)

    /** Returns a module by name
     *
     * @param name Name of module
     * @return Module* @throws {@link org.asciidoctor.gradle.base.ModuleNotFoundException} when the module
     *   is not registered.
     */
    AsciidoctorModuleDefinition getByName(final String name)
}
