package org.asciidoctor.gradle.model5.core;

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
