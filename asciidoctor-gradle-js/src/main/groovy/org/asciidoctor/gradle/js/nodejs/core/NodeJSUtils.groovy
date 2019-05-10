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
