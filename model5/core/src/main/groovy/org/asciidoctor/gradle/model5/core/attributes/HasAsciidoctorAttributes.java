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
package org.asciidoctor.gradle.model5.core.attributes;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

/**
 * Indicates the DSL item has Asciidoctor attributes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface HasAsciidoctorAttributes {
    /**
     * Configures the attributes for this publication
     *
     * @param configurator Configurator
     */
    void attributes(Action<Attributes> configurator);

    /**
     * Configures the attributes for this publication
     *
     * @param configurator Configurator
     */
    void attributes(@DelegatesTo(Attributes.class) Closure configurator);

    /**
     * Direct access to the attributes for this publication.
     *
     * @return Attributes
     */
    Attributes getAttributes();
}
