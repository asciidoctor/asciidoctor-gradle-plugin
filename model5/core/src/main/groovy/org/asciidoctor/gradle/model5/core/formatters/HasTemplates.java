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
package org.asciidoctor.gradle.model5.core.formatters;

import org.asciidoctor.gradle.model5.core.ConversionTemplate;
import org.gradle.api.provider.Provider;

import java.util.Collection;

/**
 * Declares that the output formatter supports templates.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface HasTemplates {
    /**
     * One of more directories that serve as template directories
     *
     * @param dirs Anything recursively convertible to a list of files.
     */
    void templateDirs(Object... dirs);

    /**
     * One of more directories that serve as template directories
     *
     * @param dirs Anything recursively convertible to a list of files.
     */
    void templateDirs(Collection<Object> dirs);

    /**
     * Configured templates.
     *
     * @return Information about configured templates. Probably empty.
     */
    Provider<ConversionTemplate> getConversionTemplate();
}
