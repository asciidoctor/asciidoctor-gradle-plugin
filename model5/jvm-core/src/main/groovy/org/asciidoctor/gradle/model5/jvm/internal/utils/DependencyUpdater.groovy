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
package org.asciidoctor.gradle.model5.jvm.internal.utils

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 * Utility class to add dependencies to a classpath.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DependencyUpdater {
    private final DependencyHandler dependencies
    private final ConfigurationContainer configurations

    @Inject
    DependencyUpdater(Project project) {
        this.dependencies = project.dependencies
        this.configurations = project.configurations
    }

    /**
     * Adds a dependency to the given configuration.
     *
     * @param cfgName Name of configuration.
     * @param dep A project dependency.
     */
    void add(String cfgName, ProjectDependency dep) {
        dependencies.add(cfgName, dep)
    }

    /**
     * Adds a dependency to the given configuration.
     *
     * @param cfgName Name of configuration.
     * @param dep Provider to a string in the standard Maven coordinate format.
     */
    void add(String cfgName, Provider<String> dep) {
        dependencies.addProvider(cfgName, dep)
    }

    /**
     *  Adds a dependency to the given configuration.
     *
     * @param cfgName Name of configuration.
     * @param moduleName Name of module in the format {@code "${groupName}:${artifactName}"}.
     * @param version Provider to a version.
     */
    void add(String cfgName, String moduleName, Provider<String> version) {
        dependencies.addProvider(cfgName, version.map { "${moduleName}:${it}" })
    }

    /**
     * Make one configuration extends from another.
     *
     * @param targetCfgName Target configuration. (Will extend from source configuration).
     * @param srcCfgName Source configuration.
     */
    void extendsFrom(String targetCfgName, String srcCfgName) {
        configurations.getByName(targetCfgName).extendsFrom(configurations.getByName(srcCfgName))
    }
}
