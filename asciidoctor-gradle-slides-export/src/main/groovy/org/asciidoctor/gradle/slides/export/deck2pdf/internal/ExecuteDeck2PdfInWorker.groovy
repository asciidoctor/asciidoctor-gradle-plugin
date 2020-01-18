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
package org.asciidoctor.gradle.slides.export.deck2pdf.internal

import groovy.transform.CompileStatic

import javax.inject.Inject

/**
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class ExecuteDeck2PdfInWorker implements Runnable {

    private final DeckWorkerConfiguration configuration

    @Inject
    ExecuteDeck2PdfInWorker(final DeckWorkerConfiguration config) {
        this.configuration = config
    }

    @Override
    void run() {
        configuration.outputDir.mkdirs()
        configuration.arguments.each { String[] convertArgs ->
            me.champeau.deck2pdf.Main.main(convertArgs)
        }
    }
}
