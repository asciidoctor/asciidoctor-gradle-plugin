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
package org.asciidoctor.gradle.model5.jvm.toolchains;

import org.asciidoctor.gradle.model5.core.toolchains.AsciidoctorToolchain;
import org.gradle.api.file.FileCollection;

/**
 * A toolchain for running asciidoctorj.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorjToolchain extends AsciidoctorToolchain, CoreVersions {

    /**
     * Adds additional items to the runtime classpath.
     *
     * @param fc Files to add to classpath. Can be a {@code Configuration}.
     */
    void classpath(FileCollection fc);

//    /**
//     * The level at which the AsciidoctorJ process should be logging.
//     *
//     * @return The currently configured log level. By default, this is {@code project.logging.level}.
//     */
//    LogLevel getLogLevel();
//
//    /**
//     * Set the level at which the AsciidoctorJ process should be logging.
//     *
//     * @param logLevel LogLevel to use
//     */
//    void setLogLevel(LogLevel logLevel);
//
//    /**
//     * Set the level at which the AsciidoctorJ process should be logging.
//     *
//     * @param logLevel LogLevel to use
//     */
//    void setLogLevel(String logLevel);
//
//        /* -------------------------
//       tag::extension-property[]
//        options:: Options for running the AsciidoctorJ engine.
//   ------------------------- */
//    /** Returns the Asciidoctor options.
//     *
//     * @return Resolved options.
//     */
//    Provider<Map<String, String>> getOptions();
//
//    /**
//     * Apply a new set of Asciidoctor options, clearing any options previously set.
//     *
//     * @param m Map with new options
//     */
//    void setOptions(Map<String,?> m);
//
//    /**
//     * Add additional asciidoctorj options
//     *
//     * @param m Map with new options
//     */
//    void options(Map<String,?> m);
}
