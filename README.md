Asciidoctor Gradle Plugin
-------------------------

[![Travis Build Status](https://travis-ci.org/asciidoctor/asciidoctor-gradle-plugin.png?branch=master)](https://travis-ci.org/asciidoctor/asciidoctor-gradle-plugin)

This is a port of the [asciidoctor-maven-plugin][1] project by [@LightGuard][2]. Relies on [asciidoctor-java-integration][3] by [@lordofthejars][4].

Installation
------------

Use the following snippet

    buildscript {
        repositories {
            mavenRepo name: 'Bintray Asciidoctor repo', url: 'http://dl.bintray.com/content/aalmiray/asciidoctor'
            mavenRepo name: 'Bintray JCenter', url: 'http://jcenter.bintray.com'
        }

        dependencies {
            classpath 'org.asciidoctor:asciidoctor-gradle-plugin:0.4.0'
        }
    }

    apply plugin: 'asciidoctor'


Usage
-----

The plugin adds a new task named `asciidoctor`. This task exposes 4 properties as part of its configuration

 * sourceDir - where the asciidoc sources are. Type: File. Default: `src/asciidoc`.
 * sourceDocumentName - an override to process a single source file; defaults to all files in `${sourceDir}`
 * outputDir - where generated docs go. Type: File. Default: `$buildDir/asciidoc`.
 * backend - the backend to use. Type: String. Default: `html5`.
 * options - a Map specifying different options that can be sent to Asciidoctor.
 
Sources may have any of the following extensions in order to be discovered

 * .asciidoc
 * .adoc
 * .asc
 * .ad

Configuration
-------------

The following options may be set using the task's `options` property

 * header_footer - (boolean)
 * template_dir - (String)
 * template_engine - (String)
 * compact - (boolean)
 * doctype - (String)
 * attributes - (Map)

Any key/values set on `attributes` are sent as is to Asciidoctor. You may use this Map to specify an stylesheet for example.
Refer to the [Ascidoctor documentation][asciidoctor_docs] to learn more about these options and attributes.

History
-------

### 0.4.0

 * Supports Asciidoctor 0.1.3
 * Fixes [#13][issue_13]. Rendering under Windows fails.
 * Fixes [#14][issue_14]. Stylesheet attributes cannot take a GString for a value.
 * Fixes [#15][issue_15]. Provide `sourceDocumentName` as a property.

### 0.3.0

 * Supports Asciidoctor 0.1.2
 * Rely on asciidoctor-java.integration

### 0.2.2

 * Fixes [#7][issue_7]. Make up-to-date directory detection work for AsciidoctorTask.

### 0.2.1

 * Fixes [#6][issue_6]. Can't run plugin if Gradle daemon is active.

### 0.2

 * Support Asciidoctor 0.1.1
 * Backends: html5, docbook

### 0.1

 * First release.
 * Supports Asciidoctor 0.0.9
 * Backends: html5

[1]: https://github.com/asciidoctor/asciidoctor-maven-plugin
[2]: https://github.com/LightGuard
[3]: https://github.com/asciidoctor/asciidoctor-java-integration
[4]: https://github.com/lordofthejars
[issue_6]: https://github.com/asciidoctor/asciidoctor-gradle-plugin/pull/6
[issue_7]: https://github.com/asciidoctor/asciidoctor-gradle-plugin/pull/7
[issue_13]: https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/13
[issue_14]: https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/14
[issue_15]: https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/15
[asciidoctor_docs]: http://asciidoctor.org/docs/
