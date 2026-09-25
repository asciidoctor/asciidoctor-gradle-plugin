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
package org.asciidoctor.gradle.model5.jrubytools.tasks

import groovy.transform.CompileStatic
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.workers.WorkerExecutor
import org.ysb33r.gradle.jruby.api.tasks.AbstractGemPrepareTask

import javax.inject.Inject

/**
 * Prepare additional GEMs for AsciidoctorReducer.
 *
 * @since 5.1
 *
 * @author Schalk W. Cronjé
 */
@CacheableTask
@CompileStatic
class AsciidoctorGemPrepareTask extends AbstractGemPrepareTask {

    private final FileCollection jruby

    @Inject
    @SuppressWarnings('UnnecessarySetter')
    AsciidoctorGemPrepareTask(String jRubyConfiguration, WorkerExecutor we) {
        super(we)
        this.jruby = project.configurations.getByName(jRubyConfiguration)
        jrubyJarsProvider = project.provider { -> jruby.files }
    }
}
