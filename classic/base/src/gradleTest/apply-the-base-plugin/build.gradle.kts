plugins {
    id ("org.asciidoctor.base.classic")
}

tasks.register<DefaultTask>("runGradleTest") {
    doLast {
        println("hello")
    }
}
