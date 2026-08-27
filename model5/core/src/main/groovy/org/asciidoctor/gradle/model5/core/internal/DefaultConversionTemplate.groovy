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
package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.ConversionTemplate
import org.gradle.api.file.Directory

/**
 * Implementation of conversion templates.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultConversionTemplate implements ConversionTemplate {
    final List<Directory> templateDirs
    final List<String> templateEngines

    DefaultConversionTemplate(Collection<Directory> dirs, Collection<String> engines) {
        this.templateDirs = dirs.toList().asImmutable()
        this.templateEngines = engines.toList().asImmutable()
    }
}
