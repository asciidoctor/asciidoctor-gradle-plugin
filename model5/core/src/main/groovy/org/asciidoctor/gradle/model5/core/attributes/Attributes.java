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

import org.gradle.api.provider.Provider;

import java.util.Map;

/**
 * An Asciidoctor-implementation agnostic representation of attributes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface Attributes {
    /**
     * Resolves all the Asciidoctor options.
     *
     * <p>
     *     Resolves values to either boolean, dates or strings.
     *     URIs are resolved to string with {@code toAsciiString()}.
     * </p>
     *
     * @return A map of resolved attributes
     */
    Provider<Map<String, String>> getAttributeResolver();

    /**
     * Apply a new set of Asciidoctor attributes, clearing any attributes and providers previously set.
     *
     * @param m Map with new options
     */
    void replaceAll(Map<String, Object> m);

    /**
     * Add additional Asciidoctor attributes.
     *
     * @param m Map with new options
     */
    void addAll(Map<String, Object> m);

    /** Add a single attribute
     *
     * @param key Name of attribute
     * @param value Value of attribute.
     */
    void add(String key, Object value);

    /**
     * Adds a provider as an additional attribute provider.
     *
     * @param provider A provider that returns a {@code Map<String,Object>}.
     */
    void attributeProvider(Provider<Map<String, Object>> provider);

    /**
     * Indicates that the value should be treated as a time value.
     *
     * @param value Value.
     * @return Something that can be passed as value in members of {@link #addAll(Map)}
     *   or as a single value to {@link #add(String, Object)}
     */
    AttributeType asTime(Object value);

    /**
     * Indicates that the value should be treated as a time value.
     *
     * @param key Attribute name
     * @param value Value.
     */
    default void addAsTime(String key, Object value) {
        add(key, asTime(value));
    }

    /**
     * Indicates that the value should be treated as a date value.
     *
     * @param value Value.
     * @return Something that can be passed as value in members of {@link #addAll(Map)}
     *   or as a single value to {@link #add(String, Object)}
     */
    AttributeType asDate(Object value);

    /**
     * Indicates that the value should be treated as a date value.
     *
     * @param key Attribute name
     * @param value Value.
     */
    default void addAsDate(String key, Object value) {
        add(key, asDate(value));
    }

    /**
     * Indicates that the value should be treated as a boolean value.
     *
     * @param value Value.
     * @return Something that can be passed as value in members of {@link #addAll(Map)}
     *   or as a single value to {@link #add(String, Object)}
     */
    AttributeType asBoolean(Object value);

    /**
     * Indicates that the value should be treated as a time value.
     *
     * @param key Attribute name
     * @param value Value.
     */
    default void addAsBoolean(String key, Object value) {
        add(key, asBoolean(value));
    }

}
