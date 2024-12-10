package org.asciidoctor.gradle.model5.core.internal.attributes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorAttributeProvider
import org.asciidoctor.gradle.model5.core.AttributeType
import org.asciidoctor.gradle.model5.core.Attributes
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.core.internal.attributes.AttributeUtils.resolveAttribute

/**
 * An engine-agnostic implementation of Asciidoctor attributes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAttributes implements Attributes {

    private final Map<String, Object> attrs
    private final List<AsciidoctorAttributeProvider> providers
    private final Provider<Map<String, String>> resolver

    @Inject
    DefaultAttributes(Project project) {
        final stringTools = ConfigCacheSafeOperations.from(project).stringTools()
        this.attrs = [:]
        this.providers = []

        this.resolver = project.provider { ->
            final collectedAttrs = owner.attrs.collectEntries { k, v ->
                [k, resolveAttribute(stringTools, v)]
            }
            owner.providers.each {
                it.getAttributes().collectEntries(collectedAttrs) { k, v ->
                    [k, resolveAttribute(stringTools, v)]
                }
            }
            collectedAttrs as Map<String, String>
        }
    }

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
    @Override
    Provider<Map<String, String>> getAttributeResolver() {
        this.resolver
    }

    /**
     * Apply a new set of Asciidoctor attributes, clearing any attributes previously set.
     *
     * This can be set globally for all Asciidoctor tasks in a project. If this is set in a task
     * it will override the global attributes.
     *
     * @param m Map with new options
     */
    @Override
    void replaceAll(Map<String, Object> m) {
        this.attrs.clear()
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
    void add(Map<String, Object> m) {
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
     * Returns a list of additional attribute providers.
     *
     * @return List of providers. Can be empty. Never {@code null}.
     */
    @Override
    List<AsciidoctorAttributeProvider> getAttributeProviders() {
        this.providers
    }

    /**
     * Adds an attribute provider.
     *
     * @param provider
     */
    @Override
    void attributeProvider(AsciidoctorAttributeProvider provider) {
        this.providers.add(provider)
    }

    /**
     * Adds a provider as an additional attribute provider.
     *
     * @param provider A provider that returns a {@code Map<String,Object>}.
     */
    @Override
    void attributeProvider(Provider<Map<String, Object>> provider) {
        attributeProvider(
                new AsciidoctorAttributeProvider() {
                    @Override
                    Map<String, Object> getAttributes() {
                        provider.get()
                    }
                }
        )
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
}
