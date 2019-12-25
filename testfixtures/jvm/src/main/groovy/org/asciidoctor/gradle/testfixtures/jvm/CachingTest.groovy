/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.gradle.testfixtures.jvm

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.rules.TemporaryFolder

import java.nio.file.Path
import java.nio.file.Paths

/**
 * A set of convenience methods for testing compatibility with the build cache.
 */
trait CachingTest {
    Boolean scan
    String scanServer

    void setupCache() {
        // Use a test-specific build cache directory.  This ensures that we'll only use cached outputs generated during
        // this test and we won't accidentally use cached outputs from a different test or a different build.
        file('settings.gradle') << """
            rootProject.name = 'test'

            buildCache {
                local() {
                    directory = new File(rootDir, 'build-cache')
                }
            }
        """
    }

    String getBuildScanConfiguration() {
        """
            buildScan {
                ${scanServer ? "server = '${scanServer}'" : ''}
                termsOfServiceUrl = "https://gradle.com/terms-of-service"
                termsOfServiceAgree = "yes"
            }
        """
    }

    void enableBuildScan(String server = null) {
        scan = true
        scanServer = server
    }

    void assertTaskRunsWithOutcomeInDir(String task, TaskOutcome outcome, File projectDir) {
        List<String> scanArguments = scan ? ['--scan'] : []
        List<String> buildArguments = ['clean', task, '--build-cache'] + scanArguments
        BuildResult result = FunctionalTestSetup.getGradleRunner(projectDir, buildArguments).build()
        assert result.task(task).outcome == outcome
    }

    void assertTaskRunsWithOutcome(String task, TaskOutcome outcome) {
        assertTaskRunsWithOutcomeInDir(task, outcome, testProjectDir.root)
    }

    void assertDefaultTaskExecutes() {
        assertTaskRunsWithOutcome(defaultTask, TaskOutcome.SUCCESS)
    }

    void assertDefaultTaskIsCached() {
        assertTaskRunsWithOutcome(defaultTask, TaskOutcome.FROM_CACHE)
    }

    void assertDefaultTaskIsCachedInRelocatedDirectory() {
        assertTaskRunsWithOutcomeInDir(defaultTask, TaskOutcome.FROM_CACHE, alternateProjectDir.root)
    }

    void assertDefaultTaskIsCachedAndRelocatable() {
        assertDefaultTaskIsCached()
        FileUtils.copyDirectory(testProjectDir.root, alternateProjectDir.root)
        assertDefaultTaskIsCachedInRelocatedDirectory()
    }

    File getOutputFileInRelocatedDirectory() {
        Path basePath = Paths.get(testProjectDir.root.toURI())
        Path outputFilePath = Paths.get(outputFile.toURI())
        Path relativeOutputFilePath = basePath.relativize(outputFilePath)
        fileInRelocatedDirectory(relativeOutputFilePath.toString())
    }

    File fileInRelocatedDirectory(String relativePath) {
        new File(alternateProjectDir.root, relativePath)
    }

    void deleteIfExists(File file) {
        if (file.exists()) {
            assert file.delete()
        }
    }

    File file(String relativePath) {
        new File(testProjectDir.root, relativePath)
    }

    void changeBuildConfigurationTo(String extraContent) {
        deleteIfExists(file('build.gradle'))
        getBuildFile(extraContent)
    }

    abstract File getBuildFile(String extraContent)
    abstract File getOutputFile()
    abstract String getDefaultTask()
    abstract TemporaryFolder getTestProjectDir()
    abstract TemporaryFolder getAlternateProjectDir()
}