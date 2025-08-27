/**
 * Copyright ${year} the original author or authors.
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
package org.asciidoctor.gradle.model5.core.tasks;

import org.asciidoctor.gradle.model5.core.waitingroom.BaseDirStrategy;
import org.asciidoctor.gradle.model5.core.SafeMode;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;

import java.io.File;
import java.util.Map;

public interface AsciidoctorTaskMethods {

    /**
     * THe attributes the task will use.
     *
     * @param attrs Provider of attributes.
     */
    void setAttributes(Provider<Map<String, String>> attrs);

    /**
     * The {@link SafeMode} to use for processing.
     *
     * @param safeMode Safety mode.
     */
    void setSafeMode(Provider<SafeMode> safeMode);

    /**
     * Whether document names should be logged prior to processing.
     *
     * @param flag Provider of the setting.
     */
    void setLogDocuments(Provider<Boolean> flag);

    /**
     * Set the base directory, if it is required.
     *
     * @param dir Provider of directory.
     */
    void setBaseDirStrategy(Provider<BaseDirStrategy> dir);

    /**
     * The top directory where sources for this task will be located.
     *
     * @param dir Provider to the source directory.
     */
    void setSourceDir(Provider<File> dir);

    /**
     * The source patterns to look for in the source directory.
     *
     * @param patterns Provider of patterns
     */
    void setSourcePatterns(Provider<PatternFilterable> patterns);

    /**
     * The secondary source patterns to look for in the source directory.
     *
     * @param patterns Provider of patterns
     */
     void setSecondarySourcePatterns(Provider<PatternFilterable> patterns);
}
