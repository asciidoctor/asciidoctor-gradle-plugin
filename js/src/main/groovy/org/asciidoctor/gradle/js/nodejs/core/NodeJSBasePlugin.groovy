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
package org.asciidoctor.gradle.js.nodejs.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSNodeExtension
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSNpmExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @since 3.0
 */
@CompileStatic
class NodeJSBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create( AsciidoctorJSNodeExtension.NAME, AsciidoctorJSNodeExtension, project )
        project.extensions.create( AsciidoctorJSNpmExtension.NAME, AsciidoctorJSNpmExtension, project )
    }
}
