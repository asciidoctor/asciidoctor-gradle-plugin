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
package org.asciidoctor.gradle.model5.core;

import org.gradle.api.provider.Provider;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A collection of scripts that can be used for extension.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface ScriptCollection {
    /**
     * A set of scripts.
     *
     * @return List of scripts.
     */
    Provider<Set<String>> getScripts();

    /**
     * A set of files containing scripts.
     *
     * @return Collection of files locations.
     */
    Provider<Set<File>> getScriptFiles();

    /**
     * The language of the script collection.
     *
     * @return The language used for the scripts.
     */
    String getScriptType();
}
