/*
 * Copyright ${year} the original author or authors.
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
package org.asciidoctor.gradle.model5.core.testfixtures.examples

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorCoreExtension
import org.asciidoctor.gradle.model5.core.AsciidoctorCorePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class ExamplePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(AsciidoctorCorePlugin)

        final asciidoc = project.extensions.getByType(AsciidoctorCoreExtension)
        final objectFactory = project.objects
        asciidoc.toolchains.registerFactory(ExampleToolchain) { String name ->
            objectFactory.newInstance(ExampleToolchain, name)
        }

        asciidoc.toolchains.create('myexample', ExampleToolchain)
    }
}
