/*
 * Copyright 2013-2021 the original author or authors.
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
package org.asciidoctor.gradle.jvm.epub

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorExecutionException
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.base.process.ProcessMode
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.GradleVersion
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.core.Version

import javax.inject.Inject
import java.util.regex.Matcher

/** Builds EPUB documents using the epub3 backend.
 *
 * @author Schalk W. Cronjé
 * @author Gary Hale
 *
 * @since 2.0.0
 */
@CompileStatic
@CacheableTask
class AsciidoctorEpubTask extends AbstractAsciidoctorTask {

    public static final String EPUB3 = 'epub3'

    private static final String BACKEND = 'epub3'
    private static final String EBOOK_FORMAT_ATTR = 'ebook-format'

    private final Set<String> ebookFormats = []

    @Inject
    AsciidoctorEpubTask(WorkerExecutor we) {
        super(we)

        configuredOutputOptions.backends = [BACKEND]
        copyNoResources()
        inProcess = JAVA_EXEC
    }

    /** The eBook formats that needs to be generated.
     *
     * @return eBook formats that needs to be generated.
     */
    @Input
    Set<String> getEbookFormats() {
        this.ebookFormats
    }

    /** Resets the list of formats that needs to be generated
     *
     * Any format supported by asciidoctorj-epub can be listed here.
     * This method will overide any {@code ebook-format} that is set via {@link #attributes}.
     *
     * @param formats List of formats. The plugin does not verify whether the eBook format
     * is valid.
     */
    @SuppressWarnings('UnnecessaryCollectCall')
    void setEbookFormats(Iterable<String> formats) {
        this.ebookFormats.clear()
        this.ebookFormats.addAll(Transform.toSet(formats) { String it -> it.toLowerCase() } as Set<String>)
    }

    /** Adds aditional eBook formats
     *
     * @param formats List of formats. The plugin does not verify whether the eBook format
     *  is valid.
     */
    void ebookFormats(String... formats) {
        this.ebookFormats.addAll(formats*.toLowerCase() as List)
    }

    /** The default pattern set for secondary sources.
     *
     * @return {@link #getDefaultSourceDocumentPattern} + `*docinfo*`.
     */
    @Override
    protected PatternSet getDefaultSecondarySourceDocumentPattern() {
        defaultSourceDocumentPattern
    }

    /** Returns all of the executor configurations for this task
     *
     * @return Executor configurations
     */
    @Override
    protected Map<String, ExecutorConfiguration> getExecutorConfigurations(
        File workingSourceDir,
        Set<File> sourceFiles,
        Optional<String> lang
    ) {
        Map<String, ExecutorConfiguration> executorConfigurations = super.getExecutorConfigurations(
            workingSourceDir,
            sourceFiles,
            lang
        )

        final Closure<String> backendName = { String fmt ->
            fmt == this.EPUB3 ? this.BACKEND : "epub3/${fmt}".toString()
        }

        final String ebookAttr = EBOOK_FORMAT_ATTR
        if (this.ebookFormats.empty) {
            throw new AsciidoctorExecutionException("No eBook format specified for task ${name}")
        } else if (this.ebookFormats.size() == 1) {
            executorConfigurations.collectEntries { configName, config ->
                config.attributes[ebookAttr] = this.ebookFormats.first()
                [backendName(this.ebookFormats.first().toString()), config]
            } as Map<String, ExecutorConfiguration>
        } else {
            Map<String, ExecutorConfiguration> newConfigurations = [:]
            String firstFormat = this.ebookFormats.first()
            executorConfigurations.each { configName, config ->
                this.ebookFormats.each { fmt ->
                    if (fmt == firstFormat) {
                        config.attributes[ebookAttr] = fmt
                        newConfigurations.put(backendName(fmt), config)
                    } else {
                        ExecutorConfiguration newConfig = (ExecutorConfiguration) config.clone()
                        newConfig.attributes[ebookAttr] = fmt
                        newConfigurations.put(backendName(fmt), newConfig)
                    }
                }
            }
            newConfigurations
        }
    }

    /** Selects a final process mode.
     *
     * Selects JAVA_EXEC on any Gradle version that has classpath ;eakage issues.
     *
     * @return Process mode to use for execution.
     */
    @Override
    protected ProcessMode getFinalProcessMode() {
        if (GradleVersion.current() <= LAST_GRADLE_WITH_CLASSPATH_LEAKAGE) {
            if (inProcess != JAVA_EXEC) {
                logger.warn 'EPUB processing on this version of Gradle will fail due to classpath issues. ' +
                    'Switching to JAVA_EXEC instead.'
            }
            return JAVA_EXEC
        }
        super.finalProcessMode
    }

    @CompileDynamic
    private Version getVersion(Matcher matcher) {
        Version.of(matcher[0][1])
    }
}
