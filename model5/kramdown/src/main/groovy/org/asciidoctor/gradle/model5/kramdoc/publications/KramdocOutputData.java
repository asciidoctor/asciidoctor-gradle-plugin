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
package org.asciidoctor.gradle.model5.kramdoc.publications;

import org.asciidoctor.gradle.model5.kramdoc.MarkdownFormat;
import org.asciidoctor.gradle.model5.core.publications.BaseOutputData;
import org.gradle.api.provider.Provider;

/**
 * Holds data regarding a publication output.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface KramdocOutputData extends BaseOutputData {
    /**
     * The expected Markdown format the source files will be in.
     * @return Provider to the format of Markdown the engine should parse.
     */
    Provider<MarkdownFormat> getSourceFormat();
}
