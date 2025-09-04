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
package org.asciidoctor.gradle.model5.core.formatters;

import org.gradle.api.provider.Provider;

/**
 * Indicates that the backend can produce output without headers and footers.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface NoHeaderFooter {
    /**
     * Removes header and footer output.
     *
     * @param flag {@code true} will not add headers and footers to output.
     */
    void setNoHeaderFooter(boolean flag);

    /**
     * Header-footer conversion setting.
     *
     * @return Provider to the Header-footer setting.
     */
    Provider<Boolean> getNoHeaderFooter();
}
