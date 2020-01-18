/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.base.internal.slides

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.slides.Profile

/** Utilities for dealing with Profile instances.
 *
 * @since 3.0
 */
@CompileStatic
class ProfileUtils {

    /** Tries to find a profile that matches the given string.
     *
     * It will perform a case-insensitive search in the following order:
     * <ol>
     *   <li>Profile short name</li>
     *   <li>Profile alternative name (decktape)<li>
     *   <li>Profile name/li>
     *   <li>Profile instance</li>
     * </ol>
     *
     * @param search search string
     * @return matched profile
     * @throw
     */
    static Profile findMatch(final String search) {
        String lc = search.toLowerCase(Locale.US)
        Profile profile = Profile.values().find {
            it.profileShortName.toLowerCase(Locale.US) == lc || it.name.toLowerCase(Locale.US) == lc ||
                    it.deckTapeShortName?.toLowerCase(Locale.US) == lc
        }

        profile ?: Profile.valueOf(search.toUpperCase(Locale.US))
    }
}
