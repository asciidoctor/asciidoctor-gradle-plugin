import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.base.process.ProcessMode

// tag::using-two-plugins-three-backends[]
plugins {
    id("org.asciidoctor.jvm.convert")
//    id("com.gradle.build-scan") version "1.16"
}

repositories {
    jcenter()
}

asciidoctorj {
    modules.getDiagram().setVersion("1.5.16")
    logLevel = LogLevel.INFO
}

tasks.named<AsciidoctorTask>("asciidoctor") {
    outputOptions {
        backends("html5", "docbook")
    }

    sources ("sample.asciidoc")
    resources {
        include("images/**")
    }
    copyResourcesOnlyIf("html5")
    useIntermediateWorkDir()
}

// end::using-two-plugins-three-backends[]

tasks.register<DefaultTask>("runGradleTest") {
    group = "Custom"
    dependsOn("asciidoctor")
}
