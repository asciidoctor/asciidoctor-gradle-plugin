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
package org.asciidoctor.gradle.internal

import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.testfixtures.jvm.DslType
import org.asciidoctor.gradle.testfixtures.jvm.FunctionalTestSetup
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.asciidoctor.gradle.testfixtures.jvm.DslType.GROOVY_DSL
import static org.asciidoctor.gradle.testfixtures.jvm.DslType.KOTLIN_DSL
import static org.asciidoctor.gradle.testfixtures.jvm.FunctionalTestSetup.getOfflineRepositoriesGroovyDsl
import static org.asciidoctor.gradle.testfixtures.jvm.FunctionalTestSetup.getOfflineRepositoriesKotlinDsl

class FunctionalSpecification extends Specification {

    @SuppressWarnings('LineLength')
    static
        final String TEST_PROJECTS_DIR = System.getProperty('TEST_PROJECTS_DIR', './asciidoctor-gradle-jvm/src/intTest/projects')
    static
    final String TEST_REPO_DIR = System.getProperty('OFFLINE_REPO', './testfixtures/offline-repo/build/repo')

    @Rule
    TemporaryFolder testProjectDir

    @Rule
    TemporaryFolder alternateProjectDir

    @SuppressWarnings(['PrivateFieldCouldBeFinal'])
    private List<String> allowedDeprecations = []

    @CompileStatic
    GradleRunner getGradleRunner(List<String> taskNames = ['asciidoctor']) {
        FunctionalTestSetup.getGradleRunner(testProjectDir.root, taskNames)
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects'])
    void createTestProject(String docGroup = 'normal') {
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, docGroup), testProjectDir.root)
    }

    @CompileStatic
    String getOfflineRepositories(DslType dslType = GROOVY_DSL) {
        dslType == GROOVY_DSL ? getOfflineRepositoriesGroovyDsl(new File(TEST_REPO_DIR)) :
            getOfflineRepositoriesKotlinDsl(new File(TEST_REPO_DIR))
    }

    File getJvmConvertGroovyBuildFile(String extraContent, String plugin = 'org.asciidoctor.jvm.convert') {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id '${plugin}'
            }

            ${offlineRepositories}

            ${extraContent}
        """
        buildFile
    }

    File getJvmConvertKotlinBuildFile(String extraContent, String plugin = 'org.asciidoctor.jvm.convert') {
        File buildFile = testProjectDir.newFile('build.gradle.kts')
        buildFile << """
            plugins {
                id ("${plugin}")
            }

            ${getOfflineRepositories(KOTLIN_DSL)}

            ${extraContent}
        """
        buildFile
    }

    String getDefaultProcessModeForAppveyor(final DslType dslType = GROOVY_DSL) {
        if (System.getenv('APPVEYOR')) {
            dslType == GROOVY_DSL ? 'inProcess = JAVA_EXEC' : 'inProcess = ProcessMode.JAVA_EXEC'
        } else {
            ''
        }
    }

    void assertNoDeprecatedUsages(BuildResult result) {
        List<String> outputLines = result.output.readLines()

        outputLines.each { String line ->
            assert !isUnallowedDeprecation(line) : "Output contains an unallowed deprecation: ${line}"
        }
    }

    boolean isUnallowedDeprecation(String line) {
        line.contains('has been deprecated') &&
                !allowedDeprecations.any { String allowedDeprecation -> line.startsWith(allowedDeprecation) }
    }

    void allowDeprecation(String allowedDeprecation) {
        allowedDeprecations.add(allowedDeprecation)
    }
}