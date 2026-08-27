/*
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
package org.asciidoctor.gradle.model5.js.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import static org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreBasePlugin.INTERMEDIATE_RESOURCE_PATH

/**
 * Utilities for use inside this set of {@code asciidoctorjs} plugins.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class PluginUtils {
    /**
     * Loads a given configuration entity as a lazy-evaluated value
     *
     * @param entity Entity to load
     * @param project Associated project
     * @param classLoader The class loader.
     * @return A provider to the value.
     */
    static Provider<String> loadDefaultVersion(String entity, Project project, ClassLoader classLoader) {
        loadDefaultVersion(entity, ConfigCacheSafeOperations.from(project), classLoader)
    }

    /**
     * Loads a given configuration entity as a lazy-evaluated value
     *
     * @param entity Entity to load
     * @param ccsp Instance of {@link ConfigCacheSafeOperations}.
     * @param classLoader The class loader.
     * @return A provider to the value.
     */
    static Provider<String> loadDefaultVersion(
        String entity,
        ConfigCacheSafeOperations configCacheSafeOperations,
        ClassLoader classLoader
    ) {
        final props = configCacheSafeOperations.fsOperations().loadPropertiesFromResource(
            "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-js-core-plugin.properties",
            classLoader
        )
        configCacheSafeOperations.providerTools().provider { -> props[entity].toString() }
    }
}
