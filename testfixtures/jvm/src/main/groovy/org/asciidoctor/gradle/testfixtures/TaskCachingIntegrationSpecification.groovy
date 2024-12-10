package org.asciidoctor.gradle.testfixtures

abstract class TaskCachingIntegrationSpecification extends IntegrationSpecification implements CachingTestFixture {

    @Override
    List<String> getBuildScanArguments() {
        []
    }

    @Override
    File getAlternateProjectDir() {
        new File(testProjectDir, 'alternate')
    }

    @Override
    File getOutputFile() {
       new File(buildDir,'docs/asciidoc/sample.html')
    }

    @Override
    File getBuildFile(String extraContent) {
        return null
    }
}