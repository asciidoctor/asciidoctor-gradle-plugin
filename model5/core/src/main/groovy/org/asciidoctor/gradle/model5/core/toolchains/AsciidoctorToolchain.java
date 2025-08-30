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
package org.asciidoctor.gradle.model5.core.toolchains;

import org.asciidoctor.gradle.model5.core.formatters.AsciidoctorOutputFormatter;
import org.asciidoctor.gradle.model5.core.engines.AsciidoctorEngine;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;

import java.util.Collections;

/**
 *
 * Represents an Asciidoctor toolchain.
 *
 * <p>
 *     A toolchain includes an engine
 * </p>
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorToolchain extends AsciidoctorEngine, ProcessingOptions {

    /**
     * Output formatters registered with this toolchain.
     *
     * @return Container of registered output formatters.
     */
    ExtensiblePolymorphicDomainObjectContainer<AsciidoctorOutputFormatter> getRegisteredOutputFormatters();

    /**
     * A list of tasks that will perform toolchain-related preparation before conversion using the toolchain can start.
     *
     * @return List of task names. Can be empty, but never {@code null}
     */
    default Iterable<String> getToolchainPreparationTaskNames() {
        return Collections.EMPTY_LIST;
    }
//
//    /**
//     * The interface this toolchain instance represents, not the actual instance itself.
//     *
//     * @return Class type of the toolchain interface.
//     */
//    Class<?> getToolchainClass();
}
