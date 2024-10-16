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
package org.asciidoctor.gradle.jvm.slides.internal

import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.testfixtures.FunctionalTestFixture
import org.asciidoctor.gradle.testfixtures.FunctionalTestSetup
import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.grolifant.api.core.OperatingSystem
import spock.lang.Specification
import spock.lang.TempDir

import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL

class FunctionalSpecification extends Specification implements FunctionalTestFixture {

    @SuppressWarnings('LineLength')
    static
    final String TEST_PROJECTS_DIR = System.getProperty('TEST_PROJECTS_DIR', './src/intTest/projects')
    static
    final String TEST_REPO_DIR = FunctionalTestSetup.offlineRepo.absolutePath
    public static final OperatingSystem OS = OperatingSystem.current()

    @TempDir
    File testProjectDir

    @TempDir
    File alternateProjectDir

    void setup() {
        projectDir.mkdirs()
    }

    @CompileStatic
    GradleRunner getGradleRunner(List<String> taskNames = ['asciidoctor']) {
        FunctionalTestSetup.getGradleRunner(GROOVY_DSL, projectDir, taskNames)//.withTestKitDir(testKitDir)
    }

    @SuppressWarnings(['BuilderMethodWithSideEffects'])
    void createTestProject(String docGroup = 'normal') {
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, docGroup), projectDir)
    }

    String getOfflineRepositories() {
        File repo = new File(TEST_REPO_DIR, 'repositories.gradle')
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
}
