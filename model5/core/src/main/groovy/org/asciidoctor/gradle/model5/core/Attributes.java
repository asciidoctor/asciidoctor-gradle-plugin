package org.asciidoctor.gradle.model5.core;

import org.gradle.api.provider.Provider;

import java.util.List;
import java.util.Map;

/**
 * An Asciidoctor-implementation agnostic representation of attributes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface Attributes {

     /* -------------------------
       tag::extension-property[]
        attributes:: Asciidoctor attributes.
          Use `attributes` to append and `setAttributes` to replace any current attributes with a new set.
          Attribute values are lazy-evaluated to strings.
          See <<options-and-attributes,Setting Attributes>> for more detail.
       end::extension-property[]
       ------------------------- */

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
     * Apply a new set of Asciidoctor attributes, clearing any attributes previously set.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will override the global attributes.
     *
     * @param m Map with new options
     */
    void replaceAll(Map<String,Object> m);

    /**
     * Add additional Asciidoctor attributes.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will use this attributes in the task in addition to any global attributes.
     *
     * @param m Map with new options
     */
    void add(Map<String,Object> m);

    /** Add a single attribute
     *
     * @param key Name of attribute
     * @param value Value of attribute.
     */
    void add(String key, Object value);

    /* -------------------------
       tag::extension-property[]
       attributeProviders:: Additional sources where attributes can be obtained from.
         Attribute providers are useful for where changes should not cause a rebuild of the docs.
       end::extension-property[]
       ------------------------- */

    /**
     * Returns a list of additional attribute providers.
     *
     * @return List of providers. Can be empty. Never {@code null}.
     */
    List<AsciidoctorAttributeProvider> getAttributeProviders();

    /**
     * Adds an attribute provider.
     *
     * @param provider An external provder of Asciidoc attributes.
     */
    void attributeProvider(AsciidoctorAttributeProvider provider);

    /**
     * Adds a provider as an additional attribute provider.
     *
     * @param provider A provider that returns a {@code Map<String,Object>}.
     */
    void attributeProvider(Provider<Map<String,Object>> provider);

    /**
     * Indicates that the value should be treated as a time value.
     *
     * @param value Value.
     * @return Something that can be passed as value in members of {@link #add(Map)}
     *   or as a single value to {@link #add(String, Object)}
     */
    AttributeType asTime(Object value);

    /**
     * Indicates that the value should be treated as a date value.
     *
     * @param value Value.
     * @return Something that can be passed as value in members of {@link #add(Map)}
     *   or as a single value to {@link #add(String, Object)}
     */
    AttributeType asDate(Object value);
}
