/*
 * Copyright ${year} the original author or authors.
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
package org.asciidoctor.gradle.model5.core.internal.basedir

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.waitingroom.BaseDirStrategy
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Strategy where base directory is null so that input file's parent directory gets used as base_dir
 * in asciidoctor conversion
 *
 * @author Lari Hotari
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class BaseDirIsNull extends BaseDirIsFixedPath implements BaseDirStrategy  {
    @Inject
    BaseDirIsNull(Project project) {
        super( project.provider { -> (File) null })
    }
}
