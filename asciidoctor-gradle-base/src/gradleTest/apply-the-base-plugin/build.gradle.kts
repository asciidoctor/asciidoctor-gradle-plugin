plugins {
    id ("org.asciidoctor.base")
}

tasks.register<DefaultTask>("runGradleTest") {
    doLast {
        println("hello")
    }
}
