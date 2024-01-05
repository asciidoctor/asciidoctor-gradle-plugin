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
import org.ysb33r.gradle.nodejs.utils.npm.NpmExecutor
import org.ysb33r.grolifant.api.core.ProjectOperations

/** Utilities for dealing with NodeJS in an Asciidoctor-Gradle context
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
class NodeJSUtils {
    /**
     * Creates a skeleton {@code package.json} file.
     *
     * Primarily used to keep NPM from complaining.
     *
     * @param npmHome Working home directory for NPM.
     *   This is the directory in which {@code node_modules} will be created.
     * @param projectAlias An name for the project than can be used inside {@code package.json}.
     * @param po The {@link ProjectOperations} instance to use to resolve the version.
     * @param nodejs The NodeJS extension which is being used for this operation.
     * @param npm The NPM extension that is being used for this operation.
     *
     * @since 4.0
     */
    static void initPackageJson(
            File npmHome,
            String projectAlias,
            ProjectOperations po,
            AsciidoctorJSNodeExtension nodejs,
            AsciidoctorJSNpmExtension npm
    ) {
        File packageJson = new File(npmHome, 'package.json')
        if (!packageJson.exists()) {
            npmHome.mkdirs()
            new NpmExecutor(po, nodejs, npm)
                    .initPkgJson(projectAlias, po.projectTools.versionProvider.orElse('UNDEFINED'))
        }
    }
}
