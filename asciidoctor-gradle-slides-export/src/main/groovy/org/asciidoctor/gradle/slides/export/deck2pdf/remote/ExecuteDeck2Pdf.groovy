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
package org.asciidoctor.gradle.slides.export.deck2pdf.remote

import groovy.transform.CompileStatic

import javax.inject.Inject

/** Runs Deck2Pdf inside a worker or as an external process.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class ExecuteDeck2Pdf implements Runnable {

    private final DeckWorkerConfiguration configuration

    /** Entrypoint when running as a JAVA_EXEC prorocess.
     *
     * @param args Only want one parameter which is the location of the serialised data.
     */
    static void main(String[] args) {
        if (args.size() != 1) {
            throw new Deck2PdfRemoteExecutionException('No serialised location specified')
        }

        DeckWorkerConfiguration config
        new File(args[0]).withInputStream { input ->
            new ObjectInputStream(input).withCloseable { ois ->
                config = (DeckWorkerConfiguration) ois.readObject()
            }
        }
        new ExecuteDeck2Pdf(config).run()
    }

    /** Injection constructor allows for worker infrastructure to
     * find it or for it to be directly contructed when running
     * as an external process.
     *
     * @param config
     */
    @Inject
    ExecuteDeck2Pdf(final DeckWorkerConfiguration config) {
        this.configuration = config
    }

    /** Runs the conversion process. Will execute Deck2pdf once for
     * each conversion group.
     */
    @Override
    void run() {
        configuration.outputDir.mkdirs()
        configuration.arguments.each { String[] convertArgs ->
//            me.champeau.deck2pdf.Main.main(convertArgs)
        }
    }
}
