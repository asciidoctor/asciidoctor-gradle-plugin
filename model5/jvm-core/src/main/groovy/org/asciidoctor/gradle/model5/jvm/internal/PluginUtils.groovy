/*
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
package org.asciidoctor.gradle.model5.jvm.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.errors.BadPluginException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

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
        final props = ConfigCacheSafeOperations.from(project).fsOperations().loadPropertiesFromResource(
                "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-jvm-core-plugin.properties",
                classLoader
        )
        if(props[entity] == null) {
            throw new BadPluginException("'${entity}' is missing from properties file.")
        }
        project.provider { -> props[entity].toString() }
    }
}
