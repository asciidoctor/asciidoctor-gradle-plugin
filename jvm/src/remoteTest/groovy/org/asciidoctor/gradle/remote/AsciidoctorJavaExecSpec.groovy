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

class AsciidoctorJavaExecSpec extends RemoteSpecification {

    void 'Can execute a conversion from execution specification'() {
        given:
        Map asciidoc = getProject(testProjectDir.root)
        AsciidoctorJavaExec aje = new AsciidoctorJavaExec(getContainerSingleEntry(asciidoc.src, asciidoc.outputDir))

        when:
        aje.run()

        then:
        new File(asciidoc.outputDir, OUTPUT_HTML).exists()
        new File(asciidoc.outputDir, OUTPUT_HTML2).exists()
    }

    void 'Can execute a conversion using serialised execution specification'() {
        given:
        File executionData = new File(testProjectDir.root, 'execdata')
        Map asciidoc = getProject(testProjectDir.root)
        ExecutorConfigurationContainer ecc = getContainerMultipleEntries(
            asciidoc.src,
            asciidoc.outputDir,
            asciidoc.gemPath
        )
        ecc.toFile(executionData, ecc.configurations)

        when:
        AsciidoctorJavaExec.main([executionData.absolutePath].toArray() as String[])

        then:
        new File(asciidoc.outputDir, OUTPUT_HTML).exists()
        new File(asciidoc.outputDir, OUTPUT_HTML2).exists()
        new File(asciidoc.outputDir, OUTPUT_DOCBOOK).exists()
    }

    void 'Requires a serialised execution specification'() {
        when:
        AsciidoctorJavaExec.main(new String[0])

        then:
        thrown(AsciidoctorRemoteExecutionException)
    }

    void 'Should throw an exception when failure level is reached or exceeded'() {
        given:
        Map asciidoc = getProject(testProjectDir.root)
        AsciidoctorJavaExec aje = new AsciidoctorJavaExec(new ExecutorConfigurationContainer(
                getExecutorConfiguration(HTML, asciidoc.src, new File(asciidoc.outputDir, OUTPUT_HTML), null, 1)
        ))

        when:
        aje.run()

        then:
        thrown(AsciidoctorRemoteExecutionException)
    }
}