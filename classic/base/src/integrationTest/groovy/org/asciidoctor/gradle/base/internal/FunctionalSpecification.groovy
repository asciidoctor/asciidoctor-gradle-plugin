/*
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.base.internal


import org.asciidoctor.gradle.testfixtures.IntegrationSpecification

import static org.asciidoctor.gradle.testfixtures.DslType.KOTLIN_DSL

class FunctionalSpecification extends IntegrationSpecification {

    public static final String TEST_PROJECTS_DIR = System.getProperty(
            'TEST_PROJECTS_DIR',
            './asciidoctor-gradle-base/src/intTest/projects'
    )

    public static final String TEST_REPO_DIR = System.getProperty(
            'OFFLINE_REPO',
            './testfixtures/offline-repo/build/repo'
    )

//    @TempDir
//    File testProjectDir

//    @CompileStatic
//    GradleRunner getGradleRunner(List<String> taskNames = ['tasks']) {
//        FunctionalTestSetup.getGradleRunner(GROOVY_DSL, testProjectDir, taskNames)
//    }
//
//    @CompileStatic
//    GradleRunner getGradleRunnerForKotlin(List<String> taskNames = ['tasks']) {
//        FunctionalTestSetup.getGradleRunner(KOTLIN_DSL, testProjectDir, taskNames)
//    }
//
//    @SuppressWarnings(['BuilderMethodWithSideEffects'])
//    void createTestProject(String docGroup = 'normal') {
//        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, docGroup), testProjectDir)
//    }


    File getGroovyBuildFile(String extraContent, String plugin = 'org.asciidoctor.base.classic') {
        File buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id '${plugin}'
            }

            ${offlineRepositories}

            ${extraContent}
        """
        buildFile
    }

    File getKotlinBuildFile(String extraContent, String plugin = 'org.asciidoctor.base.classic') {
        File buildFile = new File(testProjectDir, 'build.gradle.kts')
        buildFile << """
            plugins {
                id("${plugin}")
            }

            ${getOfflineRepositories(KOTLIN_DSL)}

            ${extraContent}
"""
        buildFile
    }

}