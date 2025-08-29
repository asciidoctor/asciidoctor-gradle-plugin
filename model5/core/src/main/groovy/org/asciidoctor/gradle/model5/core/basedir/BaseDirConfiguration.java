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
package org.asciidoctor.gradle.model5.core.basedir;

import org.gradle.api.provider.Provider;

/**
 * Asciidoctor base directories need special care, and those methods are specified by this interface.

 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface BaseDirConfiguration {

    /**
     * The base dir will be the same as the source directory.
     * <p>
     * If an intermediate working directory is used, the base dir will be where the
     * source directory is located within the temporary working directory.
     * </p>
     */
    void baseDirFollowsSourceDir();

    /**
     * Sets the basedir to be the same directory as the current project directory.
     */
    void baseDirIsProjectDir();

    /**
     * Sets the basedir to be the same directory as the root project directory.
     */
    void baseDirIsRootProjectDir();

    /**
     * Returns the current basedir strategy if it has been configured.
     *
     * @return Strategy or empty provider.
     */
    Provider<BaseDirStrategy> getBaseDirStrategy();

    /**
     * Sets the base directory for a conversion.
     *
     * @param f Base directory
     */
    void setBaseDir(Object f);
}
