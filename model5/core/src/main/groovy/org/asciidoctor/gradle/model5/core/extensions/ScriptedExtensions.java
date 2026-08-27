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
package org.asciidoctor.gradle.model5.core.extensions;

import org.asciidoctor.gradle.model5.core.ScriptCollection;
import org.gradle.api.provider.Provider;

import java.util.Map;

/**
 * An interface for providing scripted extensions.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface ScriptedExtensions {
    /**
     * Clears all registered scripts in the extensions.
     */
    void clearScripts();

    /**
     * Adds an extension from string content.
     *
     * @param ext Anything that can be lazy-evaluated to a string
     */
    void fromString(Object ext);

    /**
     * Adds an extension from content in a file.
     *
     * @param ext Anything that can be lazy-evaluated to a file
     */
    void fromFile(Object ext);

    /**
     * Returns a list of scripted extensions.
     *
     * @return Not {@code null}, but can be empty.
     */
    ScriptCollection getScriptedExtensions();
}
