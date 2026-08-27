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
package org.asciidoctor.gradle.model5.core.basedir;

import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

/**
 * Strategy to set where the base directory should be relative to
 * a project.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface BaseDirStrategy {
    /**
     * Base directory location.
     *
     * @param srcDir Location of asciidoc sources
     * @return Base directory
     */
    Provider<Directory> getBaseDir(Provider<Directory> srcDir);

    /**
     * Base directory location for a specific language.
     *
     * @param srcDir Location of asciidoc sources
     * @param lang   Source language
     * @return Base directory
     */
    Provider<Directory> getBaseDir(Provider<Directory> srcDir, String lang);

    /**
     * Whether to adjust the base directory per file.
     *
     * @return A provider to the mode.
     */
    Provider<Boolean> getAdjustBaseDirPerFile();
}
