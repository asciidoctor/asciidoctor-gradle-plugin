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
package org.asciidoctor.gradle.model5.kramdoc.tasks;

import org.asciidoctor.gradle.model5.kramdoc.KramdocLauncher;
import org.asciidoctor.gradle.model5.kramdoc.publications.KramdocOutputData;
import org.asciidoctor.gradle.model5.core.tasks.ConversionTaskBaseMethods;
import org.gradle.api.provider.Provider;

public interface KramdocTaskMethods extends ConversionTaskBaseMethods {
    /**
     * Sets the conversion launcher for this task.
     *
     * @param launcher Provider to launcher.
     */
    void setLauncher(Provider<? extends KramdocLauncher> launcher);

    /**
     * Configures the task according to a toolchain and output formatter combination.
     *
     * @param outputData Output data.
     */
    void setOutputData(final KramdocOutputData outputData);
}
