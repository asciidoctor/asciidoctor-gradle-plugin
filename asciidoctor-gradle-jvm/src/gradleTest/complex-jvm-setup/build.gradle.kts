import org.asciidoctor.gradle.jvm.AsciidoctorPdfTask
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.jvm.OutputOptions
import org.asciidoctor.gradle.jvm.ProcessMode

plugins {
    id("org.asciidoctor.jvm.convert")  version "2.0.0-alpha.2"
    id("org.asciidoctor.jvm.pdf") version "2.0.0-alpha.2"
    id("com.gradle.build-scan") version "1.16"
}

repositories {
    jcenter()
}

asciidoctorj {
    setDiagramVersion("1.5.4.1")
    logLevel = LogLevel.INFO
}

tasks.named<AsciidoctorTask>("asciidoctor") {
    outputOptions {
        backends("html5", "docbook")
    }
    sources {
        include("sample.asciidoc")
    }
    resources {
        include("images/**")
    }
    copyResourcesOnlyIf("html5")
    useIntermediateWorkDir()
}

tasks.named<AsciidoctorPdfTask>("asciidoctorPdf") {
    inProcess = ProcessMode.OUT_OF_PROCESS
    logDocuments = true
    setSourceDir("src/docs/asciidoc")

    sources {
        include("subdir/sample2.ad")
    }


    if (findProperty("CALLING_GRADLETEST_USES_GROOVY_VERSION") == "GroovySystem.version") {
        inProcess = ProcessMode.JAVA_EXEC
    }
}

tasks.register<DefaultTask>("runGradleTest") {
    group = "Custom"
    dependsOn("asciidoctor", "asciidoctorPdf")
}



buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    publishAlways()
}