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

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.testfixtures.DslType
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.OperatingSystem
import org.ysb33r.grolifant5.api.core.StringTools
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin
import spock.lang.Specification
import spock.lang.TempDir

import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL

@Slf4j
class IntegrationSpecification extends Specification {
    public static final boolean IS_WINDOWS = OperatingSystem.current().windows
    public static final boolean IS_KOTLIN_DSL = false
    public static final boolean IS_GROOVY_DSL = true
    public static final OperatingSystem OS = OperatingSystem.current()
    public static final File TEST_PROJECTS_DIR = new File(System.getProperty('TEST_PROJECTS_DIR'))
    public static final Boolean IS_OFFLINE = System.getProperty('IS_OFFLINE', 'false').toBoolean()

    @TempDir
    File testProjectDir

    String configCacheGradleVersion = '8.9'
    File projectDir
    File buildDir
    File buildCacheDir
    File projectCacheDir
    File buildFile
    File buildFileKts
    File settingsFile
    File testKitDir
    File alternateProjectDir
    File alternateBuildDir

    void setup() {
        projectDir = new File(testProjectDir, 'test-project')

        projectDir.mkdirs()
        buildDir = new File(projectDir, 'build')
        projectCacheDir = new File(projectDir, '.gradle')
        buildFile = new File(projectDir, 'build.gradle')
        buildFileKts = new File(projectDir, 'build.gradle.kts')
        settingsFile = new File(projectDir, 'settings.gradle')
        settingsFile.text = "rootProject.name = 'test-project'"

        testKitDir = new File(testProjectDir, ".testkit-${UUID.randomUUID()}")
        testKitDir.mkdirs()

        alternateProjectDir = new File(testProjectDir, 'alternate-test-project')
        alternateBuildDir = new File(alternateProjectDir, 'build')

        buildCacheDir = new File(testProjectDir, '.build-cache')
    }

    void cleanup() {
        if (testKitDir && testKitDir.exists()) {
            testKitDir.deleteDir()
        }
    }

    /**
     * Gradle runner for a single task.
     *
     * @param groovyDsl DSL
     * @param taskName Task name
     * @return Gradle runner
     */
    GradleRunner getGradleRunner(
        boolean groovyDsl,
        String taskName
    ) {
        getGradleRunner(groovyDsl, [taskName])
    }

    /**
     * Gradle runner for a list of tasks.
     *
     * @param groovyDsl DSL
     * @param args Arguments
     * @return Gradle runner
     */
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

    /**
     * Gradle runner for a configuration cache.
     *
     * The Gradle version used for testing can be changed by setting {@link #configCacheGradleVersion}.
     *
     * @param groovyDsl DSL
     * @param args Tasks and arguments
     * @return Gradle runner
     */
    GradleRunner getGradleRunnerConfigCache(
        boolean groovyDsl,
        List<String> args
    ) {
        getGradleRunner(groovyDsl, args + ['--configuration-cache', '--configuration-cache-problems=fail'])
            .withGradleVersion(configCacheGradleVersion)
            .withDebug(false)
    }

    /**
     * Gradle runner for a configuration cache.
     *
     * The Gradle version used for testing can be changed by setting {@link #configCacheGradleVersion}.
     *
     * @param groovyDsl DSL
     * @param args Tasks and arguments
     * @return Gradle runner
     */
    GradleRunner getGradleRunnerWithBuildCache(
        boolean groovyDsl,
        List<String> args
    ) {
        getGradleRunner(groovyDsl, args + ['--build-cache'])
    }

    /**
     * Gradle runner for a configuration cache.
     *
     * The Gradle version used for testing can be changed by setting {@link #configCacheGradleVersion}.
     *
     * @param groovyDsl DSL
     * @param args Tasks and arguments
     * @return Gradle runner
     */
    GradleRunner getAlternativeGradleRunnerWithBuildCache(
        boolean groovyDsl,
        List<String> args
    ) {
        getGradleRunner(groovyDsl, args + ['--build-cache'])
            .withProjectDir(alternateProjectDir)
    }

    /**
     * Gradle runner for a configuration cache.
     *
     * The Gradle version used for testing can be changed by setting {@link #configCacheGradleVersion}.
     *
     * @param groovyDsl DSL
     * @param args Tasks and arguments
     * @return Gradle runner
     */
    GradleRunner getGradleRunnerWithBuildAndConfigCache(
        boolean groovyDsl,
        List<String> args
    ) {
        getGradleRunnerWithBuildCache(groovyDsl, args + ['--configuration-cache', '--configuration-cache-problems=fail'])
            .withGradleVersion(configCacheGradleVersion)
            .withDebug(false)
    }

    /**
     * Copies the named test project.
     *
     * @param projectName Project name.
     */
    void copyTestProject(String projectName) {
        File srcDir = new File(TEST_PROJECTS_DIR, projectName).absoluteFile
        File target = projectDir
        FileUtils.copyDirectory(srcDir, target)
    }

    /**
     * Writes a basic build file.
     *
     * @param plugins List of plugins.
     * @param imports List of imports.
     */
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

        new File(projectDir, 'gradle.properties').text = '''
        version=0.0.1
        org.gradle.daemon=false
        '''.stripIndent()
    }

    /**
     * Copies the created project to an alternate project area.
     *
     * Call this after everything has been setup.
     */
    void addBuildCacheAndCopyProjectToAlternateArea() {
        alternateProjectDir.mkdirs()
        buildCacheDir.mkdirs()
        settingsFile << """
        buildCache {
            local {
                directory = '${getEscapedPathString(buildCacheDir.absolutePath)}'
            }
        }
        """
        FileUtils.copyDirectory(projectDir, alternateProjectDir)
    }

    /**
     * Add an output to the AsciiDoc publication.
     *
     * @param toolchainName Name of toolchain.
     * @param formatterName Name of formatter.
     * @param sourceSetName Name of publication.
     */
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

    /**
     * Adds a block of DSL to the given publication's source set.
     *
     * @param sourceSetName Publication name.
     * @param dsl DSL block.
     */
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

    /**
     * Does the given file exists.
     *
     * @param path File path.
     * @return Exists?
     */
    boolean fileExists(File path) {
        path.exists()
    }

    /**
     * Does the file at the subpath exists.
     * @param baseDir Base directory.
     * @param path Subpath.
     * @return Exists?
     */
    boolean fileExists(File baseDir, String path) {
        new File(baseDir, path).exists()
    }

    /**
     * Does the given file contain the provided content?
     *
     * @param path File path
     * @param content Content
     * @return Exists?
     */
    boolean fileContains(File path, String content) {
        path.text.contains(content)
    }

    /**
     * Does the given file contain the provided content?
     * @param baseDir Base directory
     * @param path Sub path.
     * @param content Content.
     * @return Exists?
     */
    boolean fileContains(File baseDir, String path, String content) {
        new File(baseDir, path).text.contains(content)
    }

    /**
     * Get hold of the existing PATH.
     *
     * @return System path.
     */
    String getEscapedEnvPathString() {
        if (OS.windows) {
            System.getenv(OS.pathVar).replace(BACKSLASH, DOUBLE_BACKSLASH)
        } else {
            System.getenv(OS.pathVar)
        }
    }

    /**
     * Returns a scriptlet that can be included in Groovy DSL build script to load an off-line repository.
     *
     * @return Groovy DSL
     */
    String getOfflineRepositoriesGroovyDsl() {
        getOfflineRepositoriesDsl('org.ysb33r.ivypot.repo.groovy.dsl.file')
    }

    /**
     * Returns a scriptlet that can be included in Groovy DSL build script to load an off-line repository.
     *
     * @return Groovy DSL
     */
    String getOfflineRepositoriesKotlinDsl() {
        getOfflineRepositoriesDsl('org.ysb33r.ivypot.repo.kotlin.dsl.file')
    }

    /**
     * Escapes a file path.
     * @param path Path
     * @return Escaped path.
     */
    String getEscapedPathString(String path) {
        if (OS.windows) {
            path.replace('/', BACKSLASH)
        } else {
            path
        }
    }

    String getOfflineRepositories(DslType dslType = GROOVY_DSL) {
        dslType == GROOVY_DSL ? getOfflineRepositoriesGroovyDsl() : offlineRepositoriesKotlinDsl
    }

    Properties loadPropertiesFile(String propertiesName) {
        final p = ProjectBuilder.builder().build()
        p.pluginManager.apply(GrolifantServicePlugin)
        final fso = ConfigCacheSafeOperations.from(p).fsOperations()
        fso.loadPropertiesFromResource(
            "META-INF/asciidoctor.gradle/${propertiesName}.properties",
            this.class.classLoader
        )
    }

    private String getOfflineRepositoriesDsl(String propertyName) {
        final location = System.getProperty(propertyName)
        if (location == null) {
            log.warn("No property called ${propertyName}. Returning no DSL content")
        } else {
            new File(location).text
        }
    }

    public static final String BACKSLASH = '\\'
    public static final String DOUBLE_BACKSLASH = BACKSLASH * 2
}