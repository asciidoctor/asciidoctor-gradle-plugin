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
package org.asciidoctor.gradle.model5.core.pdfthemes;

import org.gradle.api.Named;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

/**
 * PDF theme declaration.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface PdfTheme extends Named {

    /**
     * Extract the theme from the theme collection.
     *
     * @param collection The theme collection.
     */
    void fromCollection(final Provider<PdfThemeCollection> collection);

    /**
     * Extract the theme from the named theme collection
     *
     * @param collectionName The name of the theme collection.
     */
    void fromCollection(final String collectionName);

    /**
     * Provider to where the theme is located.
     *
     * @return Provider to a {@link Directory}. If the provider is empty it indicates a built-in theme.
     */
    Provider<Directory> getThemeDir();

    /**
     * The theme name.
     *
     * <p>
     *     By default, this is the name under which the theme was registered, but the location may contain more than
     *     one theme, and then the name can be set.
     * </p>
     * @return
     */
    Provider<String> getThemeName();

    /**
     * Override the name of the theme as described by {@link #getThemeName()}.
     *
     * @param name Anything convertible to a string.
     */
    void setThemeName(Object name);
}
