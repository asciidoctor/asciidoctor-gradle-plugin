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
package org.asciidoctor.gradle.model5.js.internal.utils

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.js.internal.PluginUtils
import org.gradle.api.Project
import org.gradle.api.provider.Provider

import javax.inject.Inject
import java.util.function.Consumer

/**
 * Utility class to add dependencies to a classpath.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DependencyUpdater {
    private final Project project

    @Inject
    DependencyUpdater(Project project) {
        this.project = project
    }

    void addFromFromDefault(String entityName, Consumer<Provider<String>> updater) {
        updater.accept(PluginUtils.loadDefaultVersion(entityName, project, this.class.classLoader))
    }
}
