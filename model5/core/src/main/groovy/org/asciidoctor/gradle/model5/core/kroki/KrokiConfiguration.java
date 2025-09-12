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
package org.asciidoctor.gradle.model5.core.kroki;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider;
import org.asciidoctor.gradle.model5.core.revealjs.FileOrUri;
import org.gradle.api.Action;

/**
 * Configuring Kroki
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface KrokiConfiguration extends HasAttributeProvider {
    /**
     * The URL of the Kroki server.
     *
     * <p>If not set, the implementation will use the one at {@code kroki.io}.</p>
     *
     * @param uri Anything convertible to a URI.
     */
    void setServerUri(Object uri);

    /**
     * Define if the images from the Kroki server should be stored to disk.
     *
     * @param flag {@code true} to download images.
     */
    void setFetchDiagrams(boolean flag);

    /**
     * Define how we should get the image from the Kroki server.
     *
     * <p>
     *     If {@code adaptive} is used, {@code get} will be used unless the URI length is longer than what was set by
     *     {@link #setMaxUriLength(int)}.
     * </p>
     * @param method {@code get}, {@code post}, or {@code adaptive}
     */
    void setKrokiMethod(String method);

    /**
     * Define the max URI length before using a POST request when using adaptive HTTP method.
     *
     * <p>If not set, an internal default will be used.</p>
     *
     * @param length Max length.
     */
    void setMaxUriLength(int length);

    /**
     * A file that will be included at the top of all PlantUML diagrams as if {@code !include} file was used.
     * This can be useful when you want to define a common skin for all your diagrams. The value can be a path or a URL.
     *
     * @param path Configure a file, a URI, or a relative path.
     */
    void plantUmlIncludePath(Action<FileOrUri> path);

    /**
     * A file that will be included at the top of all PlantUML diagrams as if {@code !include} file was used.
     * This can be useful when you want to define a common skin for all your diagrams. The value can be a path or a URL.
     *
     * @param path Configure a file, a URI, or a relative path.
     */
    void plantUmlIncludePath(@DelegatesTo(FileOrUri.class) Closure<?> path);

    /**
     * Search path(s) that will be used to resolve {@code !include} file additionally to current diagram directory,
     *     similar to PlantUML property plantuml.include.path.
     *
     * <p>
     *     The implementation will take care to present the correctly formatted value to the engine as required
     *     by the operating system conventions.
     * </p>
     * @param paths One or more search paths. Anything convertible to a file will do.
     */
    void plantUmlSearchPaths(Object... paths);
}
