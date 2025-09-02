/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.testfixtures.model5

import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.testfixtures.DslType
import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.grolifant5.api.core.OperatingSystem
import org.ysb33r.grolifant5.api.core.StringTools
import spock.lang.Specification
import spock.lang.TempDir

import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL
import static org.asciidoctor.gradle.testfixtures.model5.FunctionalTestSetup.getOfflineRepositoriesGroovyDsl
import static org.asciidoctor.gradle.testfixtures.model5.FunctionalTestSetup.getOfflineRepositoriesKotlinDsl

class IntegrationSpecification extends Specification {
    public static final boolean IS_KOTLIN_DSL = false
    public static final boolean IS_GROOVY_DSL = true
    public static final OperatingSystem OS = OperatingSystem.current()
    public static final File TEST_PROJECTS_DIR = new File(System.getProperty('TEST_PROJECTS_DIR'))
    public static final Boolean IS_OFFLINE = System.getProperty('IS_OFFLINE','false').toBoolean()

    @TempDir
    File testProjectDir

    File projectDir
    File buildDir
    File projectCacheDir
    File buildFile
    File buildFileKts
    File settingsFile
    File testKitDir

    void setup() {
        projectDir = new File(testProjectDir, 'test-project')
        projectDir.mkdirs()
        buildDir = new File(projectDir, 'build')
        projectCacheDir = new File(projectDir, '.gradle')
        buildFile = new File(projectDir, 'build.gradle')
        buildFileKts = new File(projectDir, 'build.gradle.kts')
        settingsFile = new File(projectDir, 'settings.gradle')
        settingsFile.text = ''

        testKitDir = new File(testProjectDir, ".testkit-${UUID.randomUUID()}")
        testKitDir.mkdirs()
    }

    void cleanup() {
        if (testKitDir && testKitDir.exists()) {
            testKitDir.deleteDir()
        }
    }

    GradleRunner getGradleRunner(
            boolean groovyDsl,
            String taskName
    ) {
        getGradleRunner(groovyDsl, [taskName])
    }

    GradleRunner getGradleRunner(
            boolean groovyDsl,
            List<String> args
    ) {
        GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(args)
                .forwardOutput()
                .withDebug(groovyDsl)
                .withPluginClasspath()
                .withTestKitDir(testKitDir)
    }

    GradleRunner getGradleRunnerConfigCache(
            boolean groovyDsl,
            List<String> args
    ) {
        getGradleRunner(groovyDsl, args + ['--configuration-cache', '--configuration-cache-problems=fail'])
                .withGradleVersion('8.9')
                .withDebug(false)
    }

    void copyTestProject(String projectName) {
        File srcDir = new File(TEST_PROJECTS_DIR, projectName).absoluteFile
        File target = projectDir
        FileUtils.copyDirectory(srcDir, target)
    }

    void writeBasicBuildFileGroovy(Iterable<String> plugins, Iterable<String> imports = []) {
        final importedItems = imports.collect {
            "import ${it}"
        }.join('\n')

        final pluginIds = plugins.collect {
            "id '${it}'"
        }.join('\n' + (StringTools.SPACE * 12))

        buildFile.text = """
        ${importedItems}

        plugins {
            ${pluginIds}
        }

        ${offlineRepositoriesGroovyDsl}
        """.stripIndent()
    }

    void addOutputToSourceSetGroovy(String toolchainName, String formatterName, String sourceSetName) {
        buildFile << """
        asciidoc {
            publications {
                ${sourceSetName} {
                    output('${toolchainName}', '${formatterName}')
                }
            }
        }
        """.stripIndent()
    }

    void configureSourceSetGroovy(
            String sourceSetName,
            String dsl
    ) {
        buildFile << """
        asciidoc.publications.${sourceSetName}.sourceSet {
            ${dsl}
        }
        """
    }

    boolean fileExists(File path) {
        path.exists()
    }

    boolean fileExists(File baseDir, String path) {
        new File(baseDir, path).exists()
    }

    boolean fileContains(File path,String content) {
        path.text.contains(content)
    }

    boolean fileContains(File baseDir,String path, String content) {
        new File(baseDir,path).text.contains(content)
    }

    static String getEscapedEnvPathString() {
        if (OS.windows) {
            System.getenv(OS.pathVar).replace(BACKSLASH, DOUBLE_BACKSLASH)
        } else {
            System.getenv(OS.pathVar)
        }
    }

    static String getEscapedPathString(String path) {
        if (OS.windows) {
            path.replace('/', BACKSLASH)
        } else {
            path
        }
    }

    static String getOfflineRepositories(DslType dslType = GROOVY_DSL) {
        dslType == GROOVY_DSL ? getOfflineRepositoriesGroovyDsl() : offlineRepositoriesKotlinDsl
    }

    static private final String BACKSLASH = '\\'
    static private final String DOUBLE_BACKSLASH = BACKSLASH * 2
}