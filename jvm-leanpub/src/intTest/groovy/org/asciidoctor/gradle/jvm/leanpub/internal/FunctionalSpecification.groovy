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
package org.asciidoctor.gradle.jvm.leanpub.internal

import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.testfixtures.DslType
import org.asciidoctor.gradle.testfixtures.FunctionalTestSetup
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ysb33r.grolifant.api.OperatingSystem
import spock.lang.Specification

import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL
import static org.asciidoctor.gradle.testfixtures.DslType.KOTLIN_DSL
import static org.asciidoctor.gradle.testfixtures.FunctionalTestSetup.getOfflineRepositoriesGroovyDsl
import static org.asciidoctor.gradle.testfixtures.FunctionalTestSetup.getOfflineRepositoriesKotlinDsl

class FunctionalSpecification extends Specification {
    public static final String TEST_PROJECTS_DIR = System.getProperty(
        'TEST_PROJECTS_DIR',
        './src/intTest/projects'
    )
    public static final String TEST_REPO_DIR = FunctionalTestSetup.offlineRepo.absolutePath
//    public static final OperatingSystem OS = OperatingSystem.current()

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @CompileStatic
    GradleRunner getGradleRunner(List<String> taskNames = ['asciidoctor']) {
        FunctionalTestSetup.getGradleRunner(GROOVY_DSL, testProjectDir.root, taskNames)
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects'])
    void createTestProject(String docGroup = 'leanpub') {
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, docGroup), testProjectDir.root)
    }

    @CompileStatic
    String getOfflineRepositories(DslType dslType = GROOVY_DSL) {
        dslType == GROOVY_DSL ? getOfflineRepositoriesGroovyDsl(new File(TEST_REPO_DIR)) :
            getOfflineRepositoriesKotlinDsl(new File(TEST_REPO_DIR))
    }

    File getJvmConvertGroovyBuildFile(String extraContent, String plugin = 'org.asciidoctor.jvm.leanpub') {
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

    File getJvmConvertKotlinBuildFile(String extraContent, String plugin = 'org.asciidoctor.jvm.leanpub') {
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

}