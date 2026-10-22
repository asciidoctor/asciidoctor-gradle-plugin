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
package org.asciidoctor.gradle.model5.kramdoc.internal.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.publications.BaseSourceDefinition
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Helper class to implement definition of AsciiDoc sources.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultKramdocSource extends BaseSourceDefinition {
    @Inject
    DefaultKramdocSource(Project project) {
        super(project, [], ['**/*.md'])
    }
}
