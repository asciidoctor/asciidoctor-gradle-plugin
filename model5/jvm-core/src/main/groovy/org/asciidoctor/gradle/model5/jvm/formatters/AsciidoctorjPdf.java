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
package org.asciidoctor.gradle.model5.jvm.formatters;

/**
 * THe {@code asciidoctorj-pdf} output formatter.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorjPdf extends AsciidoctorjOutputFormatterVersioned {

    /**
     * Use the named theme from {@code asciidocPdfThemes}.
     *
     * @param name Name of theme
     */
    void useTheme(String name);

    /**
     * Supply an alternative location for fonts.
     *
     * @param dir Anything convertible to a file.
     */
    void setFontsDir(Object dir);
}
