/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.base.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorAttributeProvider
import org.asciidoctor.gradle.base.Transform
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant.api.core.ProjectOperations
import org.ysb33r.grolifant.api.core.StringTools

import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.function.Supplier

/**
 * Utilities for dealing with Asciidoctor attributes
 *
 * @author Schalk W. Cronjé
 *
 * @since 4.0
 */
@CompileStatic
class AsciidoctorAttributes {
    /**
     * Recursively resolves all content recursively into string, booleans, containers of string & boolean,
     * and providers of string & boolean.
     *
     * @param attrs Attributes
     * @param po {@link ProjectOperations} instance
     * @return Cacheable map whicvh can contain providers.
     */
    static Map<String, Object> resolveAsCacheable(Map<String, Object> attrs, ProjectOperations po) {
        resolveMapRecursive(attrs, po)
    }

    /**
     * Recursively resolves all content recursively into string, booleans, containers of string & boolean,
     * and providers of string & boolean.
     *
     * All providers are resolved into string & boolean or containers of these two types.
     *
     * @param attrs Attributues
     * @param stringTools Reference to a {@link StringTools} instance.
     * @return Serializable map.
     */
    static Map<String, Object> resolveAsSerializable(Map<String, Object> attrs, StringTools stringTools) {
        final Map<String, Object> resolvedAs = [:] as TreeMap<String, Object>
        attrs.forEach { String key, Object value ->
            resolvedAs[key] = resolveItemAsBasicType(value, stringTools)
        }
        resolvedAs
    }

    /**
     * Resolves all provider into the underlying type.
     *
     * @param initialMap Map possible containing Providers as values.
     * @return Map with top-layer of providers stripped out.
     *
     * @since 4.0
     */
    static Map<String, Object> evaluateProviders(final Map<String, Object> initialMap) {
        initialMap.collectEntries { String k, Object v ->
            if (v instanceof Provider) {
                [k, v.get()]
            } else {
                [k, v]
            }
        } as Map<String, Object>
    }

    /**
     * Prepare attributes to be serialisable
     *
     * @param stringTools {@link StringTools} instance to use for stringification.
     * @param workingSourceDir Working source directory from which source documents will be made available.
     * @param seedAttributes Initial attributes set on the task.
     * @param langAttributes Any language specific attributes.
     * @param tdsAttributes Task-specific default attributes.
     * @param attributeProviders Additional attribute providers.
     * @param lang Language being processed. Can be unset if multi-language feature is not used.
     * @return Attributes ready for serialisation.
     *
     * @since 3.0.0
     */
    @SuppressWarnings('ParameterCount')
    static Map<String, Object> prepareAttributes(
            final StringTools stringTools,
            Map<String, ?> seedAttributes,
            Map<String, ?> langAttributes,
            Map<String, ?> tsdAttributes,
            List<AsciidoctorAttributeProvider> attributeProviders,
            Optional<String> lang
    ) {
        Map<String, Object> attrs = [:]
        attrs.putAll(seedAttributes)
        attrs.putAll(langAttributes)
        attributeProviders.each {
            attrs.putAll(it.attributes)
        }

        Map<String, Object> defaultAttrs = prepareDefaultAttributes(
                stringTools,
                attrs,
                tsdAttributes,
                lang
        )
        attrs.putAll(defaultAttrs)
        evaluateProviders(attrs)
    }

    private static Map<String, Object> prepareDefaultAttributes(
            final StringTools stringTools,
            Map<String, ?> seedAttributes,
            Map<String, ?> defaultAttributes,
            Optional<String> lang
    ) {
        Set<String> userDefinedAttrKeys = trimOverridableAttributeNotation(seedAttributes.keySet())

        Map<String, Object> defaultAttrs = defaultAttributes.findAll { k, v ->
            !userDefinedAttrKeys.contains(k)
        }.collectEntries { k, v ->
            ["${k}@".toString(), v instanceof Serializable ? v : stringTools.stringize(v)]
        } as Map<String, Object>

        if (lang.present) {
            defaultAttrs.put('lang@', lang.get())
        }

        defaultAttrs
    }

    private static Set<String> trimOverridableAttributeNotation(Set<String> attributeKeys) {
        // remove possible trailing '@' character that is used to encode that the attribute can be overridden
        // in the document itself
        Transform.toSet(attributeKeys) { k -> k - ~/@$/ }
    }

    private static Object resolveItemAsBasicType(Object value, StringTools stringTools) {
        switch (value) {
            case URI:
            case URL:
            case File:
            case Path:
            case String:
            case Boolean:
                return value
            case Map:
                return resolveMapToBasicTypes(value as Map<String, Object>, stringTools)
            case Collection:
                return resolveCollectionToBasicTypes(value as Collection<Object>, stringTools)
            case Provider:
                return resolveItemAsBasicType(((Provider) value).get(), stringTools)
            case Supplier:
                return resolveItemAsBasicType(((Supplier) value).get(), stringTools)
            case Callable:
                return resolveItemAsBasicType(((Callable) value).call(), stringTools)
            default:
                return stringTools.stringize(value)
        }
    }

    private static Map<String, Object> resolveMapToBasicTypes(Map<String, Object> attrs, StringTools stringTools) {
        final Map<String, Object> resolvedAs = [:] as TreeMap<String, Object>
        attrs.forEach { String key, Object value ->
            resolvedAs[key] = resolveItemAsBasicType(value, stringTools)
        }
        resolvedAs
    }

    private static Collection<Object> resolveCollectionToBasicTypes(
            Collection<Object> attrValues,
            StringTools stringTools
    ) {
        final Collection<Object> resolvedAs = attrValues instanceof Set ? [].toSet() : []
        attrValues.forEach { Object value ->
            resolvedAs.add(resolveItemAsBasicType(value, stringTools))
        }
        resolvedAs
    }

    private static Map<String, Object> resolveMapRecursive(Map<String, Object> attrs, ProjectOperations po) {
        final Map<String, Object> resolvedAs = [:] as TreeMap<String, Object>
        attrs.forEach { String key, Object value ->
            switch (value) {
                case Map:
                    resolvedAs[key] = resolveMapRecursive(value as Map<String, Object>, po)
                    break
                case Collection:
                    resolvedAs[key] = resolveCollectionRecursive(value as Collection<Object>, po)
                    break
                default:
                    resolvedAs[key] = resolveSingleItem(value, po)
            }
        }
        resolvedAs
    }

    private static Collection<Object> resolveCollectionRecursive(Collection<Object> attrValues, ProjectOperations po) {
        final Collection<Object> resolvedAs = attrValues instanceof Set ? [].toSet() : []
        attrValues.forEach { Object value ->
            switch (value) {
                case Map:
                    resolvedAs.add(resolveMapRecursive(value as Map<String, Object>, po))
                    break
                case Collection:
                    resolvedAs.add(resolveCollectionRecursive(value as Collection<Object>, po))
                    break
                default:
                    resolvedAs.add(resolveSingleItem(value, po))
            }
        }
        resolvedAs
    }

    private static Object resolveSingleItem(Object value, ProjectOperations po) {
        switch (value) {
            case Map:
                return resolveMapRecursive(value as Map<String, Object>, po)
            case Collection:
                return resolveCollectionRecursive(value as Collection<Object>, po)
            case Callable:
                return po.provider(value as Callable<Object>)
            case Supplier:
                return po.provider({ -> ((Supplier) value).get() } as Callable<Object>)
            case URI:
            case URL:
            case Provider:
            case File:
            case Path:
            case String:
            case Boolean:
                return value
            default:
                return po.provider { -> po.stringTools.stringize(value) }
        }
    }
}
