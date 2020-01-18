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
package org.asciidoctor.gradle.remote

import org.asciidoctor.gradle.internal.ExecutorConfigurationContainer
import org.asciidoctor.gradle.remote.internal.RemoteSpecification

class AsciidoctorJExecutorSpec extends RemoteSpecification {

    void 'Can execute a worked-based conversion from a single backend'() {
        given:
        Map asciidoc = getProject(testProjectDir.root)
        ExecutorConfigurationContainer ecc = getContainerSingleEntry(asciidoc.src, asciidoc.outputDir)
        AsciidoctorJExecuter aje = new AsciidoctorJExecuter(ecc)

        when:
        aje.run()

        then:
        new File(asciidoc.outputDir, OUTPUT_HTML).exists()
    }

    void 'Can execute a worked-based conversion from multiple backends'() {
        given:
        Map asciidoc = getProject(testProjectDir.root)
        ExecutorConfigurationContainer ecc = getContainerMultipleEntries(
            asciidoc.src,
            asciidoc.outputDir,
            asciidoc.gemPath
        )
        AsciidoctorJExecuter aje = new AsciidoctorJExecuter(ecc)

        when:
        aje.run()

        then:
        new File(asciidoc.outputDir, OUTPUT_HTML).exists()
        new File(asciidoc.outputDir, OUTPUT_DOCBOOK).exists()
    }
}