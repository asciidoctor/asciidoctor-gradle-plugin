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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.asciidoctor.gradle.model5.core.formatters.AsciidoctorOutputFormatter;
import org.gradle.api.Action;
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleSetJavaForkOptions;

/**
 * Defines an output formatter that works on an {@code asciidoctorj} engine.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorjOutputFormatter extends AsciidoctorOutputFormatter {

    /**
     * When running this output formatter, do it in-process, but with classpath isolation.
     *
     * <p>This is the default behaviour.</p>
     */
    void useClassloaderIsolation();

    /**
     * Use process isolation when using this output formatter to perform conversions.
     */
    default void useProcessIsolation() {
        useProcessIsolation( x -> {} );
    }

    /**
     * Use process isolation when using this output formatter to perform conversions.
     *
     * @param forkOptions Reduced set of fork options.
     */
    void useProcessIsolation(Action<GrolifantSimpleSetJavaForkOptions> forkOptions);

    /**
     * Use process isolation when using this output formatter to perform conversions.
     *
     * @param forkOptions Reduced set of fork options.
     */
    void useProcessIsolation(@DelegatesTo(GrolifantSimpleSetJavaForkOptions.class) Closure<?> forkOptions);
}
