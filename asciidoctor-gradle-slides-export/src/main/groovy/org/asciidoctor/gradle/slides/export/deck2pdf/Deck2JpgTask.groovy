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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

/** Runs a conversion from a slide deck to a collection of JPG images.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class Deck2JpgTask extends Deck2ImagesTask {

    private static final String QUALITY = 'quality'
    private Integer quality
    private Integer cmdlineQuality

    @Optional
    Integer getQuality() {
        this.cmdlineQuality ?: this.quality
    }

    void setQuality(Integer q) {
        this.quality = q
    }

    @Inject
    Deck2JpgTask(WorkerExecutor we) {
        super(we, 'jpg')
    }

    @Override
    @Internal
    protected List<String> getStandardParameters() {
        super.standardParameters + [QUALITY]
    }

    @SuppressWarnings('UnusedPrivateMethod')
    @Option(description = 'Slide export quality', option = 'quality')
    private void setQualityFromCommandLine(String q) {
        this.cmdlineQuality = q.toInteger()
    }

}

