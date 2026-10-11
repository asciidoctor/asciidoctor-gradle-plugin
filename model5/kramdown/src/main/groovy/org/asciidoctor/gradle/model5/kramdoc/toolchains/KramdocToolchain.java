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
package org.asciidoctor.gradle.model5.kramdoc.toolchains;

import org.asciidoctor.gradle.model5.kramdoc.KramdocLauncher;
import org.asciidoctor.gradle.model5.kramdoc.MarkdownFormat;
import org.asciidoctor.gradle.model5.kramdoc.formatters.KramdocOutputFormatter;
import org.asciidoctor.gradle.model5.jrubytools.toolchains.JRubyToolchainEngineOptions;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.provider.Provider;

import java.util.Collections;
import java.util.Locale;

/**
 * Represents a Kramdown toolchain.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
public interface KramdocToolchain extends Named, JRubyToolchainEngineOptions {


    /**
     * Something that can execute Asciidoctor conversions.
     *
     * @return Provider to a runnable Asciidoctor engine.
     */
    Provider<? extends KramdocLauncher> getLauncher();

    /**
     * Output formatters registered with this toolchain.
     *
     * @return Container of registered output formatters.
     */
    NamedDomainObjectContainer<KramdocOutputFormatter> getRegisteredOutputFormatters();

    /**
     * A list of tasks that will perform toolchain-related preparation before conversion using the toolchain can start.
     *
     * @return List of task names. Can be empty, but never {@code null}
     */
    default Iterable<String> getToolchainPreparationTaskNames() {
        return Collections.EMPTY_LIST;
    }

    /**
     * The format of Markdown the engine should parse.
     * @param fmt A case-insentive string being one of the formats defined by {@link MarkdownFormat}.
     */
    default void setFormat(String fmt) {
      setFormat( MarkdownFormat.valueOf(fmt.toUpperCase(Locale.US)));
    }

    /**
     * The format of Markdown the engine should parse.
     * @param fmt Either {@code GFM}, {@code KRAMDOWN}, or {@code MARKDOWN}.
     */
    void setFormat(MarkdownFormat fmt);

    /**
     * The format of Markdown the engine should parse.
     * @return Provider to the format of Markdown the engine should parse.
     */
    Provider<MarkdownFormat> getFormat();

    /**
     * Override the default version of {@code kramdown-asciidoc}.
     * @param ver Anything lazy-convertible to a string.
     */
    void useKramdownAsciidoc(Object ver);
}
