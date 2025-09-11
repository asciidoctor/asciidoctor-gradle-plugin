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
package org.asciidoctor.gradle.model5.js.components;

import java.util.Map;

/**
 * For defining components (backends or extensions) that are in cloud repositories.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorjsGenericComponent {
    /**
     * Set the package from the NPM repository here.
     *
     * @param scope The package scope. Can be {@code null}.
     * @param moduleName The package name.
     * @param version Something convertible to a string that can provide the version.
     */
    void usePackage(String scope, String moduleName, Object version);

    /**
     * Set the package from the NPM repository here.
     *
     * <p>This methoid is meant for packages without scopes.</p>
     *
     * @param moduleName The package name.
     * @param version Something convertible to a string that can provide the version.
     */
    default void usePackage(String moduleName, Object version) {
        usePackage(null, moduleName, version);
    }

    /**
     * If the backend needs one or more {@code require} entries to be passed to the engine, put it here.
     *
     * @param reqs One or more {@code require} entries
     */
    void requires(String... reqs);

    /**
     * Attributes that are specific to the backend.
     *
     * @param attrs Attributes of which the values are lazy-evaluated.
     */
    void attributes(Map<String, ?> attrs);
}
