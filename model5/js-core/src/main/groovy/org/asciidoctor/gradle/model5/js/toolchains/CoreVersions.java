/**
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.js.toolchains;

/**
 * Configuring core versions for {@code aAsciidoctor.js}.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface CoreVersions {
    /**
     * Overrides the default version of {@code aAsciidoctor.js}.
     *
     * @param v New version to be used. Can be of anything that can be resolved by
     *          {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    void useAsciidoctorjs(Object v);

    /**
     * Overrides the default version of Node.
     *
     * @param v Node version
     */
    void useNode(Object v);
}
