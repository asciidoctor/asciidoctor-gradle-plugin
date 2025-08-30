/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.jvm.engines


import org.asciidoctor.gradle.testfixtures.DslType
import org.asciidoctor.gradle.testfixtures.model5.IntegrationSpecification

class AsciidoctorjEngineSpec extends IntegrationSpecification {

    public static final String ENGINE_NAME = 'default-engine'

    File baseDir
    File outputDir

    void setup() {
        baseDir = new File(testProjectDir, 'base')
        outputDir = new File(testProjectDir, 'output')

        baseDir.mkdirs()
        new File(baseDir, 'subdir').mkdirs()
        new File(baseDir, 'index.adoc').text = '= Just a title'
        new File(baseDir, 'subdir/index2.adoc').text = '= Just a title too'
    }

    void 'Can run a asciidoctorj engine'() {
        setup:
        createBuildFile()

        when:
        getGradleRunner(IS_GROOVY_DSL, ['runEngine']).build()

        then:
        new File(outputDir, 'index.html').exists()
        new File(outputDir, 'subdir/index2.html').exists()
    }

    void createBuildFile() {
        buildFile.text = """
        import org.asciidoctor.gradle.model5.jvm.engines.*
        import org.asciidoctor.gradle.model5.core.internal.*
        import org.asciidoctor.gradle.model5.core.*
        
        plugins {
            id 'org.asciidoctor.core'
            id 'org.asciidoctor.jvm.base' apply false
        }

        ${getOfflineRepositories(DslType.GROOVY_DSL)}

        final exeSettings = project.objects.newInstance(DefaultAsciidoctorExecutionSettings).tap {
            safeMode.set(SafeMode.UNSAFE)
        }
        
        final docSettings = project.objects.newInstance(DefaultAsciidoctorConversionSettings).tap {
            backend.set(AsciidoctorNamedBackend.of('html'))
            sourceRootDir.set(new File('${baseDir.absolutePath}'))
            baseDir.set(new File('${baseDir.absolutePath}'))
            destinationDir.set(new File('${outputDir.absolutePath}'))
            attributes.set([:])
            sourceFiles.set(
                    ['index.adoc', 'subdir/index2.adoc'].collect {
                        new File('${baseDir.absolutePath}',it)
                    }.toSet()
            )
        }
        
        final engine = project.objects.newInstance(AsciidoctorjEngine, 'testEngine')
                
        tasks.register('runEngine') {
            doLast {
                engine.launcher.get().run(exeSettings,docSettings)
            }
        }
        """.stripIndent()
    }
}