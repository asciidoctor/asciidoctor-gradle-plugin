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
package org.asciidoctor.gradle.kindlegen.internal

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class FunctionalSpecification extends Specification {
    @Rule
    TemporaryFolder testProjectDir

    @Shared
    List<File> pluginClasspath

    void setupSpec() {
        def pluginClasspathResource = getClass().classLoader.getResource('plugin-classpath.txt')
        if (pluginClasspathResource == null) {
            pluginClasspathResource = new File('./build/createClasspathManifest/plugin-classpath.txt')
        }
        if (pluginClasspathResource == null) {
            throw new IllegalStateException('Did not find plugin classpath resource, run `intTestClasses` build task.')
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    GradleRunner getGradleRunner(List<String> taskNames) {
        GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(taskNames)
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withDebug(true)
    }
}