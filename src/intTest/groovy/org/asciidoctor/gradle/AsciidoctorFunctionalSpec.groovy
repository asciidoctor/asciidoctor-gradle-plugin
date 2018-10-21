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
package org.asciidoctor.gradle

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
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
@SuppressWarnings(['DuplicateStringLiteral', 'DuplicateNumberLiteral', 'MethodName', 'ClassSize', 'DuplicateMapLiteral', 'UnnecessaryGString'])
class AsciidoctorFunctionalSpec extends Specification {
    public static final String TEST_PROJECTS_DIR = "src/intTest/projects"
    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    List<File> pluginClasspath

    def setup() {
        def pluginClasspathResource = getClass().classLoader.getResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    def "Should do nothing with an empty project"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """
        plugins {
            id "org.asciidoctor.convert"
        }
        
        """

        when:
        final result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .build()

        then:
        result.task(":asciidoctor").outcome == TaskOutcome.NO_SOURCE
    }

    def "Should build normally for a standard project"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """
        plugins {
            id "org.asciidoctor.convert"
        }
        
        asciidoctor {
            options safe : 'UNSAFE'
        }
        """

        and: "Some source files"
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, "normal"), testProjectDir.root)
        final buildDir = new File(testProjectDir.root, "build")

        when:
        final BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(["asciidoctor"])
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .build()

        then:
        result.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
        new File(buildDir, "asciidoc/html5/sample.html").exists()
        new File(buildDir, "asciidoc/html5/subdir/sample2.html").exists()
        result.output.contains('Use --warning-mode=all to see list of potential affected files')
    }

    def "Should print warning message for legacy attributes"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """
        plugins {
            id "org.asciidoctor.convert"
        }
        """

        and: "Some source files"
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, 'normal'), testProjectDir.root)
        final buildDir = new File(testProjectDir.root, 'build')

        when:
        final BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments('asciidoctor', '--warning-mode=all')
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .build()
        final String output = result.output

        then:
        result.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
        new File(buildDir, "asciidoc/html5/sample.html").exists()
        output.contains('It seems that you may be using implicit attributes ')
        output.contains('sample.asciidoc')
    }

    def "Task should be up-to-date when executed a second time"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """
        plugins {
            id "org.asciidoctor.convert"
        }
        """

        and: "Some source files"
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, "normal"), testProjectDir.root)

        when:
        GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .build()
        final result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .build()

        then:
        result.task(":asciidoctor").outcome == TaskOutcome.UP_TO_DATE
    }

    @SuppressWarnings('MethodName')
    def "Task should not be up-to-date when classpath is changed"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """
        plugins {
            id "org.asciidoctor.convert"
        }
        repositories {
            jcenter()
        }
        if (project.hasProperty('modifyClasspath')) {
            dependencies {
                asciidoctor 'org.hibernate.infra:hibernate-asciidoctor-extensions:1.0.3.Final'
            }
        }
        """

        and: "Some source files"
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, "normal"), testProjectDir.root)

        when:
        GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .build()
        final result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("asciidoctor", "-PmodifyClasspath")
            .withPluginClasspath(pluginClasspath)
            .build()

        then:
        result.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
    }

}

