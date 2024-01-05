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
package org.asciidoctor.gradle.testfixtures

import groovy.transform.CompileStatic
import org.gradle.testkit.runner.GradleRunner

import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL
import static org.asciidoctor.gradle.testfixtures.DslType.KOTLIN_DSL
import static org.asciidoctor.gradle.testfixtures.FunctionalTestSetup.getOfflineRepositoriesGroovyDsl
import static org.asciidoctor.gradle.testfixtures.FunctionalTestSetup.getOfflineRepositoriesKotlinDsl

/**
 * Apply this trait to integration tests.
 */
@CompileStatic
@SuppressWarnings('DuplicateStringLiteral')
trait FunctionalTestFixture {

    public static final String TEST_REPO_DIR = System.getProperty(
            'OFFLINE_REPO',
            './testfixtures/offline-repo/build/repo'
    )

    File getProjectDir() { new File(testProjectDir, 'test-project') }

    File getBuildFile() { new File(projectDir, 'build.gradle') }

    File getBuildFileKts() { new File(projectDir, 'build.gradle.kts') }

    File getSettingsFile() { new File(projectDir, 'settings.gradle') }

    File getBuildDir() { new File(projectDir, 'build') }

    File getAlternateProjectDir() { new File(testProjectDir, 'alternate-test-project') }

    String getOfflineRepositories(DslType dslType = GROOVY_DSL) {
        dslType == GROOVY_DSL ? getOfflineRepositoriesGroovyDsl(new File(TEST_REPO_DIR)) :
                getOfflineRepositoriesKotlinDsl(new File(TEST_REPO_DIR))
    }

    void initializeProjectLayout() {
        projectDir.mkdirs()
        settingsFile.text = "rootProject.name='test-project'"
    }

    GradleRunner getGroovyGradleRunner(List<String> taskNames) {
        FunctionalTestSetup.getGradleRunner(GROOVY_DSL, projectDir, taskNames)
    }

    GradleRunner getKotlinGradleRunner(List<String> taskNames) {
        FunctionalTestSetup.getGradleRunner(KOTLIN_DSL, projectDir, taskNames)
    }

    File writeGroovyBuildFile(Collection<String> plugins, String extraContent) {
        buildFile.withWriter { w ->
            w.println 'plugins {'
            plugins.each { p ->
                w.println "  id '${p}'"
            }
            w.println '}'
            w.println()
            w.println(offlineRepositories)
            w.println()
            w.println(extraContent)
        }
        buildFile
    }

    File writeGroovyBuildFile(String plugin, String extraContent) {
        writeGroovyBuildFile([plugin], extraContent)
    }

    File writeKotlinBuildFile(Collection<String> plugins, String extraContent) {
        buildFileKts.withWriter { w ->
            w.println 'plugins {'
            plugins.each { p ->
                w.println "  id (\"${p}\")"
            }
            w.println '}'
            w.println()
            w.println(getOfflineRepositories(KOTLIN_DSL))
            w.println()
            w.println(extraContent)
        }
        buildFileKts
    }

    File writeKotlinBuildFile(String plugin, String extraContent) {
        writeKotlinBuildFile([plugin], extraContent)
    }

    abstract File getTestProjectDir()
}