/**
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
package org.asciidoctor.gradle.model5.core.formatters;

/**
 * Generic output formatter for adding backends that are not
 * directly supported by this plugin suite.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorGenericOutputFormatter extends AsciidoctorOutputFormatter{

    /**
     * Sets the name of the backend.
     *
     * @param backendName Name of backend.
     */
    void setBackend(String backendName);

    /**
     * Set whether resources should be copied.
     *
     * @param flag {@code true} to copy resources.
     */
    void setCopyResources(boolean flag);

    /**
     * Set a document type if the backend always requires a specific one.
     *
     * @param doctype Document type.
     */
    void setEnforcedDocType(String doctype);
}
