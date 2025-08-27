/*
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
package org.asciidoctor.gradle.model5.jvm.internal

import groovy.transform.CompileStatic
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.AttributesBuilder
import org.asciidoctor.Options
import org.asciidoctor.OptionsBuilder
import org.asciidoctor.SafeMode
import org.gradle.workers.WorkAction

/**
 * Running AsciidoctorJ in a worker.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
@SuppressWarnings('AbstractClassWithoutAbstractMethod')
abstract class LauncherWorker implements WorkAction<LauncherParameters> {
    @Override
    void execute() {
        final asciidoctor = Asciidoctor.Factory.create()

        final reqs = parameters.requires.get()
        if (!reqs.empty) {
            asciidoctor.requireLibraries(reqs)
        }

        // TODO: Handle extensions
        // TODO: Handle logging
        // TODO: Handle errors (fatal messages)

        final destDir = parameters.destinationDir.get().asFile

        destDir.mkdirs()

        // If converting all source files in one go
        asciidoctor.convertFiles(parameters.sourceFiles.get(),normalisedOptions())

//        parameters.sourceFiles.get().each {
//            asciidoctor.convertFile(it,normalisedOptionsFor(it))
//        }
    }

//    @SuppressWarnings('UnnecessaryObjectReferences')
    private Options normalisedOptions() {//(final File file, ExecutorConfiguration runConfiguration) {
        final optionsBuilder = Options.builder()
        final attributesBuilder = Attributes.builder()

        parameters.attributes.get().each { k,v ->
            if(v == null) {
                attributesBuilder.attribute(k, null)
            } else {
                attributesBuilder.attribute(k, v)
            }
        }

        // TODO: Handle presented options.
//        options.each { key, value -> optionsBuilder.option(key, value) }
        optionsBuilder.tap {
            inPlace(false)
            mkDirs(true)
            backend(parameters.backend.get())
            safe(SafeMode.valueOf(parameters.safeMode.get().toUpperCase(Locale.US)))
            baseDir(parameters.baseDir.get().asFile)
            toDir(parameters.destinationDir.get().asFile)
            attributes(attributesBuilder.build())
        }
//
//            optionsBuilder.attributes(attributesBuilder.build())
//        }

        optionsBuilder.build()
    }
}
