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
package org.asciidoctor.gradle.testfixtures.generators

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable

import static org.asciidoctor.gradle.testfixtures.AsciidoctorjTestVersions.SERIES_20

/** A test fixture generator class for AsciidoctorJ version & process mode.
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorjVersionProcessModeGenerator {

    public static final String JAVA_EXEC = 'JAVA_EXEC'
    public static final String IN_PROCESS = 'IN_PROCESS'
    public static final String OUT_OF_PROCESS = 'OUT_OF_PROCESS'

    @SuppressWarnings('ClassName')
    @EqualsAndHashCode
    @Sortable
    static class VersionProcess {
        String version
        String processMode

        static VersionProcess of(final String v, final String p) {
            new VersionProcess(version: v, processMode: p)
        }

        @Override
        String toString() {
            "Asciidoctorj: ${version}, mode: ${processMode}"
        }
    }

    static List<VersionProcess> get() {
        if (System.getenv('APPVEYOR') || System.getenv('TRAVIS') || System.getenv('GITHUB_ACTIONS')) {
            [SERIES_20].collect {
                VersionProcess.of(it, JAVA_EXEC)
            }.toUnique()
        } else {
            [SERIES_20].collectMany { it ->
                [
                    VersionProcess.of(it, JAVA_EXEC),
                    VersionProcess.of(it, IN_PROCESS),
                    VersionProcess.of(it, OUT_OF_PROCESS)
                ]
            }.toUnique() as List<VersionProcess>
        }
    }

    static List<VersionProcess> getRandom() {
        List<VersionProcess> vp = get()
        Collections.shuffle(vp)
        vp
    }
}
