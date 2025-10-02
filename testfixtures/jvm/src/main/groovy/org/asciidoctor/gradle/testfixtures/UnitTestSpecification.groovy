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
package org.asciidoctor.gradle.testfixtures

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.grolifant5.api.core.ProjectOperations
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin
import spock.lang.Specification
import spock.lang.TempDir

class UnitTestSpecification extends Specification {
    @TempDir
    File projectDir
    Project project = ProjectBuilder.builder().withProjectDir(projectDir)build()
    ProjectOperations projectOperations

    void setup() {
        project.pluginManager.apply(GrolifantServicePlugin)
        projectOperations = ProjectOperations.find(project)
    }
}