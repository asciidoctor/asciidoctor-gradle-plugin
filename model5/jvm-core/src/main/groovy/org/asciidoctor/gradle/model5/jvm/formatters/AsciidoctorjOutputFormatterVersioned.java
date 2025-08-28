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

import org.asciidoctor.gradle.model5.core.AsciidoctorOutputFormatter;
import org.gradle.api.file.FileCollection;

/**
 * Defines an output formatter that works on an {@code asciidoctorj} engine.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorjOutputFormatterVersioned extends AsciidoctorjOutputFormatter {

    /**
     * The component version.
     *
     * @param ver Anything convertible to a string with
     *            {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}.
     */
    void useVersion(Object ver);

    /**
     * Additional itemns to add to the classpath when it runs.
     *
     * @return Classpath.
     */
    FileCollection getClasspath();
}
