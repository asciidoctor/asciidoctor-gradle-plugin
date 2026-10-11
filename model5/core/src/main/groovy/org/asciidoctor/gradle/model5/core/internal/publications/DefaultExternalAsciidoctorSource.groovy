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
package org.asciidoctor.gradle.model5.core.internal.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.tasks.TaskFactory
import org.asciidoctor.gradle.model5.core.publications.ExternalAsciidoctorSource
import org.gradle.api.Project

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.conversionTaskName

/**
 * Implementation of {@link ExternalAsciidoctorSource}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultExternalAsciidoctorSource extends DefaultExternalSource implements ExternalAsciidoctorSource {

    @Inject
    DefaultExternalAsciidoctorSource(Project project) {
        super(project)
    }

    @Override
    void fromPublication(String publicationName, String formatterOrAliasName) {
        final sourceTask = ccso.providerTools().newInstance(TaskFactory)
            .lookupTask(conversionTaskName(publicationName, formatterOrAliasName))
        this.allExternalSources.from(sourceTask.map { it.outputs.files })
        builtBy(sourceTask)
    }
}
