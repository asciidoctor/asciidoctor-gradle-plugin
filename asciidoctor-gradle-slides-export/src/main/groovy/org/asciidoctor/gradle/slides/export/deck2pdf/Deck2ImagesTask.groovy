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
import org.gradle.api.tasks.Input
import org.gradle.workers.WorkerExecutor

/** Runs a conversion form a slide deck to a collection of images
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class Deck2ImagesTask extends Deck2ExportBaseTask {

    @Input
    String fileNumberFormat = '%02d'

    protected Deck2ImagesTask(WorkerExecutor we, final String imageFormat) {
        super(we, imageFormat)
    }

    @Override
    protected String formatOutputFilename(String filename) {
        "${filename}-${fileNumberFormat}"
    }
}

