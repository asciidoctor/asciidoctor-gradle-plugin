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
package org.asciidoctor.gradle.model5.core.attributes;

import org.gradle.api.provider.Provider;

/**
 * Manages the use of include directories.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface IncludeDir {
    /**
     * Whether a soft attribute entry should be used.
     *
     * @param flag {@code true} uses {@code includedir@}
     */
    void setSoft(boolean flag);

    /**
     * Whether a soft attribute entry should be used.
     *
     * @param flag {@code true} uses {@code includedir@}
     */
    void setSoft(Provider<Boolean> flag);

    /**
     * Sets the include directory path.
     * A relative path will always be resolved relative to a base directory.
     * Care should be taken with absolute apths if external sources are present.
     *
     * <p>Overrides {@link #withSourceDir()}</p>
     *
     * @param relOrAbsPath Anything that lazy-evaluates to a string.
     */
    void withFixedPath(Object relOrAbsPath);

    /**
     * Instead of supplying a path, make the include directory be the main source directory or in case of an
     * intermediate working directory, the working directory.
     *
     * <p>Overrides {@link #withFixedPath(Object)}</p>
     */
    void withSourceDir();
}
