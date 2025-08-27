package org.asciidoctor.gradle.model5.core;

import org.asciidoctor.gradle.model5.core.internal.backends.DefaultAsciidoctorNamedBackend;
import org.gradle.api.Named;

/**
 * Named backend which represents a backend, but which might be named differently.
 *
 * <p>
 *     In most cases {@link #getName()} will return the same as {@link #getBackend()}.
 * </p>
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 *
 */
public interface AsciidoctorNamedBackend extends Named {

    /**
     * Creates an instance of {@link AsciidoctorNamedBackend} where the alias and the abckend name is the same.
     *
     * @param backend Actual name of the backend.
     * @return Something that implements link AsciidoctorNamedBackend}.
     */
    static AsciidoctorNamedBackend of(String backend) {
        return new DefaultAsciidoctorNamedBackend(backend, backend);
    }

    /**
     * Creates an instance of {@link AsciidoctorNamedBackend}.
     * @param name Alias name of the backend.
     * @param backend Actual name of the backend.
     * @return Something that implements link AsciidoctorNamedBackend}.
     */
    static AsciidoctorNamedBackend of(String name,String backend) {
        return new DefaultAsciidoctorNamedBackend(name, backend);
    }

    /**
     * The actual backend.
     *
     * @return String representation of a real backend type.
     */
    String getBackend();
}
