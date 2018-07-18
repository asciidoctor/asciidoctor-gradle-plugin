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
package org.asciidoctor.gradle.compat

import org.apache.commons.io.FileUtils
import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.IgnoreIf

/**
 * The first functional specification.
 *
 * @author Peter Ledbrook
 */
@SuppressWarnings(['DuplicateStringLiteral', 'MethodName', 'UnnecessaryGString'])
class AsciidoctorFunctionalSpec extends FunctionalSpecification {

    static final String TEST_PROJECTS_DIR = FunctionalSpecification.TEST_PROJECTS_DIR

    @SuppressWarnings('MethodName')
    @IgnoreIf({ System.getProperty('OFFLINE_MODE') })
    def "Should do nothing with an empty project"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """\
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

    @SuppressWarnings('MethodName')
    @IgnoreIf({ System.getProperty('OFFLINE_MODE') })
    void 'Should build normally for a standard project'() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """
        plugins {
            id "org.asciidoctor.convert"
        }
        """

        and: "Some source files"
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, "normal"), testProjectDir.root)
        final buildDir = new File(testProjectDir.root, "build")

        when:
        final result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .build()

        then:
        result.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
        new File(buildDir, "asciidoc/html5/sample.html").exists()
        new File(buildDir, "asciidoc/html5/subdir/sample2.html").exists()
    }

    @SuppressWarnings('MethodName')
    @IgnoreIf({ System.getProperty('OFFLINE_MODE') })
    def "Task should be up-to-date when executed a second time"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """\
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
    @IgnoreIf({ System.getProperty('OFFLINE_MODE') })
    def "Task should not be up-to-date when classpath is changed"() {
        given: "A minimal build file"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """\
        plugins {
            id "org.asciidoctor.convert"
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

