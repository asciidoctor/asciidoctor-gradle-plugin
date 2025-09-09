plugins {
    id ("org.asciidoctor.editorconfig.classic")
}

tasks.register<DefaultTask>("runGradleTest") {
    dependsOn("asciidoctorEditorConfig")
}
