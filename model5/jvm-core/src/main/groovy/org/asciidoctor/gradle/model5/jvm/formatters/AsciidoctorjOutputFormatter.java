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
package org.asciidoctor.gradle.model5.jvm.formatters;

import org.asciidoctor.gradle.model5.core.formatters.AsciidoctorOutputFormatter;
import org.asciidoctor.gradle.model5.jvm.ExecutionMode;
import org.gradle.api.provider.Provider;

/**
 * Defines an output formatter that works on an {@code asciidoctorj} engine.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorjOutputFormatter extends AsciidoctorOutputFormatter {

    /**
     * Sets whether the workers should run in or out of process.
     *
     * @param mode Execution mode.
     */
    default void setExecutionMode(String mode) {
        setExecutionMode(ExecutionMode.of(mode));
    }

    /**
     * Sets whether the workers should run in or out of the Gradle process.
     *
     * @param mode Execution mode.
     */
    void setExecutionMode(ExecutionMode mode);

    /**
     * Get the execution mode for the formatter.
     *
     * @return Provider to execution mode.
     */
    Provider<ExecutionMode> getExecutionMode();
}
