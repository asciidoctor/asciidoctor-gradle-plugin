// tag::using-two-plugins-three-backends[]
plugins {
    id 'org.asciidoctor.jvm.convert'
    id 'org.asciidoctor.jvm.pdf'
}

repositories {
    jcenter()
}

asciidoctorj {
    diagramVersion '1.5.4.1'
    logLevel 'INFO'
}

asciidoctor {

    outputOptions {
        backends 'html5', 'docbook'
    }

    sources {
        include 'sample.asciidoc'
    }

    resources {
        include 'images/**'
    }

    copyResourcesOnlyIf 'html5'
    useIntermediateWorkDir()
}

asciidoctorPdf {
    inProcess OUT_OF_PROCESS
    logDocuments true
    sourceDir 'src/docs/asciidoc'

    sources {
        include 'subdir/sample2.ad'
    }
}
// end::using-two-plugins-three-backends[]

task runGradleTest {
    dependsOn asciidoctor, asciidoctorPdf
}


// This is to work around a bug in Gradle. It is not part of normal build script.
// The side affect of this is that we actually get better code coverage as the process mode
// changes depending on which version of Gradle is being tested.
if(System.getProperty('CALLING_GRADLETEST_USES_GROOVY_VERSION') != GroovySystem.version) {
    asciidoctorPdf {
        inProcess JAVA_EXEC
    }
}