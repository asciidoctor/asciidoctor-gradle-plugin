/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.kindlegen

import org.asciidoctor.gradle.kindlegen.internal.FunctionalSpecification
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.ysb33r.grolifant.api.OperatingSystem

@SuppressWarnings('MethodName')
class KindleGenFunctionalSpec extends FunctionalSpecification {

    void 'Downloader can download and unpack kindlegen'() {
        given:
        Project project = ProjectBuilder.builder().build()
        OperatingSystem os = OperatingSystem.current()
        String version = KindleGenExtension.DEFAULT_KINDLEGEN_VERSION
        String baseName = os.macOsX ? 'KindleGen' : 'kindlegen'
        String fileExt = os.linux ? 'tar.gz' : 'zip'
        String fileOs = os.linux ? 'linux_2.6_i386' : (os.windows ? 'win32' : 'Mac_i386')
        String fileName = "${baseName}_${fileOs}_v${version}.${fileExt}"
        KindleGenDownloader downloader = new KindleGenDownloader(version, project)

        when:
        File root = downloader.distributionRoot

        then:
        new File(root, fileName).exists()
    }

    void 'Download and cache kindlegen'() {
        given:
        new File(testProjectDir.root, 'build.gradle').text = """
plugins {
    id 'org.asciidoctor.kindlegen.base'
}

kindlegen {
    agreeToTermsOfUse = true
}

task getKindleGen {
    doLast {
        println kindlegen.resolvableExecutable.executable
    }
}
"""

        when:
        BuildResult result = getGradleRunner(['getKindleGen', '-i']).build()

        then:
        result.output.contains('/kindlegen') || result.output.contains('kindlegen.exe')
    }
}