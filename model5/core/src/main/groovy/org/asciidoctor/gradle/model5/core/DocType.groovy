/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic

/**
 * Document type.
 *
 * <p>
 *     See <a href="https://docs.asciidoctor.org/asciidoc/latest/document/doctype/">Document Type</a>
 *     in the Asciidoctor documentation.
 *     </p>
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
enum DocType {
    /**
     * The default that is assumed by an Asciidoctor engine if nothing is supplied either from the Gradle plugin or
     * in the document itself.
     */
    ARTICLE,

    /**
     * Asciidoctor book structure.
     */
    BOOK,

    /**
     * For {@code roff} or HTML-formatted man paged.
     */
    MANPAGE,

    /**
     * Asciidoctor snippets.
     */
    INLINE

    /**
     * Provide a case-insensitive string and get a document type.
     *
     * @param val Case-insensitive string.
     *
     * @return {@link DocType}
     */
    static DocType from(String val) {
        valueOf(val.replaceAll(~/-/, '_').toUpperCase(Locale.US))
    }

    String lc() {
        name().toLowerCase(Locale.US)
    }
}
