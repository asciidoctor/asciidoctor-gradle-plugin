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
// tag::hacking-asciidoctorjs-output-formatter[]
package org.asciidoctor.gradle.model5.js.formatters;

// end::hacking-asciidoctorjs-output-formatter[]
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.asciidoctor.gradle.model5.core.revealjs.RevealjsOptions;
import org.gradle.api.Action;
import org.ysb33r.grolifant5.api.core.ClosureUtils;

/**
 * Reveal.js generated from AsciiDoc.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
// tag::hacking-asciidoctorjs-output-formatter[]
public interface AsciidoctorjsRevealjs extends AsciidoctorjsOutputFormatterVersioned, HasAsciidoctorjsTemplates { // <.>
    // end::hacking-asciidoctorjs-output-formatter[]
    /**
     * Direct access to configuring {@code reveal.js} options.
     *
     * @return Access to an instance of {@link RevealjsOptions}
     */
    // tag::hacking-asciidoctorjs-output-formatter[]
    RevealjsOptions getRevealjsOptions();
    // end::hacking-asciidoctorjs-output-formatter[]

    /**
     * Configures an instance of {@link RevealjsOptions}
     *
     * @param configurator Configurator.
     */
    // tag::hacking-asciidoctorjs-output-formatter[]
    default void revealjsOptions(Action<RevealjsOptions> configurator) {
        configurator.execute(getRevealjsOptions());
    }
    // end::hacking-asciidoctorjs-output-formatter[]

    /**
     * Configures an instance of {@link RevealjsOptions}
     *
     * @param configurator Configurator.
     */
    // tag::hacking-asciidoctorjs-output-formatter[]
    default void revealjsOptions(@DelegatesTo(RevealjsOptions.class) Closure<?> configurator) {
        ClosureUtils.configureItem(getRevealjsOptions(), configurator);
    }
    // end::hacking-asciidoctorjs-output-formatter[]

// tag::hacking-asciidoctorjs-output-formatter[]
}
// end::hacking-asciidoctorjs-output-formatter[]
