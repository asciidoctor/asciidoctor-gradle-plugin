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
package org.asciidoctor.gradle.testfixtures.model5

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.grolifant5.api.core.ProjectOperations
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin
import spock.lang.Specification
import spock.lang.TempDir

class UnitTestSpecification extends Specification {
    public static final Boolean IS_OFFLINE = System.getProperty('IS_OFFLINE','false').toBoolean()
    public static final String OFFLINE_REASON = 'Gradle is in offline mode'

    @TempDir
    File testProjectDir

    File projectDir
    Project project
    ProjectOperations projectOperations

    void setup() {
        projectDir = new File(testProjectDir,'test-project')
        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.pluginManager.apply(GrolifantServicePlugin)
        projectOperations = ProjectOperations.find(project)
    }
}