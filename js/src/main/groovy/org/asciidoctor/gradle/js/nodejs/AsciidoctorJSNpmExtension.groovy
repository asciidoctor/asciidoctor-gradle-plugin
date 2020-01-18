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
package org.asciidoctor.gradle.js.nodejs

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.ysb33r.gradle.nodejs.NpmExtension

/** An extension to configure Npm.
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSNpmExtension extends NpmExtension {
    public final static String NAME = 'asciidoctorNpm'

    AsciidoctorJSNpmExtension(Project project) {
        super(project)
        homeDirectory = { new File(project.buildDir, "/.npm/${project.name}") }
    }

    AsciidoctorJSNpmExtension(Task task) {
        super(task, NAME)
    }

    @Override
    protected String getNodeJsExtensionName() {
        AsciidoctorJSNodeExtension.NAME
    }

}
