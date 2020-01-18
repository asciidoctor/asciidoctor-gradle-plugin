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
package org.asciidoctor.gradle.testfixtures

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.testfixtures.internal.TestFixtureVersionLoader
import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.grolifant.api.OperatingSystem

/** Utility methods for setting up functional plugin tests.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.0
 */
@CompileStatic
class FunctionalTestSetup {

    public final static OperatingSystem OS = OperatingSystem.current()

    /** Provides a list of files that should be on the classpath for testing a plugin.
     *
     * @param projectSubdir Name of the project path on disk (not the project under test).
     *
     * @return List of classpath files.
     */
    static List<File> loadPluginClassPath(final Class testClass, final String projectSubdir) {
        URL pluginClasspathResource = testClass.classLoader.getResource('plugin-classpath.txt')

        if (pluginClasspathResource == null) {
            return new File(
                "./${projectSubdir}/build/createClasspathManifest/plugin-classpath.txt"
            ).readLines().collect {
                new File(it)
            }
        }

        if (pluginClasspathResource == null) {
            throw new IllegalStateException(
                'Did not find plugin classpath resource, run `intTestClasses` or `testClasses` build task.'
            )
        }

        pluginClasspathResource.readLines().collect { new File(it) }
    }

    static GradleRunner getGradleRunner(DslType dsl, File projectDir, List<String> taskNamesAndParameters) {
        getGradleRunner(dsl, projectDir, null, taskNamesAndParameters)
    }

    static GradleRunner getGradleRunner(
        DslType dsl,
        File projectDir,
        List<File> pluginClasspath,
        List<String> taskNames
    ) {
        List<String> eventualTaskNames = []
        eventualTaskNames.addAll(taskNames)

        if (OS.windows) {
            eventualTaskNames.add '--no-daemon'
        }

        GradleRunner runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(taskNames)
            .forwardOutput()

        dsl == DslType.GROOVY_DSL ? runner.withDebug(true) : runner.withDebug(false)
        pluginClasspath ? runner.withPluginClasspath(pluginClasspath) : runner.withPluginClasspath()
    }

    /** Returns a scriptlet that can be included in Groovy DSL build script to load an off-line repository.
     *
     * @param repoDir
     * @param fileName
     * @return Groovy DSL
     */
    static String getOfflineRepositoriesGroovyDsl(File repoDir, final String fileName = 'repositories.gradle') {
        File repo = new File(repoDir, fileName)
        if (!repo.exists()) {
            throw new FileNotFoundException(
                "${repo} not found. Run ':testfixture-offline-repo:buildOfflineRepositories' build task"
            )
        }

        if (OS.windows) {
            "apply from: /${repo.absolutePath}/"
        } else {
            "apply from: '${repo.absolutePath}'"
        }
    }

    /** Returns a scriptlet that can be included in Kotlin DSL build script to load an off-line repository.
     *
     * @param repoDir
     * @param fileName
     * @return Kotlin DSL
     */
    static String getOfflineRepositoriesKotlinDsl(File repoDir, final String fileName = 'repositories.gradle.kts') {
        File repo = new File(repoDir, fileName)
        if (!repo.exists()) {
            throw new FileNotFoundException(
                "${repo} not found. Run ':testfixture-offline-repo:buildOfflineRepositories' build task"
            )
        }

        if (OS.windows) {
            $/apply( from = "${repo.absolutePath.replace(BACKSLASH, DOUBLE_BACKSLASH)}")/$
        } else {
            $/apply( from = "${repo.absolutePath}")/$
        }
    }

    static File getOfflineRepo() {
        new File(TestFixtureVersionLoader.VERSIONS['offline-repo-location'])
    }

    static private final String BACKSLASH = '\\'
    static private final String DOUBLE_BACKSLASH = BACKSLASH * 2
}