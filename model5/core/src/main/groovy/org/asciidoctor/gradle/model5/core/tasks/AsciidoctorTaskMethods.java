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
package org.asciidoctor.gradle.model5.core.tasks;

import org.asciidoctor.gradle.model5.core.*;
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorOutputData;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;

import java.util.Map;

public interface AsciidoctorTaskMethods {

    /**
     * Sets the conversion launcher for this task.
     *
     * @param launcher Provider to launcer.
     */
    void setLauncher(Provider<? extends AsciidoctorLauncher> launcher);

    /**
     * Configures the task according to a toolchain & output formatter combination.
     *
     * @param outputData Output data.
     */
    void setOutputData(final AsciidoctorOutputData outputData);

    /**
     * The safety mode the specific task will run conversions under.
     *
     * @param safeMode Provider of the safety mode.
     */
    void setSafeMode(Provider<SafeMode> safeMode);

    /**
     * Sets the base directory for conversions.
     *
     * @param dir Provider to a directory.
     */
    void setBaseDir(Provider<Directory> dir);

    /**
     * Sets the source directory for actual sources.
     *
     * @param dir Provider to a directory.
     */
    void setSourceDir(Provider<Directory> dir);

    /**
     * The source patterns to look for in the source directory.
     *
     * @param patterns Provider of patterns
     */
    void setSourcePatterns(Provider<PatternFilterable> patterns);

    /**
     * The attributes the task will use.
     *
     * @param attrs Provider of attributes.
     */
    void setAttributes(Provider<Map<String,String>> attrs);

//
//    /**
//     * Whether document names should be logged prior to processing.
//     *
//     * @param flag Provider of the setting.
//     */
//    void setLogDocuments(Provider<Boolean> flag);
//
//    /**
//     * The top directory where sources for this task will be located.
//     *
//     * @param dir Provider to the source directory.
//     */
//    void setSourceDir(Provider<File> dir);
//

//
//    /**
//     * The secondary source patterns to look for in the source directory.
//     *
//     * @param patterns Provider of patterns
//     */
//     void setSecondarySourcePatterns(Provider<PatternFilterable> patterns);
}
