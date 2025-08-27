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
package org.asciidoctor.gradle.model5.core.waitingroom;

import org.gradle.api.Named;

/**
 * Defines an output formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorOutputFormatter extends Named {

    /**
     * The name of the task that will be run for the given publication.
     *
     * @param publication Asciidoctor publication.
     *
     * @return Task name
     */
    String getAsciidoctorTaskName(AsciidoctorPublication publication);

    /**
     * Registers the tasks associated with this given output formatter, its toolchain and the corresponding publication.
     *
     * @param publication Publication
     */
    void registerTasksIfAbsent(AsciidoctorPublication publication);

    /**
     * The interface this output formatter instance represents, not the actual instance itself.
     *
     * @return CLass type of the output formatter interface.
     */
    Class<?> getOutputFormatterClass();
}
