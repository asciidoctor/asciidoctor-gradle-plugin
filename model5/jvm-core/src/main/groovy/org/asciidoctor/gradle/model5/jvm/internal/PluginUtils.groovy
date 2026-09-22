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
package org.asciidoctor.gradle.model5.jvm.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.errors.BadPluginException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.FileSystemOperations
import org.ysb33r.grolifant5.api.core.ProviderTools

import static org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreBasePlugin.INTERMEDIATE_RESOURCE_PATH

/**
 * Utilities for use inside this set of {@code asciidoctorj} plugins.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class PluginUtils {
    static Provider<String> loadDefaultVersion(String entity, Project project, ClassLoader classLoader) {
        final ccso = ConfigCacheSafeOperations.from(project)
        loadDefaultVersion(entity, ccso.fsOperations(), ccso.providerTools(), classLoader)
    }

    static Provider<String> loadDefaultVersion(
        String entity,
        FileSystemOperations fsOperations,
        ProviderTools providerTools,
        ClassLoader classLoader
    ) {
        final props = fsOperations.loadPropertiesFromResource(
            "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-jvm-core-plugin.properties",
            classLoader
        )
        if (props[entity] == null) {
            throw new BadPluginException("'${entity}' is missing from properties file.")
        }
        providerTools.provider { -> props[entity].toString() }
    }
}
