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
package org.asciidoctor.gradle.js.nodejs.core

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.js.nodejs.AbstractAsciidoctorNodeJSTask
import org.gradle.api.Project
import org.ysb33r.gradle.nodejs.BaseNpmExtension

/**
 * An extension to configure Npm.
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorJSNpmExtension extends BaseNpmExtension<AsciidoctorJSNodeExtension, AsciidoctorJSNpmExtension> {

    AsciidoctorJSNpmExtension(Project project) {
        super(project, project.extensions.getByType(AsciidoctorJSNodeExtension))
        homeDirectory = projectOperations.buildDirDescendant(".asciidoctor-npm/${project.name}")
    }

    AsciidoctorJSNpmExtension(AbstractAsciidoctorNodeJSTask task) {
        super(
                task,
                task.project.extensions.getByType(AsciidoctorJSNodeExtension),
                task.project.extensions.getByType(AsciidoctorJSNpmExtension)
        )
    }

    @Override
    protected String getNodeJsExtensionName() {
        AsciidoctorJSNodeExtension.NAME
    }
}
