/**
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.core.formatters;

import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend;
import org.asciidoctor.gradle.model5.core.DocType;
import org.gradle.api.Named;
import org.gradle.api.provider.Provider;

import java.util.Collections;
import java.util.Optional;

/**
 * Defines an output formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorOutputFormatter extends Named {

    /**
     * What this is known to Asciidoctor as the backend.
     *
     * <p>
     *     It is possible to register more than one output formatter of the same type, but with different settings.
     *     The backend name will always be the same in this case.
     * </p>
     *
     * @return Backend name.
     */
    Provider<AsciidoctorNamedBackend> getBackend();

    /**
     * Whether resource files should be copied to the output directory.
     *
     * @return {@code true} if resources should be copied.
     */
    boolean getCopyResources();

    /**
     * Indicates that this backend always requires a specific document type and will try to enforce it.
     *
     * <p>
     *     The default is not to enforce a document type.
     * </p>
     *
     * @return Enforced document type. Can be empty indicating that nothing is enforced.
     */
    default Optional<DocType> getEnforcedDocType() {
        return Optional.empty();
    }

    /**
     * A list of {@code requires} that an output formatter places on the associated toolchain.
     *
     * @return List of {@code requires}. Can be empty, but never {@code null}.
     */
    default Iterable<String> getRequires() {
        return Collections.EMPTY_LIST;
    }
}
