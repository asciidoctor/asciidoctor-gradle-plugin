/*
 * Copyright 2013-2018 the original author or authors.
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
import org.asciidoctor.gradle.testfixtures.jvm.FunctionalTestSetup
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class FunctionalSpecification extends Specification {
    static
    final String TEST_PROJECTS_DIR = System.getProperty('TEST_PROJECTS_DIR') ?: './asciidoctor-gradle-jvm/src/intTest/projects'
    static
    final String TEST_REPO_DIR = System.getProperty('OFFLINE_REPO') ?: './testfixtures/offline-repo/build/repo'

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Shared
    List<File> pluginClasspath = FunctionalTestSetup.loadPluginClassPath(getClass(), 'asciidoctor-gradle-jvm')

    @CompileStatic
    GradleRunner getGradleRunner(List<String> taskNames = ['asciidoctor']) {
        FunctionalTestSetup.getGradleRunner(testProjectDir.root,pluginClasspath,taskNames)
   }

    @SuppressWarnings(['FactoryMethodName', 'BuilderMethodWithSideEffects'])
    void createTestProject(String docGroup = 'normal') {
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, docGroup), testProjectDir.root)
    }

    @CompileStatic
    String getOfflineRepositories() {
        FunctionalTestSetup.getOfflineRepositories(new File(TEST_REPO_DIR))
    }

    File getJvmConvertBuildFile(String extraContent) {
        File buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
plugins {
    id 'org.asciidoctor.jvm.convert'
}

${offlineRepositories}

${extraContent}
"""
        buildFile
    }

}