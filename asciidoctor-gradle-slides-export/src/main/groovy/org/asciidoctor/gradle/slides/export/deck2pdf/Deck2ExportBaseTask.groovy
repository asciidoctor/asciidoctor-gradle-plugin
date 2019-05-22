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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.process.ProcessMode
import org.asciidoctor.gradle.base.slides.Profile
import org.asciidoctor.gradle.slides.export.base.AbstractExportBaseTask
import org.asciidoctor.gradle.slides.export.deck2pdf.remote.DeckWorkerConfiguration
import org.asciidoctor.gradle.slides.export.deck2pdf.remote.ExecuteDeck2Pdf
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec
import org.gradle.workers.WorkerConfiguration
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.FileUtils

import static org.asciidoctor.gradle.base.process.ProcessMode.JAVA_EXEC
import static org.asciidoctor.gradle.base.slides.Profile.DECK_JS
import static org.asciidoctor.gradle.base.slides.Profile.DZ
import static org.asciidoctor.gradle.base.slides.Profile.FLOWTIME_JS
import static org.asciidoctor.gradle.base.slides.Profile.GOOGLE_HTML5
import static org.asciidoctor.gradle.base.slides.Profile.IMPRESS_JS
import static org.asciidoctor.gradle.base.slides.Profile.REMARK_JS
import static org.asciidoctor.gradle.base.slides.Profile.REVEAL_JS
import static org.asciidoctor.gradle.base.slides.Profile.RUBAN
import static org.gradle.workers.IsolationMode.CLASSLOADER
import static org.ysb33r.grolifant.api.MapUtils.stringizeValues

/** Base task class for converting slides to PDFs and images using the
 * deck2pdf tool.
 *
 * @author Schalk W. Cronjé
 * @since 3.0
 */
@CompileStatic
class Deck2ExportBaseTask extends AbstractExportBaseTask {

    private static final List<Profile> PROFILES = [
            DECK_JS, DZ, FLOWTIME_JS, GOOGLE_HTML5, IMPRESS_JS, REMARK_JS, REVEAL_JS, RUBAN
    ]

    private final ProcessMode processMode = JAVA_EXEC
    private final WorkerExecutor worker
    private final String format

    private Provider<Map<String, Object>> parametersProvider
//    private
//    final org.ysb33r.grolifant.api.JavaForkOptions javaForkOptions = new org.ysb33r.grolifant.api.JavaForkOptions()
    @Internal
    boolean parallelMode = false

    /** Parameters provided via providers are processed before any locally set parameters.
     *
     * Usually this is via an extension on a task.
     *
     * @param provider
     */
    void setParametersProvider(Provider<Map<String, Object>> provider) {
        this.parametersProvider = provider
    }

    @Input
    String getProfileName() {
        profile?.name
    }

//    /** Set fork options for {@link #JAVA_EXEC} and {@link #OUT_OF_PROCESS} modes.
//     *
//     * These options are ignored if {@link #inProcess} {@code ==} {@link #IN_PROCESS}.
//     *
//     * @param configurator Closure that configures a {@link org.ysb33r.grolifant.api.JavaForkOptions} instance.
//     */
//    void forkOptions(@DelegatesTo(org.ysb33r.grolifant.api.JavaForkOptions) Closure configurator) {
//        executeDelegatingClosure(this.javaForkOptions, configurator)
//    }
//
//    /** Set fork options for {@link #JAVA_EXEC} and {@link #OUT_OF_PROCESS} modes.
//     *
//     * These options are ignored if {@link #inProcess} {@code ==} {@link #IN_PROCESS}.
//     *
//     * @param configurator Action that configures a {@link org.ysb33r.grolifant.api.JavaForkOptions} instance.
//     */
//    void forkOptions(Action<org.ysb33r.grolifant.api.JavaForkOptions> configurator) {
//        configurator.execute(this.javaForkOptions)
//    }

    @SuppressWarnings('UnnecessaryGetter')
    @TaskAction
    void convert() {
        List<String> deck2PdfArgs = buildArguments()
        File outputDirCached = getOutputDir()
        Set<File> deck2PdfClasspath = project.extensions.getByType(Deck2PdfExtension).configuration.files
        String workerDisplayName = "Asciidoctor slides conversion using deck2pdf (task=${name})"

        if (processMode == JAVA_EXEC) {
            Map<String, File> conversionMap = slides.get().collectEntries { File input ->
                final String output = outputFileNameFromInput(input)
                [output, input]
            }
            DeckWorkerConfiguration config = new DeckWorkerConfiguration(deck2PdfArgs, outputDirCached, conversionMap)
            File configData = project.file("${project.buildDir}/tmp/${FileUtils.toSafeFileName(name)}.deck2pdf-data")
            DeckWorkerConfiguration.toFile(configData, config)
            project.javaexec { JavaExecSpec jes ->
                jes.with {
                    main = ExecuteDeck2Pdf.canonicalName
                    args configData.absolutePath
                    classpath(deck2PdfClasspath + FileUtils.resolveClassLocation(ExecuteDeck2Pdf).file)
//                    configureForkOptions(config.forkOptions)
                }
            }
        } else {
            if (parallelMode) {
                slides.get().each { File input ->
                    String output = outputFileNameFromInput(input)
                    worker.submit(ExecuteDeck2Pdf) { WorkerConfiguration config ->
                        config.displayName = workerDisplayName
                        config.classpath(deck2PdfClasspath)
                        config.params(new DeckWorkerConfiguration(deck2PdfArgs, outputDirCached, [(output): input]))
                        config.isolationMode = CLASSLOADER
//            configureForkOptions(config.forkOptions)
                    }
                }
            } else {
                Map<String, File> conversionMap = slides.get().collectEntries { File input ->
                    final String output = outputFileNameFromInput(input)
                    [output, input]
                }
                worker.submit(ExecuteDeck2Pdf) { WorkerConfiguration config ->
                    config.displayName = workerDisplayName
                    config.classpath(deck2PdfClasspath)
                    config.params(new DeckWorkerConfiguration(deck2PdfArgs, outputDirCached, conversionMap))
                    config.isolationMode = CLASSLOADER
//            configureForkOptions(config.forkOptions)
                }
            }
        }
    }

    /** Returns any standard parameters that were provided externally.
     *
     * @return Map of parameters with values.
     */
    @Internal
    Map<String, String> getStandardProvidedParameters() {
        if (this.parametersProvider) {
            List<String> params = standardParameters
            stringizeValues(this.parametersProvider.get().findAll { key, value ->
                key in params
            })
        } else {
            [:]
        }
    }

    @Internal
    Map<String, String> getOtherProvidedParameters() {
        if (this.parametersProvider) {
            List<String> stdParams = standardParameters
            stringizeValues(this.parametersProvider.get().findAll { key, value ->
                !(key in stdParams)
            })
        } else {
            [:]
        }
    }

    @Override
    @Internal
    protected List<Profile> getSupportedProfiles() {
        PROFILES
    }

/** Provide the infrastructure
 * for a conversion.
 *
 * @param we Worker instance to use.
 * @param format Output format as supported by deck2pdf.
 */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    protected Deck2ExportBaseTask(final WorkerExecutor we, final String format) {
        worker = we
        this.format = format
        inputs.property('parameters', { Deck2ExportBaseTask t ->
            t.otherProvidedParameters + t.standardProvidedParameters
        }.curry(this))
    }

    /** Add additional arguments to send to deck2pdf.
     *
     * The default implementation simply return the unmodified container
     * @param args Argument list to update
     * @return Reference to the container.
     */
    protected List<String> addArguments(List<String> args) {
        args
    }

    /** Allows for the output filename to be changed.
     *
     * The default implementation returns the filename.
     *
     * @param filename Proposed filename without extension
     * @return Filename with potential formatting characters
     */
    protected String formatOutputFilename(String filename) {
        filename
    }

    /** Names of standard parameters
     *
     * @return List of standard parameters.
     */
    @Internal
    protected List<String> getStandardParameters() {
        ['height', 'width']
    }

    /** Provides the outputfile name
     *
     * @param input Input file
     * @return Formatted output file name
     */
    @Override
    protected String outputFileNameFromInput(File input) {
        formatOutputFilename(input.name.replaceFirst(~/\.html$/, '') + ".${format}")
    }

    /** Adds a parameter for deck2pdf.
     *
     * If both {@code value} and the {@code providedMap[providedKey]} is {@code null}
     * nothing will be added.
     *
     * @param args Container of arguments to update.
     * @param providedMap Hashmap of values that were provided by external source.
     * @param keyword Keyname of the parameter to set.
     * @param value Value to set. Can be {@code null}.
     * @param providedKey The key to look for in the provided map.
     */
    private void addDeckParam(
            final List<String> args,
            final Map<String, String> providedMap,
            final String keyword,
            String value,
            String providedKey
    ) {
        if (value != null) {
            args.addAll(["--${keyword}".toString(), value])
        } else if (providedMap.containsKey(providedKey)) {
            args.addAll(["--${keyword}".toString(), providedMap[providedKey]])
        }
    }

    private List<String> buildArguments() {
        List<String> params = this.standardParameters
        Map<String, String> standard = standardProvidedParameters
        List<String> args = ['--format', this.format, '--profile', profileName]

        params.each {
            addDeckParam(args, standard, it, getLocalValueForParam(it)?.toString(), it)
        }

        Map<String, String> other = otherProvidedParameters
        other.each { keyword, value ->
            addDeckParam(args, other, keyword, null, keyword)
        }
        addArguments(args)
        args
    }

    @CompileDynamic
    private Object getLocalValueForParam(final String name) {
        this."get${name.capitalize()}"()
    }
//    private void configureForkOptions(JavaForkOptions pfo) {
//        this.javaForkOptions.copyTo(pfo)
//    }
}
