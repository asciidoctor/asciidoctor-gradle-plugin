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

import static org.asciidoctor.gradle.testfixtures.AsciidoctorjsTestVersions.DOCBOOK_SERIES_20
import static org.asciidoctor.gradle.testfixtures.AsciidoctorjsTestVersions.SERIES_20

/** A test fixture generator class for AsciidoctorJS versions.
 *
 * @since 3.0
 */
@CompileStatic
class AsciidoctorjsVersionGenerator {

    @SuppressWarnings('ClassName')
    @EqualsAndHashCode
    @Sortable
    static class Versions {
        String version
        String docbookVersion

        static Versions of(final String v, final String d) {
            new Versions(version: v, docbookVersion: d)
        }

        @Override
        String toString() {
            "Asciidoctorjs: ${version}, docbook: ${docbookVersion}"
        }
    }

    static List<Versions> get() {
        [
            Versions.of(SERIES_20, DOCBOOK_SERIES_20),
        ]
    }

    static List<Versions> getRandom() {
        List<Versions> vp = get()
        Collections.shuffle(vp)
        vp
    }
}
