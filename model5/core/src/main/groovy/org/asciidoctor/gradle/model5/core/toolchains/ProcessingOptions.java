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
package org.asciidoctor.gradle.model5.core.toolchains;

import org.asciidoctor.gradle.model5.core.SafeMode;
import org.gradle.api.provider.Provider;

/**
 * Definitions of publication processing options.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
public interface ProcessingOptions {
   /* -------------------------
   tag::extension-property[]
   safeMode:: Asciidoctor safe mode.
     Set the Safe mode as either `UNSAFE`, `SAFE`, `SERVER`, `SECURE`.
     Can be a number (0, 1, 10, 20), a string, or the entity name
   end::extension-property[]
   ------------------------- */

    /**
     * Returns the Asciidoctor SafeMode under which a conversion will be run.
     *
     * @return Asciidoctor Safe Mode
     */
    Provider<SafeMode> getSafeMode();

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode An instance of Asciidoctor SafeMode.
     */
    void setSafeMode(SafeMode mode);

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A valid integer representing a Safe Mode
     */
    void setSafeMode(int mode);

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A valid string representing a Safe Mode
     */
    void setSafeMode(String mode);

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A provider representing a Safe Mode
     */
    void setSafeMode(Provider<SafeMode> mode);

    /**
     * Whether documents should be logged as they are processed.
     *
     * @param logDocuments {@code true} when documents should be logged.
     */
    void setLogDocuments(boolean logDocuments);

    /**
     * Whether document should be logged
     *
     * @return {@code true} when documents should be logged.
     */
    Provider<Boolean> isLogDocuments();
}
