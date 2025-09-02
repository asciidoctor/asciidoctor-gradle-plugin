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
package org.asciidoctor.gradle.model5.core;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Provider;

import java.util.Set;

/**
 * Describes execution options for running a launcher.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorExecutionSettings {
    /**
     * Safe mode
     *
     * @return Execution safe mode. Cannot be null.
     */
    Provider<SafeMode> getSafeMode();

    /**
     * Some launchers have the concept of {@code require} for external libraries.
     *
     * @return Provider to a list. Can be empty, but not null.
     */
    Provider<Set<String>> getModuleRequires();

    /**
     * The name of the formatter.
     *
     * @return Provider to the name.
     */
    Provider<String> getFormatterName();

    /**
     * The name of the toolchain.
     *
     * @return Provider to the name.
     */
    Provider<String> getToolchainName();

    /**
     * Additional classpath to be added.
     *
     * @return Classpath. Never {@code null}
     */
    ConfigurableFileCollection getAdditionalClasspath();
}
