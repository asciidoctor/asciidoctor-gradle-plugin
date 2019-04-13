/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.slides.export.deck2pdf

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.asciidoctor.gradle.base.slides.Profile
import org.gradle.api.Task
import org.gradle.api.provider.Provider

import static org.asciidoctor.gradle.base.slides.Profile.REVEAL_JS
import static org.asciidoctor.gradle.base.slides.Profile.RUBAN

/** Utility class for adding deck2pdf extensions to task classes.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class Deck2Pdf {
    public static final String EXTENSION_NAME = 'deck2pdf'

    /** Adds extension to object with a specific slides profile.
     *
     * @param task Task to add to
     * @param profile Slides profile.
     * @return Reference to the extension that was added. Can be {@code null} if no extension was added.
     */
    static Extension addExtensionTo(Task task, Profile profile) {
        switch (profile) {
            case REVEAL_JS:
                task.extensions.create(EXTENSION_NAME, RevealJsExtension, task)
                break
            case RUBAN:
                task.extensions.create(EXTENSION_NAME, RubanExtension, task)
                break
            default:
                null
        }
    }

//    /** Adds extension to object with a specific slides profile.
//     *
//     * @param task Task to add to
//     * @param profile Slides profile.
//     * @return Reference to the extension that was added.
//     */
//    static Extension addExtensionTo(TaskProvider task, Profile profile) {
//        switch (profile) {
//            case REVEAL_JS:
//                task.configure {extensions.create(EXTENSION_NAME, RevealJsExtension, task)
//                break
//            default:
//                task.extensions.create(EXTENSION_NAME, Extension, task)
//        }
//    }

    /** Base class for specific extensions.
     *
     */
    @ToString
    abstract static class Extension {
        private final Task task

        protected Extension(Task task) {
            this.task = task
        }

        Provider<Map<String, Object>> getParametersProvider() {
            task.project.providers.provider({ Extension deckExt ->
                deckExt.parameters
            }.curry(this)) as Provider<Map<String, Object>>
        }

        /** Parameters that were configured for the specific task.
         *
         * @return Map of parameters that are set.
         */
        abstract Map<String, ?> getParameters()
    }

    /** Extension for Reveal.js conversions
     *
     */
    @ToString(includeSuper = true)
    static class RevealJsExtension extends Extension {
        boolean skipFragments = false

        RevealJsExtension(Task task) {
            super(task)
        }

        @Override
        Map<String, ?> getParameters() {
            [skipFragments: (Boolean) skipFragments]
        }
    }

    /** Extension for Ruban conversions.
     *
     */
    @ToString(includeSuper = true)
    static class RubanExtension extends Extension {
        boolean skipSteps = false

        RubanExtension(Task task) {
            super(task)
        }

        @Override
        Map<String, ?> getParameters() {
            [skipSteps: (Boolean) skipSteps]
        }
    }

}
