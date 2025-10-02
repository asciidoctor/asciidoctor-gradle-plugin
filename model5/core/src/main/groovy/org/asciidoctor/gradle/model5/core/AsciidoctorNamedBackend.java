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
