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

import org.ysb33r.grolifant.api.remote.worker.SerializableWorkerAppParameters

/**
 * Parameters for serializing Asciidoctor jobs to workers.
 *
 * @author Schalk W> Cronjé
 *
 * @since 4.0
 */
class AsciidoctorWorkerParameters implements SerializableWorkerAppParameters {
    private static final long serialVersionUID = 1251694026305095019

    /**
     * Whether to attempt conversions in parallel inside the worker.
     *
     * Reserved for future use.
     */
    Boolean runParallelInWorker = false

    /**
     * Map of executor configuration keyed by language.
     * If there are no languages defined, the key of only entry will be an empty string.
     */
    Map<String,List<ExecutorConfiguration>> asciidoctorConfigurations
}
