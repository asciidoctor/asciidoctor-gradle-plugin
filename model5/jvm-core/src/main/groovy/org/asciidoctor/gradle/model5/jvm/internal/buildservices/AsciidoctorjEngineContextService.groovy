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
package org.asciidoctor.gradle.model5.jvm.internal.buildservices

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.internal.engines.DefaultExecutionContext
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * An internal service used by the {@code asciidoctorj} launcher to hold contextual execution information.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
abstract class AsciidoctorjEngineContextService implements BuildService<BuildServiceParameters.None> {
    private static final String LAUNCHER_SERVICE_NAME = 'org.asciidoctor.gradle.model5.jvm.internal.engines'
    private final ConcurrentMap<String, DefaultExecutionContext> executionsContexts

    /**
     * Easy way to get hold of a service.
     *
     * @param project Contextual project.
     * @return Provider to the service.
     */
    static Provider<AsciidoctorjEngineContextService> registerIfAbsent(Project project) {
        project.gradle.sharedServices.registerIfAbsent(LAUNCHER_SERVICE_NAME, AsciidoctorjEngineContextService) {
        }
    }

    AsciidoctorjEngineContextService() {
        this.executionsContexts = new ConcurrentHashMap<>()
    }

    /**
     * Stores execution context or updates an existing one.
     *
     * @param projectPath The full project path. For a root project this should be a single colon.
     * @param toolchainName Name of toolchain
     * @param formatterName Name of output formatter
     * @param context THe execution context.
     */
    void storeExecutionContext(
            String projectPath,
            String toolchainName,
            String formatterName,
            DefaultExecutionContext context
    ) {
        final index = key(projectPath, toolchainName, formatterName)
        executionsContexts.put(index, context)
        false
    }

    /**
     * Removes the execution context.
     *
     * @param projectPath The full project path. For a root project this should be a single colon.
     * @param toolchainName Name of toolchain
     * @param formatterName Name of output formatter
     */
    void removeExecutionContext(String projectPath, String toolchainName, String formatterName) {
        final index = key(projectPath, toolchainName, formatterName)
        executionsContexts.remove(index)
    }

    /**
     * Retrieves the execution context.
     *
     * @param projectPath The full project path. For a root project this should be a single colon.
     * @param toolchainName Name of toolchain
     * @param formatterName Name of output formatter
     *
     * @return Execution context. If nothing is found returns an empty {@link Optional}.
     */
    Optional<DefaultExecutionContext> getExecutionContext(String projectPath, String toolchainName, String formatterName) {
        final index = key(projectPath, toolchainName, formatterName)
        Optional.ofNullable(executionsContexts.get(index))
    }

    private String key(String projectPath, String toolchainName, String formatterName) {
        "${projectPath}:${toolchainName}:${formatterName}"
    }
}
