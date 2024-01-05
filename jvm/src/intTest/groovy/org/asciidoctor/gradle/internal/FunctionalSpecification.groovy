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
package org.asciidoctor.gradle.internal

import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.testfixtures.DslType
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
            './src/intTest/projects'
    )
    public static
    final String TEST_REPO_DIR = FunctionalTestSetup.offlineRepo.absolutePath

    @TempDir
    File testProjectDir

    File testKitDir

    void setup() {
        projectDir.mkdirs()
        testKitDir = new File(testProjectDir, ".testkit-${UUID.randomUUID()}")
    }

    void cleanup() {
        if (testKitDir && testKitDir.exists()) {
            testKitDir.deleteDir()
        }
    }

    @CompileStatic
    GradleRunner getGradleRunner(List<String> taskNames = ['asciidoctor']) {
        getGroovyGradleRunner(taskNames).withTestKitDir(testKitDir)
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects'])
    void createTestProject(String docGroup = 'normal') {
        File srcDir = new File(TEST_PROJECTS_DIR, docGroup).absoluteFile
        FileUtils.copyDirectory(srcDir, projectDir)
    }

    File getJvmConvertGroovyBuildFile(String extraContent, String plugin = 'org.asciidoctor.jvm.convert') {
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
        buildFileKts << """
            plugins {
                id ("${plugin}")
            }

            ${getOfflineRepositories(KOTLIN_DSL)}

            ${extraContent}
        """
        buildFileKts
    }

    String getDefaultProcessModeForAppveyor(final DslType dslType = GROOVY_DSL) {
        if (System.getenv('APPVEYOR')) {
            dslType == GROOVY_DSL ? 'inProcess = JAVA_EXEC' : 'inProcess = ProcessMode.JAVA_EXEC'
        } else {
            ''
        }
    }

}