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
import org.junit.Ignore
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

@SuppressWarnings(['DuplicateStringLiteral','DuplicateNumberLiteral','MethodName','ClassSize','DuplicateMapLiteral', 'UnnecessaryGString'])
class AsciidoctorExtensionFunctionalSpec extends Specification {
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

    @SuppressWarnings('MethodName')
    def "Postprocessor extensions are registered and preserved across multiple builds"() {
        given: "A build file that declares extensions"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """\
        plugins {
            id "org.asciidoctor.convert"
        }
        
        asciidoctor {
            extensions {
                postprocessor { document, output ->
                    return "Hi, Mom" + output 
                }
            }
        }
        """

        and: "Some source files"
        FileUtils.copyDirectory(new File(TEST_PROJECTS_DIR, "extension"), testProjectDir.root)
        final buildDir = new File(testProjectDir.root, "build")

        when:
        final BuildResult firstInvocationResult = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .withDebug(true)
            .build()

        then:
        firstInvocationResult.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
        File sampleHtmlOutput = new File(buildDir, "asciidoc/html5/sample.html")
        sampleHtmlOutput.exists()
        sampleHtmlOutput.text.startsWith("Hi, Mom")

        when:
        new File(testProjectDir.root, "src/docs/asciidoc/sample.asciidoc") << "changes"
        final BuildResult secondInvocationResult = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .withDebug(true)
            .build()

        then:
        secondInvocationResult.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
        new File(buildDir, "asciidoc/html5/sample.html").text.startsWith("Hi, Mom")
    }

    @spock.lang.Ignore
    @SuppressWarnings('MethodName')
    def "require modules are registered and preserved across multiple builds"() {
        given: "A build file that declares a required module"
        def buildFile = testProjectDir.newFile("build.gradle")
        buildFile << """
        plugins {
            id "org.asciidoctor.convert"
        }
        
        repositories {
            jcenter()
        }

        dependencies {
            asciidoctor("org.asciidoctor:asciidoctorj-diagram:1.5.4.1"){
                exclude( group: "org.asciidoctor", module: "asciidoctorj" )
            }
        }
        asciidoctor {
            requires( 'asciidoctor-diagram' )
            backends( 'html5' )
        }
        """

        and: "source file with graphviz"
        def folder = testProjectDir.newFolder("src")
        def folder2 = new File(folder, "docs/asciidoc")
        folder2.mkdirs()
        new File(folder2, "graph.asciidoc").text = """

== Diagrams

["graphviz", format="svg", align="center"]
.Some graphviz
----
digraph {
        compound=true
        layout=dot
        edge [ dir = none, arrowhead = none, arrowtail = none ]
        node [ shape = square ]
        //billing_domain [ label = none, style = invisible ]
        subgraph cluster_3gpp_network {
                label = "3GPP\nnetwork"

                ctf [ label = "CTF\n(Charging\nTrigger\nFunction)" ]
                cdf [ label = "CDF\n(Charging\nData\nFunction)" ]
                cgf [ label = "CGF\n(Charging\nGateway\nFunction)" ]

                ctf -> cdf [ label = "Rf", dir = forward, arrowhead = normal ]
                cdf -> cgf [ label = "Ga", dir = forward, arrowhead = normal ]

                { rank=same; ctf cdf cgf }
        }
        /*
        subgraph cluster_billing_domain {
                label = "Billing\nDomain"
                billing_domain [ label = none, style = invisible ]
                { rank=same; billing_domain }
        }
        */
        billing_domain [ label = "Billing\nDomain" ]
        cgf -> billing_domain [ constraint = false, label = "Bx", dir = forward, arrowhead = normal ]
}
----
"""
        final buildDir = new File(testProjectDir.root, "build")

        when:
        final BuildResult firstInvocationResult = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("clean", "asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withDebug(true)
            .build()

        then:
        firstInvocationResult.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
        File outputFolder = new File(buildDir, "asciidoc/html5")
        File outputFile = new File(outputFolder, "graph.html")
        outputFile.exists()
        outputFolder.listFiles().findAll { it.name.endsWith(".svg") }.size() == 1

        when:
        new File(testProjectDir.root, "src/docs/asciidoc/sample.asciidoc") << "changes"
        final BuildResult secondInvocationResult = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("clean", "asciidoctor")
            .withPluginClasspath(pluginClasspath)
            .forwardOutput()
            .withDebug(true)
            .build()

        then:
        secondInvocationResult.task(":asciidoctor").outcome == TaskOutcome.SUCCESS
        outputFile.exists()
        outputFolder.parentFile.listFiles().findAll { it.name.endsWith(".svg") }.size() == 1
    }
}
