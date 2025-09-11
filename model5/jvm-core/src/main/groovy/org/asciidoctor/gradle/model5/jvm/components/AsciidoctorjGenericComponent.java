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
package org.asciidoctor.gradle.model5.jvm.components;

import org.gradle.api.artifacts.ProjectDependency;

import java.util.Map;

/**
 * Methods for adding a generic component (backend or extension) to {@code asciidoctorj}
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorjGenericComponent {
    /**
     * If the backend is provided by a GEM, then set it here.
     *
     * <p>
     *     Requires {@code org.asciidoctor.jvm.gems} to be applied, otherwise an error will be raised.
     * </p>
     * <p>
     *     One of {@link #useGem(String, Object)} or {@link #useModule(String, Object)} is required.
     * </p>
     *
     * @param gemName Name of GEM
     * @param version Something convertible to a string that can provide the version.
     */
    void useGem(String gemName, Object version);

    /**
     * If the backend is provided by an artifact from a Maven or Ivy repository, then set it here.
     *
     * <p>
     *     One of {@link #useGem(String, Object)} or {@link #useModule(String, Object)} is required
     * </p>
     *
     * @param moduleName A module in the format {@code group:artifact}.
     * @param version Something convertible to a string that can provide the version.
     */
    void useModule(String moduleName, Object version);

    /**
     * If the backend is produced in the current hierarchy it can be used via a {@link ProjectDependency}.
     *
     * @param module Project module.
     */
    void useModule(ProjectDependency module);

    /**
     * If the backend needs one or more {@code require} entries to be passed to the engine, put it here.
     *
     * @param reqs One or more {@code require} entries
     */
    void requires(String... reqs);

    /**
     * Attributes that are specific to the backend.
     *
     * @param attrs Attributes of which the values are lazy-evaluated.
     */
    void attributes(Map<String,?> attrs);
}
