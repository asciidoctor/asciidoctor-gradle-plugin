plugins {
    id ("org.asciidoctor.editorconfig")
}

tasks.register<DefaultTask>("runGradleTest") {
    dependsOn("asciidoctorEditorConfig")
}
