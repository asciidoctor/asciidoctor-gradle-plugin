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
package org.asciidoctor.gradle.model5.core.pdfthemes;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.ysb33r.grolifant5.api.core.git.GitHubArchive;

/**
 * A PDF theme hosted on Github.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface GithubThemeCollection extends PdfThemeCollection {
    /**
     * The relative path to the themes directory inside the downloaded package.
     *
     * @param pathInRepo Relative path. Anything convertible to a string.
     */
    void setPathInRepo(Object pathInRepo);

    /**
     * Configure a location to download the theme from.
     *
     * @param configurator Configurator.
     */
    void from(Action<GitHubArchive> configurator);

    /**
     * Configure a location to download the theme from.
     *
     * @param configurator Configurator.
     */
    void from(@DelegatesTo( GitHubArchive.class) Closure<?> configurator);
}
