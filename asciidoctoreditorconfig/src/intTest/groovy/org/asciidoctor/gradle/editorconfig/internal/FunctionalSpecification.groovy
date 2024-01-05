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
package org.asciidoctor.gradle.editorconfig.internal

import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.testfixtures.FunctionalTestFixture
import org.asciidoctor.gradle.testfixtures.FunctionalTestSetup
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL
import static org.asciidoctor.gradle.testfixtures.DslType.KOTLIN_DSL

class FunctionalSpecification extends Specification implements FunctionalTestFixture {

    public static final String TEST_PROJECTS_DIR = System.getProperty(
            'TEST_PROJECTS_DIR',
            './asciidoctoreditorconfig/src/intTest/projects'
    )

    public static final String TEST_REPO_DIR = System.getProperty(
            'OFFLINE_REPO',
            './testfixtures/offline-repo/build/repo'
    )

    @TempDir
    File testProjectDir

    void setup() {
        projectDir.mkdirs()
    }

    @CompileStatic
    GradleRunner getGradleRunner(List<String> taskNames = ['tasks']) {
        FunctionalTestSetup.getGradleRunner(GROOVY_DSL, projectDir, taskNames)
    }

    @CompileStatic
    GradleRunner getGradleRunnerForKotlin(List<String> taskNames = ['tasks']) {
        FunctionalTestSetup.getGradleRunner(KOTLIN_DSL, projectDir, taskNames)
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects'])
    void createTestProject(String docGroup = 'normal') {
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, docGroup), projectDir)
    }

    File getGroovyBuildFile(String extraContent, String plugin = 'org.asciidoctor.editorconfig') {
        buildFile << """
            plugins {
                id '${plugin}'
            }

            ${offlineRepositories}

            ${extraContent}
        """
        buildFile
    }

    File getKotlinBuildFile(String extraContent, String plugin = 'org.asciidoctor.editorconfig') {
        buildFileKts << """
            plugins {
                id("${plugin}")
            }

            ${getOfflineRepositories(KOTLIN_DSL)}

            ${extraContent}
"""
        buildFileKts
    }

}