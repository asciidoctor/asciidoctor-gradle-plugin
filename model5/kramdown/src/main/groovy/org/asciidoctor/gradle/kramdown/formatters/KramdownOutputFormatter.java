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
package org.asciidoctor.gradle.kramdown.formatters;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.asciidoctor.gradle.kramdown.KramdownWrapMode;
import org.asciidoctor.gradle.model5.core.CanConfigureTaskInputs;
import org.asciidoctor.gradle.model5.core.attributes.HasAsciidoctorAttributes;
import org.asciidoctor.gradle.model5.core.formatters.HasCustomProperties;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.ysb33r.grolifant5.api.core.ClosureUtils;

import java.util.Locale;

/**
 * THe Kramdown output formatter.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface KramdownOutputFormatter extends Named, HasAsciidoctorAttributes, CanConfigureTaskInputs,
        HasCustomProperties {
    /**
     * Whether resource files should be copied to the output directory.
     *
     * @return {@code true} if resources should be copied.
     */
    boolean getCopyResources();

    /**
     * Set the source languages to interpret as diagrams.
     * If not set, the defaults are {@code plantuml} and {@code mermaid}.
     *
     * @param langs List fo source languages
     */
    void setDiagramLanguages(Iterable<String> langs);

    /**
     * Add more the source languages to interpret as diagrams.
     *
     * @param langs List fo source languages
     */
    void diagramLanguages(String... langs);

    /**
     * Add more the source languages to interpret as diagrams.
     *
     * @param langs List fo source languages
     */
    void diagramLanguages(Iterable<String> langs);

    /**
     * Set whether to passthrough HTML or convert it to AsciiDoc syntax where possible
     * @param flag {@code true} is the default as will tryu to convert where possible.
     */
    void setPassthruHtml(boolean flag);

    /**
     * Set the wrapping mode for the output.
     * @param wrap {@link KramdownWrapMode} case-insensitive string equivalent.
     */
    default void setWrap(String wrap) {
        setWrap(KramdownWrapMode.valueOf(wrap.toUpperCase(Locale.US)));
    }

    /**
     * Set how lines are wrapped in the AsciiDoc document
     * @param wrap A value of {@link KramdownWrapMode}.
     */
    void setWrap(KramdownWrapMode wrap);

    /**
     * Set the heading offset for the output.
     * @param offset The offset to apply to the heading levels.
     */
    void setHeadingOffset(int offset);

    /**
     * Set whether to automatically convert bare URLs into links.
     * @param flag {@code true} is the default and will perform conversion.
     */
    void setAutoLinks(boolean flag);

    /**
     * Configures AutoID generation for this formatter.
     *
     * @param configurator Configurator
     */
    default void autoIds(Action<AutoID> configurator) {
        configurator.execute(getAutoIds());
    }

    /**
     *Configures AutoID generation for this formatter.
     *
     * @param configurator Configurator
     */
    default void autoIds(@DelegatesTo(AutoID.class) Closure configurator) {
        ClosureUtils.configureItem(getAutoIds(), configurator);
    }

    /**
     * Direct access to the AutoID generation for this formatter.
     *
     * @return INstance of {@link AutoID}
     */
    AutoID getAutoIds();
}
