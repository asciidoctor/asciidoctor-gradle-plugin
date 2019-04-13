/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.slides.export.deck2pdf

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.ModuleVersionLoader
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/** Extension to configure the deck2pdf ({@link https://github.com/melix/deck2pdf})
 * converter.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class Deck2PdfExtension {
    public final static String EXTENSION_NAME = 'deck2pdf'
    private final Project project

    /** Version of deck2pdf to use.
     *
     */
    String version

    /** Creates a deck2pdf extension.
     *
     * @param project Project this extension will be attached to.
     */
    @SuppressWarnings('DuplicateStringLiteral')
    Deck2PdfExtension(Project project) {
        this.project = project
        this.version = ModuleVersionLoader.load('slides2pdf')['deck2pdf']
    }

    /** Obtains a configuration that can load the correct artifact.
     *
     * @return Detached deck2pdf configuration.
     */
    Configuration getConfiguration() {
        project.configurations.detachedConfiguration(
                project.dependencies.create("me.champeau.deck2pdf:deck2pdf:${version}")
        )
    }
}
