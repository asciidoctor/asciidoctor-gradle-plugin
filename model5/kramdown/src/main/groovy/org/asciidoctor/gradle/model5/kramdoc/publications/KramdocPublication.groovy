/*
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
package org.asciidoctor.gradle.model5.kramdoc.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultProvidedExternalSourceSet
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultProvidedExternalSources
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.core.publications.ProvidedExternalSourceSet
import org.asciidoctor.gradle.model5.kramdoc.KramdocAsciidocModel
import org.asciidoctor.gradle.model5.kramdoc.KramdocModelExtension
import org.asciidoctor.gradle.model5.kramdoc.internal.publications.DefaultKramdocOutputData
import org.asciidoctor.gradle.model5.kramdoc.internal.utils.TaskFactory
import org.asciidoctor.gradle.model5.kramdoc.tasks.KramdocTask
import org.asciidoctor.gradle.model5.kramdoc.tasks.KramdocTaskMethods
import org.asciidoctor.gradle.model5.kramdoc.toolchains.KramdocToolchain
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.util.PatternFilterable
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

import static java.util.Collections.EMPTY_SET
import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.conversionTaskName

/**
 * Defines a publication.
 *
 * <p>
 *     A publication consists of a source set and a definitions of outputs.
 * </p>
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class KramdocPublication implements Named {
    final String name
    private final ObjectFactory objectFactory

    private final KramdocModelExtension parent
    private final ConfigCacheSafeOperations ccso
    private final KramdocSourceSet sources
    private final NamedDomainObjectContainer<DefaultKramdocOutputData> outputs

    @Inject
    KramdocPublication(String name, KramdocModelExtension parent, Project tempProjectReference) {
        this.name = name
        this.parent = parent
        this.objectFactory = tempProjectReference.objects
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.sources = objectFactory.newInstance(KramdocSourceSet, name)
        this.outputs = objectFactory.domainObjectContainer(DefaultKramdocOutputData) { String theName ->
            objectFactory.newInstance(DefaultKramdocOutputData, theName)
        }
    }

    /**
     * Configures a source set.
     *
     * @param configurator Configuration DSL.
     */
    void sourceSet(Action<KramdocSourceSet> configurator) {
        configurator.execute(sources)
    }

    /**
     * Configures a source set.
     *
     * @param configurator Configuration DSL.
     */
    void sourceSet(@DelegatesTo(KramdocSourceSet) Closure<?> configurator) {
        ClosureUtils.configureItem(this.sources, configurator)
    }

    /**
     * Direct access to the source set.
     *
     * @return The source set.
     */
    KramdocSourceSet getSourceSet() {
        this.sources
    }

    /**
     * Creates an output using the supplied toolchain and linked output formatter.
     *
     * @param toolchainName Name of toolchain.
     * @param outputFormatterName Name of output formatter that is registered on that toolchain.
     */
    void output(String toolchainName, String outputFormatterName) {
        registerOutput(outputFormatterName, toolchainName, outputFormatterName)
    }

    /**
     * Creates an aliased output using the supplied toolchain and linked output formatter.
     *
     * <p>
     *     If output formatters in different toolchains have the same names and both toolchains are used in the same
     *     publication, one of them needs to be aliased otherwise a publicate error will occur.
     *     This affects the output directory too.
     * </p>
     *
     * @param toolchainName Name of toolchain.
     * @param outputFormatterName Name of output formatter that is registered on that toolchain.
     * @param alias Alias name.
     */
    void output(String toolchainName, String outputFormatterName, String alias) {
        registerOutput(alias, toolchainName, outputFormatterName)
    }

    /**
     * The name of the task that performs a conversion.
     *
     * @param outputName The name of the output formatter. In the case of an alias, then
     *  the alias name.
     *
     * @return The name of the task.
     *   This method makes no effort to validate the the task has been registered.
     *   It just returns the name.
     */
    String taskNameFor(String outputName) {
        conversionTaskName(name, outputName)
    }

    /**
     * The path below the build directory where output will be written to.
     *
     * @param outputName The name of the output formatter. In the case of an alias, then
     *  the alias name.
     *
     * @return The path below the build directory
     */
    String outputPath(String outputName) {
        PublicationUtils.outputPathFor(ccso.fsOperations(), name, outputName)
    }

    private void registerOutput(String finalName, String toolchainName, String outputFormatterName) {
        final toolchain = parent.toolchains.getByName(toolchainName)
        final formatter = toolchain.registeredOutputFormatters.getByName(outputFormatterName)
        final newOutput = this.outputs.create(finalName).tap { DefaultKramdocOutputData it ->
            configureFrom(owner.name, toolchain, formatter, sourceSet)
        }

        final task = registerConversionTask(
            toolchain,
            newOutput,
            formatter.copyResources
        )

        task.configure { KramdocTask t ->
            t.group = PublicationUtils.GROUP_NAME
            t.description = "Converts Markdown to AsciiDoc using formatter '${outputFormatterName}'"
            formatter.configureTaskInputs(t.inputs)
        }
    }

    @SuppressWarnings('UnnecessaryObjectReferences')
    private TaskProvider<? extends KramdocTask> registerConversionTask(
        KramdocToolchain toolchain,
        KramdocOutputData outputData,
        boolean copyResources
    ) {
        final taskFactory = objectFactory.newInstance(TaskFactory)
        final taskName = conversionTaskName(KramdocAsciidocModel.TASK_PREFIX, name, outputData.name)

        final task = taskFactory.registerConversionTask(taskName) { KramdocTaskMethods atm ->
            atm.outputData = outputData
            atm.launcher = toolchain.launcher
            atm.sourceDir = sources.sourceDir
            atm.sourcePatterns = sources.sourcePatterns
            atm.secondarySourcePatterns = sources.secondarySourcePatterns
            // TODO: Support fatal warnings in a future release
            atm.fatalWarnings = ccso.providerTools().provider { -> EMPTY_SET }
            atm.externalSources = copyResources ? sources.externalSources : sources.externalSources.map {
                new DefaultProvidedExternalSources(
                    it.externalSources.map { list ->
                        list.collect { item ->
                            new DefaultProvidedExternalSourceSet(
                                item.sourcePatterns,
                                ccso.providerTools().provider { -> (PatternFilterable) null },
                                item.into,
                                item.sourcesAndResources

                            )
                        } as List<ProvidedExternalSourceSet>
                    },
                    it.duplicatesStrategy
                )
            }
        }

        taskFactory.addPrerequisiteTasks(taskName, toolchain.toolchainPreparationTaskNames)
        taskFactory.addPrerequisiteTasks(taskName, sources.externalSourcesBuiltBy)

        task
    }
}
