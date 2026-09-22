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
package org.asciidoctor.gradle.model5.core.pdfthemes

/**
 * Built-in themes.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
enum BuiltInThemes {
    BASE('base'),
    DEFAULT('default'),
    DEFAULT_WITH_FONT_FALLBACKS('default-with-font-fallbacks'),
    DEFAULT_FOR_PRINT('default-for-print'),
    DEFAULT_SANS('default-sans'),
    DEFAULT_SANS_WITH_FONT_FALLBACKS('default-sans-with-font-fallbacks')

    final String themeName

    private BuiltInThemes(String val) {
        this.themeName = val
    }
}