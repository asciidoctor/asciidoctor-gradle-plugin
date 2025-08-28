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
package org.asciidoctor.gradle.model5.jvm.internal.engines;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkParameters;

import java.io.File;

/**
 * Parameters for running AsciidoctorJ in a worker.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface LauncherParameters extends WorkParameters {

    /**
     * Source files.
     *
     * @return List of source files. Always present and will contain at least one source file.
     */
    SetProperty<File> getSourceFiles();

    /**
     * Classic AsciidoctorJ requires.
     *
     * @return List of required GEMs. Property is always defined, but the list could be empty.
     */
    ListProperty<String> getRequires();

    /**
     * Base directory.
     *
     * @return Directory. Always present.
     */
    DirectoryProperty getBaseDir();

    /**
     * Destination directory.
     *
     * @return Directory. Always present.
     */
    DirectoryProperty getDestinationDir();

    /**
     * The backend name.
     *
     * @return Backend name. Always present.
     */
    Property<String> getBackend();

    /**
     * Safe mode presented as a string.
     *
     * @return Safe mode. Always present.
     */
    Property<String> getSafeMode();

    /**
     * Document attributes.
     *
     * @return Attributes. Always present, but can be empty.
     */
    MapProperty<String, String> getAttributes();
}
