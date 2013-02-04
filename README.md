Asciidoctor Gradle Plugin
-------------------------

This is a port of the [asciidoctor-maven-plugin][1] project by [@LightGuard][2]. Currently supports **html5** backend only.

Installation
------------


Use the following snippet

    buildscript {
        repositories {
            mavenLocal()
            mavenRepo name: 'Bintray', url: 'http://dl.bintray.com/content/aalmiray/asciidoctor'
        }
        dependencies {
            classpath 'org.asciidoctor:asciidoctor-gradle-plugin:0.1'
        }
    }
    apply plugin: 'asciidoctor'


Usage
-----

The plugin adds a new task named `asciidoctor`. This task exposes two properties as part of its configuration

 * sourceDir - where the asciidoc sources are. Type: File. Default: `src/asciidoc`.
 * outputDir - where generated docs go. Type: File. Default: `$buildDir/asciidoc`.
 
Sources may have any of the following extensions in order to be discovered

 * .asciidoc
 * .adoc
 * .asc
 * .ad


[1]: https://github.com/asciidoctor/asciidoctor-maven-plugin
[2]: https://github.com/LightGuard