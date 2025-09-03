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

import org.asciidoctor.gradle.model5.core.*;
import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider;
import org.gradle.api.Named;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

import java.util.Optional;

/**
 * Defines an output formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorOutputFormatter extends Named, AsciidoctorRequires, HasAttributeProvider,
        HasDisplayType, CanConfigureTaskInputs {

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
     * Additional items to add to the classpath when a conversion involving the output formatter is executed.
     *
     * <p>
     *     The classpath is empty by default.
     * </p>
     *
     * @return Classpath. Can be {@code null} to indicate that the formatter does not support additional classpath.
     */
    FileCollection getClasspath();
}
