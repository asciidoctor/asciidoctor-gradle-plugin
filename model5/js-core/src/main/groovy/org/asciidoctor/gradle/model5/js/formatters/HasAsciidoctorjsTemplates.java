/**
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.model5.js.formatters;

import org.asciidoctor.gradle.model5.core.formatters.HasTemplates;

/**
 * {@code asciidoctor.js} output formatters that support templates implement this interface.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface HasAsciidoctorjsTemplates extends HasTemplates  {
    /**
     * Registers an NPM package.
     *
     * <p>
     *     The engine needs to be one of {@code js}, {@code handlebars}, {@code ejs}, {@code nunjucks} or {@code pug}.
     * </p>
     *
     * @param engineName Engine name.
     */
    void useEngine(String engineName);

    /**
     * Registers an NPM package overriding the default version
     *
     * <p>
     *     This can be used to override the default version of the supported engines except {@code js}
     * </p>
     *
     * @param engineName Engine name. It must be an engine that is supported by {@code asciidoctor.js}.
     * @param version NPM version. Anything convertible to a string.
     */
    void useEngine(String engineName, Object version);
}
