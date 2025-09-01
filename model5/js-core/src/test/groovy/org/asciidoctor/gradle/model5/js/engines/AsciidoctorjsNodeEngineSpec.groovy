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
package org.asciidoctor.gradle.model5.js.engines

import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.core.SafeMode
import org.asciidoctor.gradle.model5.core.internal.DefaultAsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.internal.DefaultAsciidoctorExecutionSettings
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification
import spock.lang.Ignore

@Ignore('Can probably delete this test now that we can execute AsciidoctorTask')
class AsciidoctorjsNodeEngineSpec extends UnitTestSpecification {

    public static final String ENGINE_NAME = 'default-engine'

    File baseDir
    File outputDir
    AsciidoctorjsNodeEngine engine
    DefaultAsciidoctorExecutionSettings exeSettings
    DefaultAsciidoctorConversionSettings conversionSettings

    void setup() {
        baseDir = new File(testProjectDir, 'base')
        outputDir = new File(testProjectDir, 'output')
        engine = project.objects.newInstance(AsciidoctorjsNodeEngine, ENGINE_NAME)
        exeSettings = project.objects.newInstance(DefaultAsciidoctorExecutionSettings)
        conversionSettings = project.objects.newInstance(DefaultAsciidoctorConversionSettings)

        baseDir.mkdirs()
        new File(baseDir, 'subdir').mkdirs()
        new File(baseDir, 'index.adoc').text = '= Just a title'
        new File(baseDir, 'subdir/index2.adoc').text = '= Just a title too'
    }

    void 'Can run a asciidoctor.js node engine'() {
        setup:
        final runner = engine.launcher.get()
        final bd = baseDir
        exeSettings.tap {
            safeMode.set(SafeMode.UNSAFE)

        }
        conversionSettings.tap {
            sourceFiles.set(
                    ['index.adoc', 'subdir/index2.adoc'].collect {
                        new File(bd, it)
                    }.toSet()
            )
            backend.set(AsciidoctorNamedBackend.of('html'))
            sourceRootDir.set(owner.baseDir)
            baseDir.set(owner.baseDir)
            destinationDir.set(outputDir)
            attributes.set([:])
        }

        when:
        runner.run(exeSettings, conversionSettings)

        then:
        new File(outputDir, 'index.html').exists()
        new File(outputDir, 'subdir/index2.html').exists()
    }
}