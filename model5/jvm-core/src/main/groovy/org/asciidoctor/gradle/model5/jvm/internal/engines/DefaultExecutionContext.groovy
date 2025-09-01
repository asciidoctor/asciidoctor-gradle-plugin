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
package org.asciidoctor.gradle.model5.jvm.internal.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.engines.ExecutionContext
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.jvm.GrolifantSimpleJavaForkOptions

import javax.inject.Inject

/**
 * Execution context for an {@code asciidoctorj} launcher.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultExecutionContext implements ExecutionContext {

    @Delegate
    private final GrolifantSimpleJavaForkOptions forkOptions

    @Inject
    DefaultExecutionContext(Project project) {
        this.forkOptions = ConfigCacheSafeOperations.from(project).jvmTools().simpleJavaForkOptions()
    }
}
