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

import org.asciidoctor.gradle.base.internal.Workspace;

import java.util.Optional;

/**
 * Methods for preparing an Asciidoctor worksapce prior to conversion.
 *
 * @author Schalk W> Cronjé
 * @since 4.0
 */
public interface AsciidoctorTaskWorkspacePreparation {
    /**
     * Prepares a workspace prior to conversion.
     *
     * @return A presentation of the working source directory and the source tree.
     */
    Workspace prepareWorkspace();

    /**
     * Prepares a workspace for a specific language prior to conversion.
     *
     * @param language Language to prepare workspace for.
     * @return A presentation of the working source directory and the source tree.
     */
    Workspace prepareWorkspace(String language);


    /**
     * Prepares a workspace for a specific language prior to conversion.
     * If the language is not present, it behaves like {@link #prepareWorkspace()}.
     *
     * @param language Language to prepare workspace for.
     * @return A presentation of the working source directory and the source tree.
     */
    default Workspace prepareWorkspace(Optional<String> language) {
        if(language.isPresent()) {
            return prepareWorkspace(language.get());
        } else {
            return prepareWorkspace();
        }
    }
}
