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
import org.gradle.api.Project
import org.ysb33r.gradle.nodejs.NodeJSExtension
import org.ysb33r.gradle.nodejs.NpmExtension
import org.ysb33r.gradle.nodejs.utils.npm.NpmExecutor
import org.ysb33r.grolifant.api.StringUtils

/** Utilities for dealing with NodeJS in an Asciidoctor-Gradle context
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
class NodeJSUtils {

    /** Creaes a skeleton {@code package.json} file.
     *
     * Primarily used to keep NPM from complaining.
     *
     * @param npmHome Working home directory for NPM.
     *   This is the directory in which {@code node_modules} will be created.
     * @param projectAlias An name for the project than can be used inside {@code package.json}.
     * @param project The GRadle project for which is is done.
     * @param nodejs The NodeJS extension which is being used for this operation.
     * @param npm The NPM extension that is being used for this operation.
     */
    static void initPackageJson(
            File npmHome,
            String projectAlias,
            Project project,
            NodeJSExtension nodejs,
            NpmExtension npm
    ) {
        File packageJson = new File(npmHome, 'package.json')
        if (!packageJson.exists()) {
            npmHome.mkdirs()
            NpmExecutor.initPkgJson(
                    projectAlias,
                    project.version ? StringUtils.stringize(project.version) : 'UNDEFINED',
                    project,
                    nodejs,
                    npm
            )
        }
    }
}
