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
package org.asciidoctor.gradle.testfixtures.internal

import groovy.transform.CompileStatic
import org.ysb33r.grolifant.api.MapUtils

/** Loads AsciidoctorJ(S) versions and associated module versions from the properties map to make them available
 * to tests.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class TestFixtureVersionLoader {

    public static final Map<String, String> VERSIONS = load()

    private static Map<String, String> load() {
        final String resourcePath = '/META-INF/asciidoctor.gradle/testfixtures-core.properties'
        InputStream stream = TestFixtureVersionLoader.getResourceAsStream(resourcePath)

        if (stream == null) {
            throw new FileNotFoundException("Cannot load version resource at path ${resourcePath}")
        }

        try {
            Properties props = new Properties()
            props.load(stream)
            MapUtils.stringizeValues(props as Map<String, Object>).asImmutable()
        } finally {
            stream.close()
        }
    }
}
