/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.internal

import groovy.transform.CompileStatic
import org.ysb33r.grolifant.api.core.jvm.worker.WorkerAppParameterFactory
import org.ysb33r.grolifant.api.core.jvm.worker.WorkerExecSpec

import java.util.concurrent.Callable

/**
 * Creates worker parameters using Grolifant worker setup.
 *
 * @author Schalk W. Cronjé
 *
 * @since 4.0
 */
@CompileStatic
class AsciidoctorWorkerParameterFactory implements WorkerAppParameterFactory<AsciidoctorWorkerParameters> {

    private final Callable<Map<String,List<ExecutorConfiguration>>> populator

    AsciidoctorWorkerParameterFactory(
            Callable<Map<String,List<ExecutorConfiguration>>> populator
    ) {
        this.populator = populator
    }

    /**
     * @param execSpec Java execution specification that can be used for setting worker execution details.
     * @return A parameter configuration.
     */
    @Override
    AsciidoctorWorkerParameters createAndConfigure(WorkerExecSpec execSpec) {
        new AsciidoctorWorkerParameters(asciidoctorConfigurations: populator.call())
    }
}
