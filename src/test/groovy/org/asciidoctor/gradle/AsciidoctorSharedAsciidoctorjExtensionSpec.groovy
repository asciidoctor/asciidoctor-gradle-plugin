/*
 * Copyright 2017 the original author or authors.
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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification


/**
 * Shared Asciidoctorj extensions specification
 *
 * @author Paul Merlin
 */
class AsciidoctorSharedAsciidoctorjExtensionSpec extends Specification {

    static final String ASCIIDOCTORJ_VERSION = System.getProperty("asciidoctorjVersion")

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    File rootDir
    File buildFile
    File settingsFile

    def "setup"() {
        rootDir = temporaryFolder.newFolder("root")
        buildFile = new File(rootDir, "build.gradle")
        settingsFile = new File(rootDir, "settings.gradle")
        settingsFile << "rootProject.name = 'root'"
    }

    def "As Project Dependency"() {

        given: "a gradle build"
        settingsFile << """
            include 'extension', 'core'
        """

        and: "an extension project"
        file("extension/build.gradle") << """
            plugins { id("java") }
            repositories { jcenter() }
            dependencies {
                compileOnly("org.asciidoctor:asciidoctorj:$ASCIIDOCTORJ_VERSION")
            }
        """
        file("extension/src/main/resources/META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry") <<
                "org.asciidoctor.example.ExampleExtensionRegistry"
        file("extension/src/main/java/org/asciidoctor/example/ExampleExtensionRegistry.java") << """
            package org.asciidoctor.example;
            
            import org.asciidoctor.Asciidoctor;
            import org.asciidoctor.extension.spi.ExtensionRegistry;

            public class ExampleExtensionRegistry implements ExtensionRegistry {
                @Override
                public void register(Asciidoctor asciidoctor) {
                    asciidoctor.javaExtensionRegistry().block("YELL", YellBlock.class);
                }                
            }
        """
        file("extension/src/main/java/org/asciidoctor/example/YellBlock.java") << """
            package org.asciidoctor.example;

            import org.asciidoctor.ast.AbstractBlock;
            import org.asciidoctor.extension.BlockProcessor;
            import org.asciidoctor.extension.Reader;

            import java.util.HashMap;
            import java.util.Map;

            public class YellBlock extends BlockProcessor {
                
                public YellBlock(String name, Map<String, Object> config) {
                    super(name, config);
                }

                @Override
                public Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
                    return createBlock(parent, "paragraph", reader.read().toUpperCase(), attributes, new HashMap<>());
                }
            }
        """

        and: "an asciidoctor project using the extension"
        file("core/build.gradle") << """
            plugins { id("org.asciidoctor.convert") }
            repositories { jcenter() }
            asciidoctorj {
                version = "$ASCIIDOCTORJ_VERSION"
            }
            asciidoctor {
                attributes = [linkcss: ''] // Smaller output for assertions
            }
            dependencies {
                asciidoctor project(':extension')
            }
        """
        file("core/src/docs/asciidoc/index.adoc") << """
            # YELL block processor extension usage

            [YELL]
            Hello world!
        """

        when: "running asciidoctor"
        def result = build(/* ":extension:assemble", */ ":core:asciidoctor") // TODO:pm remove commented code
        println(result.output) // TODO:pm remove println

        then: "the extension is built"
        result.task(":extension:jar").outcome == TaskOutcome.SUCCESS

        and: "the extension is applied"
        def html = file("core/build/asciidoc/html5/index.html").text
        html.contains("HELLO WORLD!")
        !html.contains("[YELL]")
    }

    private File file(String path) {
        def file = new File(rootDir, path)
        file.parentFile.mkdirs()
        return file
    }

    private BuildResult build(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(rootDir)
                .withPluginClasspath()
                .withArguments(arguments)
                .build()
    }
}