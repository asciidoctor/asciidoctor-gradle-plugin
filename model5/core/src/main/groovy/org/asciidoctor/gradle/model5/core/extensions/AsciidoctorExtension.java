/**
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
package org.asciidoctor.gradle.model5.core.extensions;

import org.asciidoctor.gradle.model5.core.AsciidoctorRequires;
import org.asciidoctor.gradle.model5.core.HasDisplayType;
import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider;
import org.gradle.api.Named;
import org.gradle.api.file.FileCollection;

/**
 * Defines an Asciddoctor extension.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorExtension extends Named, AsciidoctorRequires, HasAttributeProvider, HasDisplayType {
    /**
     * Additional items to add to the classpath when a conversion involving the extension is executed.
     *
     * <p>
     *     The classpath is empty by default.
     * </p>
     *
     * @return Classpath. Can be {@code null} to indicate that the extension does not support additional classpath.
     */
    FileCollection getClasspath();
}
