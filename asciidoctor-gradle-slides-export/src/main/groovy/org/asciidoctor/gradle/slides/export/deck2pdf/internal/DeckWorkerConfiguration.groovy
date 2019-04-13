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
import groovy.transform.TupleConstructor

/** Configuration for deck2pdf.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
@TupleConstructor
class DeckWorkerConfiguration implements Serializable {
    final List<String[]> arguments = []
    final File outputDir

    DeckWorkerConfiguration(
        List<String> args,
        File outputDir,
        final Map<String, File> outputInputMatch
    ) {
        this.outputDir = outputDir
        outputInputMatch.each { String outputFilenamePattern, File inputFile ->
            List<String> newArgs = []
            newArgs.addAll(args)
            newArgs.add(inputFile.canonicalPath)
            newArgs.add(new File(outputDir.canonicalFile, outputFilenamePattern).path)
            arguments.add(newArgs.toArray() as String[])
        }
    }
}
