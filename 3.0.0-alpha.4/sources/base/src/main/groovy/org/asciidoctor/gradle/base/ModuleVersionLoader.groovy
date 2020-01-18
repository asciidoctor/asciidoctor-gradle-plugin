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
package org.asciidoctor.gradle.base

import groovy.transform.CompileStatic
import org.ysb33r.grolifant.api.MapUtils

/** Loads the versions of Asciidoctor modules that are required for a specific plugin.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
class ModuleVersionLoader {

    /** Loads the versions
     *
     * @param name Name of group
     * @return Map of versions
     */
    static Map<String, String> load(final String name) {
        final String resourcePath = "/META-INF/asciidoctor.gradle/${name}.properties"
        InputStream stream = ModuleVersionLoader.getResourceAsStream(resourcePath)

        if (stream == null) {
            throw new ModuleNotFoundException("Cannot load version resource at path ${resourcePath}")
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
