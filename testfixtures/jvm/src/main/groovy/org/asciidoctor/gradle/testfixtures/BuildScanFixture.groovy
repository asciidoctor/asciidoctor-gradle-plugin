/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.testfixtures

import groovy.transform.CompileStatic

/**
 * Support for running build scans during integration tests
 */
@CompileStatic
trait BuildScanFixture {
    boolean performBuildScan = false
    String buildScanServer

    String getBuildScanConfiguration() {
        """
            buildScan {
                ${buildScanServer ? "server = '${buildScanServer}'" : ''}
                termsOfServiceUrl = "https://gradle.com/terms-of-service"
                termsOfServiceAgree = "yes"
            }
        """
    }

    void enableBuildScan(String server = null) {
        performBuildScan = true
        buildScanServer = server
    }

    List<String> getBuildScanArguments() {
        performBuildScan ? ['--scan'] : []
    }
}