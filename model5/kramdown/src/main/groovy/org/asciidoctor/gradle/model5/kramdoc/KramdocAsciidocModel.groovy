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
package org.asciidoctor.gradle.model5.kramdoc

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils

/**
 * Utilities for working within a {@code kramdown-asciidoc}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class KramdocAsciidocModel {

    public static final String DEFAULT_PUBLICATION = PublicationUtils.DEFAULT_PUBLICATION
    public static final String TASK_PREFIX = 'kramdoc'

    /**
     * Name of a declarable configuration for use with a specific @code kramdown-asciidoc} engine.
     *
     * @param engineName Name of engine.
     * @return Configuration name
     */
    static String nameForEngineConfiguration(String engineName) {
        "kramdownAsciidocEngine${engineName.capitalize()}"
    }

    /**
     * Name of a resolvable configuration for use with a specific @code kramdown-asciidoc} engine.
     *
     * @param engineName Name of engine.
     * @return Configuration name
     */
    static String nameForGemsConfiguration(String engineName) {
        "${nameForEngineConfiguration(engineName)}Gems"
    }
}