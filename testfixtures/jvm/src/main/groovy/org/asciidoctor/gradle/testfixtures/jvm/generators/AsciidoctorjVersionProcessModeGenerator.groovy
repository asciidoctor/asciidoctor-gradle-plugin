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
package org.asciidoctor.gradle.testfixtures.jvm.generators

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions

/** A test fixture generator class for AsciidoctorJ version & process mode.
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorjVersionProcessModeGenerator {

    static final String JAVA_EXEC = 'JAVA_EXEC'
    static final String IN_PROCESS = 'IN_PROCESS'
    static final String OUT_OF_PROCESS = 'OUT_OF_PROCESS'

    @SuppressWarnings('ClassName')
    @EqualsAndHashCode
    @Sortable
    static class VersionProcess {
        String version
        String processMode

        @Override
        String toString() {
            "Asciidoctorj: ${version}, mode: ${processMode}"
        }

        static VersionProcess of(final String v, final String p) {
            new VersionProcess(version: v, processMode: p)
        }
    }

    static List<VersionProcess> get() {
        if (System.getenv('APPVEYOR')) {
            [AsciidoctorjTestVersions.SERIES_20, AsciidoctorjTestVersions.SERIES_16].collect {
                VersionProcess.of(it, JAVA_EXEC)
            }.toUnique()
        } else {
            [AsciidoctorjTestVersions.SERIES_20, AsciidoctorjTestVersions.SERIES_16].collect { it ->
                [
                    VersionProcess.of(it, JAVA_EXEC),
                    VersionProcess.of(it, IN_PROCESS),
                    VersionProcess.of(it, OUT_OF_PROCESS)
                ]
            }.flatten().toUnique() as List<VersionProcess>
        }
    }

    static List<VersionProcess> getRandom() {
        List<VersionProcess> vp = get()
        Collections.shuffle(vp)
        vp
    }
}
