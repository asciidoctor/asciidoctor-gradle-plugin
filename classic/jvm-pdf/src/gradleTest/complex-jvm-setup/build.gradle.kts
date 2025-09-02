import org.asciidoctor.gradle.jvm.pdf.AsciidoctorPdfTask
import org.asciidoctor.gradle.base.process.ProcessMode

// tag::using-two-plugins-three-backends[]
plugins {
    id("org.asciidoctor.jvm.pdf.classic")
}

apply(from = "${System.getProperty("OFFLINE_REPO")}/repository.gradle.kts")
//repositories {
//    mavenCentral()
//}

asciidoctorj {
    modules.getDiagram().setVersion("3.0.1")
    logLevel = LogLevel.INFO
}

tasks.named<AsciidoctorPdfTask>("asciidoctorPdf") {
    setExecutionMode("OUT_OF_PROCESS")
    logDocuments = true
    setSourceDir("src/docs/asciidoc")

    sources ("subdir/sample2.ad")
}
// end::using-two-plugins-three-backends[]

tasks.register<DefaultTask>("runGradleTest") {
    group = "Custom"
    dependsOn("asciidoctorPdf")
}
