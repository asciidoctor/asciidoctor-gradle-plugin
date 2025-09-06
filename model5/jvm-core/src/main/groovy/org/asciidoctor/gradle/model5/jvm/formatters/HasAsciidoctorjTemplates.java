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
package org.asciidoctor.gradle.model5.jvm.formatters;

import org.asciidoctor.gradle.model5.core.formatters.HasTemplates;

/**
 * {@code asciidoctorj} output formatters that support templates implement this interface.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface HasAsciidoctorjTemplates extends HasTemplates  {

    /**
     * Forces the use of a single template engine,
     *
     * @param engineName Name of engine.
     */
    void setForceEngine(String engineName);

    /**
     * Places a supported engine on the GEM classpath.
     *
     * <p>
     *     The engine needs to be one of {@code erb}, {@code slim} or {@code haml}.
     * </p>
     *
     * @param engineName Engine name.
     */
    void useEngine(String engineName);

    /**
     * Places an engine on the GEM classpath.
     *
     * <p>
     *     This can be used to override the default version of {@code slim} or {@code haml}
     * </p>
     *
     * @param engineName Engine name. It must be an engine that is supported by {@code tilt}.
     * @param version GEM version. Anything convertible to a string.
     */
    void useEngine(String engineName, Object version);
}
