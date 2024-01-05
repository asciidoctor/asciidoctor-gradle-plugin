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
package org.asciidoctor.gradle.jvm.gems.internal

import org.asciidoctor.gradle.testfixtures.FunctionalTestFixture
import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.grolifant.api.core.OperatingSystem
import spock.lang.Specification
import spock.lang.TempDir

class FunctionalSpecification extends Specification implements FunctionalTestFixture {
    public static final String TEST_PROJECTS_DIR = System.getProperty(
            'TEST_PROJECTS_DIR',
            './asciidoctor-gradle-jvm-gems/src/intTest/projects'
    )
    public static final String TEST_REPO_DIR = System.getProperty(
            'OFFLINE_REPO',
            './testfixtures/offline-repo/build/repo'
    )
    public static final OperatingSystem OS = OperatingSystem.current()

    @TempDir
    File testProjectDir

    void setup() {
        projectDir.mkdirs()
    }

    GradleRunner getGradleRunner(List<String> taskNames) {
        getGroovyGradleRunner(taskNames)
    }
}