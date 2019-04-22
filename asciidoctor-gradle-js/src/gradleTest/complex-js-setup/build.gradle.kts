// tag::using-multiple-backends-for-js[]

import org.asciidoctor.gradle.js.nodejs.AsciidoctorTask

plugins {
    id("org.asciidoctor.js.convert")
// end::using-multiple-backends-for-js[]
//    id("com.gradle.build-scan") version "1.16"
// tag::using-multiple-backends-for-js[]
}

repositories {
    jcenter()
}

asciidoctorjs {
    modules.docbook.use()
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
// end::using-multiple-backends-for-js[]

tasks.register<DefaultTask>("runGradleTest") {
    group = "Custom"
    dependsOn("asciidoctor")
}
