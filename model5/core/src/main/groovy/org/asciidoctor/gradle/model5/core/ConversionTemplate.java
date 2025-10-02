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
package org.asciidoctor.gradle.model5.core;

import org.gradle.api.file.Directory;

import java.util.Collection;
import java.util.List;

/**
 * Describes conversion template details.
 *
 * <p>
 *     Most output formatters do not have template support, and in most cases the providers will be empty.
 *     For support output formatters, the providers will contain values if templates were configured.
 * </p>
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface ConversionTemplate {

    /**
     * List of template directories
     *
     * @return List of directories. Can be empty, but never null.
     */
    Collection<Directory> getTemplateDirs();

    /**
     * List of template engines.
     *
     * <p>
     *     Where engines are used, the majority of cases this list will only contain one.
     * </p>
     *
     * @return Engines. Can be empty, but never null.
     */
    List<String> getTemplateEngines();
}
