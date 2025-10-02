/*
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
package org.asciidoctor.gradle.model5.core.internal.attributes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.attributes.AttributeType
import org.asciidoctor.gradle.model5.core.attributes.Attributes
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * An engine-agnostic implementation of Asciidoctor attributes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAttributes implements Attributes {

    final Provider<Map<String, String>> attributeResolver
    private final MapProperty<String, Object> attrs

    @Inject
    DefaultAttributes(Project project) {
        final stringTools = ConfigCacheSafeOperations.from(project).stringTools()
        this.attrs = project.objects.mapProperty(String, Object)
        this.attributeResolver = AttributeUtils.resolvingProvider(stringTools, attrs)
    }

    /**
     * Apply a new set of Asciidoctor attributes, clearing any attributes and providers previously set.
     *
     * @param m Map with new options
     */
    @Override
    void replaceAll(Map<String, Object> m) {
        this.attrs.set(Collections.EMPTY_MAP)
        this.attrs.putAll(m)
    }

    /**
     * Add additional Asciidoctor attributes.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will use this attributes in the task in addition to any global attributes.
     *
     * @param m Map with new options
     */
    @Override
    void addAll(Map<String, Object> m) {
        this.attrs.putAll(m)
    }

    /** Add a single attribute
     *
     * @param key Name of attribute
     * @param value Value of attribute.
     */
    @Override
    void add(String key, Object value) {
        this.attrs.putAll([(key): value])
    }

    /**
     * Adds a provider as an additional attribute provider.
     *
     * @param provider A provider that returns a {@code Map<String,Object>}.
     */
    @Override
    void attributeProvider(Provider<Map<String, Object>> provider) {
        this.attrs.putAll(provider)
    }

    /**
     * Indicates that the value should be treated as a time value.
     *
     * @param value Value.
     * @return Something that can be passed as value in members of {@link #add(Map)}
     *   or as a single value to {@link #add(String, Object)}
     */
    @Override
    AttributeType asTime(Object value) {
        new TimeType(value)
    }

    /**
     * Indicates that the value should be treated as a date value.
     *
     * @param value Value.
     * @return Something that can be passed as value in members of {@link #add(Map)}
     *   or as a single value to {@link #add(String, Object)}
     */
    @Override
    AttributeType asDate(Object value) {
        new DateType(value)
    }

    /**
     * Indicates that the value should be treated as a boolean value.
     *
     * @param value Value.
     * @return Something that can be passed as value in members of {@link #add(Map)}
     *   or as a single value to {@link #add(String, Object)}
     */
    @Override
    AttributeType asBoolean(Object value) {
        new BooleanType(value)
    }
}
