/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.base;

import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Asciidoctor base directories need special care and those methods are specified by this interface.
 *
 * <p>
 *     Getters in this interface are all annotated with {@code Internal} as they should be participate in
 *     task properties for up to date checks.
 * </p>
 * @author Schalk W. Cronjé
 *
 * @since 4.0
 */
public interface AsciidoctorTaskBaseDirConfiguration {

    /**
     * The base dir will be the same as the source directory.
     * <p>
     * If an intermediate working directory is used, the the base dir will be where the
     * source directory is located within the temporary working directory.
     * </p>
     */
    void baseDirFollowsSourceDir();

    /**
     * Sets the basedir to be the same directory as each individual source file.
     */
    void baseDirFollowsSourceFile();

    /**
     * Sets the basedir to be the same directory as the current project directory.
     *
     * @since 2.2.0
     */
    void baseDirIsProjectDir();

    /**
     * Sets the basedir to be the same directory as the root project directory.
     */
    void baseDirIsRootProjectDir();

    /** Base directory (current working directory) for a conversion.
     *
     * @return Base directory. Can be {@code null}
     */
    @Internal
    @Nullable
    default File getBaseDir() {
        return getBaseDirProvider().getOrNull();
    }

    /**
     * Base directory (current working directory) for a conversion.
     *
     * Depending on the strateggy in use, the source language used in the conversion
     * may change the final base directory relative to the value returned by {@link #getBaseDir}.
     *
     * @param lang Language in use
     * @return Language-dependent base directory
     */
    File getBaseDir(String lang);

    @Internal
    Provider<File> getBaseDirProvider();

    /**
     * Returns the current basedir strategy if it has been configured.
     *
     * @return Strategy or @{code null}.
     */
    @Nullable
    @Internal
    BaseDirStrategy getBaseDirStrategy();

    /**
     * Checks whether an explicit strategy has been set for base directory.
     *
     * @return {@code true} if a strategy has been configured.
     */
    @Internal
    boolean isBaseDirConfigured();

    /**
     * Sets the base directory for a conversion.
     * <p>
     * The base directory is used by AsciidoctorJ to set a current working directory for
     * a conversion. If never set, then {@code project.projectDir} will be assumed to be the base directory.
     * </p>
     * @param f Base directory
     */
    void setBaseDir(Object f);
}
