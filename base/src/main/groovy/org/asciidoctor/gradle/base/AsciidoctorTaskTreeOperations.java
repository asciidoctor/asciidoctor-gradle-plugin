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

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.util.PatternSet;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Additional operations on the source and destinations trees for Asciidoctor conversions.
 *
 * @authro Schalk W. Cronjé
 *
 * @since 4.0
 */
public interface AsciidoctorTaskTreeOperations {

    /**
     * Validates that the path roots are sane.
     *
     * @param baseDir Base directory strategy. Can be {@code null}
     */
    void checkForIncompatiblePathRoots(@Nullable BaseDirStrategy baseDir);

    /**
     * Validates the source tree
     */
    void checkForInvalidSourceDocuments() throws InvalidUserDataException;

    /**
     * The default pattern set for secondary sources.
     *
     * @return By default all *.adoc,*.ad,*.asc,*.asciidoc is included.
     */
    @Internal
    PatternSet getDefaultSecondarySourceDocumentPattern() throws InvalidUserDataException;

    /**
     * The default {@link PatternSet} that will be used if {@code sources} was never called
     *
     * @return By default all *.adoc,*.ad,*.asc,*.asciidoc is included.
     * Files beginning with underscore are excluded
     */
    @Internal
    PatternSet getDefaultSourceDocumentPattern();

    /**
     * Gets the language-specific secondary source tree.
     *
     * @param lang Language
     * @return Language-specific source tree.
     */
    @Internal
    FileTree getLanguageSecondarySourceFileTree(final String lang);

    /**
     * Gets the language-specific source tree
     *
     * @param lang Language
     * @return Language-specific source tree.
     */
    @Internal
    FileTree getLanguageSourceFileTree(final String lang);

    /**
     * Obtains a secondary source tree based on patterns.
     *
     * @param dir Toplevel source directory.
     * @return Source tree based upon configured pattern.
     */
    @Internal
    FileTree getSecondarySourceFileTreeFrom(File dir);

    /**
     * Obtains a source tree based on patterns.
     *
     * @param dir Toplevel source directory.
     * @return Source tree based upon configured pattern.
     */
    @Internal
    FileTree getSourceFileTreeFrom(File dir);
}
