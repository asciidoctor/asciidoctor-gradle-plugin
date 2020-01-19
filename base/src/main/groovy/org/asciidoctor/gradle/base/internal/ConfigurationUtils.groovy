/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.base.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.Transform
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant.api.StringUtils

/** Utilities for dealing with Configurations
 *
 * @since 3.0.0
 */
@CompileStatic
class ConfigurationUtils {

    /** Extracts a {@link Configuration} if possible.
     *
     * @param project Associated project
     * @param sourceConfig Item that could be a source of a configuration
     * @return {@link Configuration} instance
     *
     * @throw {@code UnknownConfigurationException}
     */
    static Configuration asConfiguration(Project project, Object sourceConfig) {
        switch (sourceConfig.class) {
            case Configuration:
                return (Configuration) sourceConfig
            case Provider:
                return asConfiguration(project, ((Provider) sourceConfig).get())
            default:
                project.configurations.getByName(StringUtils.stringize(sourceConfig))
        }
    }

    /** Extracts a list of {@link Configuration} instances.
     *
     * @param project Associated project.
     * @param sourceConfigs Collection of items that could be sources of configurations.
     * @return List of {@link Configuration} instances.
     *
     * @throw {@code UnknownConfigurationException}
     */
    static List<Configuration> asConfigurations(Project project, Collection<Object> sourceConfigs) {
        Transform.toList(sourceConfigs) {
            asConfiguration(project, it)
        }
    }
}
