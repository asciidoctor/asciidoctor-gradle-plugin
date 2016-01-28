/*
 * Copyright 2013-2014 the original author or authors.
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
package org.asciidoctor.gradle

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * The first functional specification.
 *
 * @author Peter Ledbrook
 */
class AsciidoctorFunctionalSpec extends Specification {
    public static final String TEST_PROJECTS_DIR = "src/intTest/projects"
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    List<File> pluginClasspath

    def setup() {
        def pluginClasspathResource = getClass().classLoader.getResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    @SuppressWarnings('MethodName')
    def "Should do nothing with an empty project"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """\
        plugins {
            id "org.asciidoctor.gradle.asciidoctor"
        }
        """

        when:
        final result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("asciidoctor")
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        result.task(":asciidoctor").outcome == TaskOutcome.UP_TO_DATE
    }

    @SuppressWarnings('MethodName')
    def "Should build normally for a standard project"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """\
        plugins {
            id "org.asciidoctor.gradle.asciidoctor"
        }
        """

        and: "Some source files"
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, "normal"), testProjectDir.root)
        final buildDir = new File(testProjectDir.root, "build")

        when:
        final result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("asciidoctor")
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        result.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
        new File(buildDir, "asciidoc/html5/sample.html").exists()
        new File(buildDir, "asciidoc/html5/subdir/sample2.html").exists()
    }

}

