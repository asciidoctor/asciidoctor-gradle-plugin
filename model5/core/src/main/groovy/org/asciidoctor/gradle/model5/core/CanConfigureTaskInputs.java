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

import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider;
import org.asciidoctor.gradle.model5.core.formatters.AsciidoctorOutputFormatter;
import org.gradle.api.tasks.TaskInputs;

/**
 * Indicates that the entity can configure task inputs.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface CanConfigureTaskInputs {
    /**
     * Primarily intended for indirect task creation and not usage in the DSL, this method will configure the inputs
     * of a given task to provide better up-to-date information.
     *
     * <p>
     *     This can be overridden by implementations as the default operation is a NOOP.
     *     Attributes i.e., {@link HasAttributeProvider#getAttributeProvider()}, should be used here, as attributes
     *     are dealt with correctly in another place.
     *     The output of {@link AsciidoctorOutputFormatter#getClasspath()} should not be used either as
     *     {@link org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTask} can correctly deal with the classpath.
     *     The main intent here is for additional configuration directories etc. to be passed as inputs.
     * </p>
     *
     * @param taskInputs The {@link TaskInputs} of a specific task instance.
     */
    default void configureTaskInputs(TaskInputs taskInputs) {}
}
