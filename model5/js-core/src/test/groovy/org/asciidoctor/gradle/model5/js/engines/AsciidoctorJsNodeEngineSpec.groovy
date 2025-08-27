package org.asciidoctor.gradle.model5.js.engines

import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.core.SafeMode
import org.asciidoctor.gradle.model5.core.internal.DefaultAsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.internal.DefaultAsciidoctorExecutionSettings
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

class AsciidoctorJsNodeEngineSpec extends UnitTestSpecification {

    public static final String ENGINE_NAME = 'default-engine'

    File baseDir
    File outputDir
    AsciidoctorJsNodeEngine engine
    DefaultAsciidoctorExecutionSettings exeSettings
    DefaultAsciidoctorConversionSettings conversionSettings

    void setup() {
        baseDir = new File(testProjectDir, 'base')
        outputDir = new File(testProjectDir, 'output')
        engine = project.objects.newInstance(AsciidoctorJsNodeEngine, ENGINE_NAME)
        exeSettings = project.objects.newInstance(DefaultAsciidoctorExecutionSettings)
        conversionSettings = project.objects.newInstance(DefaultAsciidoctorConversionSettings)

        baseDir.mkdirs()
        new File(baseDir, 'index.adoc').text = '= Just a title'
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
                    ['index.adoc'].collect {
                        new File(bd, it)
                    }.toSet()
            )
            backend.set(AsciidoctorNamedBackend.of('html'))
            baseDir.set(owner.baseDir)
            destinationDir.set(outputDir)
            attributes.set([:])
        }

        when:
        runner.run(exeSettings, conversionSettings)

        then:
        noExceptionThrown()
    }
}