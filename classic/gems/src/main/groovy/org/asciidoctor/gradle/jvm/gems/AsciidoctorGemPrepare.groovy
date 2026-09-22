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
package org.asciidoctor.gradle.jvm.gems

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.ProblemReports
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.tasks.CacheableTask
import org.gradle.workers.WorkerExecutor
import org.ysb33r.gradle.jruby.api.tasks.AbstractGemPrepareTask

import javax.inject.Inject
import java.util.concurrent.Callable

import static org.asciidoctor.gradle.base.ProblemReports.ASCIIDOCTOR_J_PROBLEM_ID
import static org.asciidoctor.gradle.base.ProblemReports.TOOLCHAIN_J

/**
 * Prepare additional GEMs for AsciidoctorJ.
 *
 * @since 2.0
 */
@CacheableTask
@CompileStatic
class AsciidoctorGemPrepare extends AbstractGemPrepareTask {

//    private final Provider<File> jrubyJarLocationProvider
    private final AsciidoctorJExtension jruby

    @Inject
    @SuppressWarnings('UnnecessarySetter')
    AsciidoctorGemPrepare(WorkerExecutor we) {
        super(we)
        this.jruby = project.extensions.getByType(AsciidoctorJExtension)
        setJrubyJarProvider(project.provider({ AsciidoctorJExtension jruby ->
            jruby.configuration.files.find { it.name.startsWith(JRUBY_COMPLETE_NAME) }
        }.curry(jruby) as Callable<File>))

        ProblemReports.report(
            problemReporter(),
            ASCIIDOCTOR_J_PROBLEM_ID,
            ProblemReports.taskProblemDetail(name, 'asciidoctorj'),
            ProblemReports.replacePlugin(
                project, name,
                'jvm.gems.classic',
                'jvm.gems',
                TOOLCHAIN_J
            )
        )
    }
}
