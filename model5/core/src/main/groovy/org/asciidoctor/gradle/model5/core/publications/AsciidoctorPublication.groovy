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
package org.asciidoctor.gradle.model5.core.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.internal.publications.DefaultAsciidoctorOutputData
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.core.internal.tasks.TaskFactory
import org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTaskMethods
import org.asciidoctor.gradle.model5.core.toolchains.AsciidoctorToolchain
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.ysb33r.grolifant5.api.core.ClosureUtils

import javax.inject.Inject

/**
 * Defines a publication.
 *
 * <p>
 *     A publication consists of a source set and a definitions of outputs.
 * </p>
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorPublication implements Named {
    final String name
//    private final ConfigCacheSafeOperations ccso
    private final ObjectFactory objectFactory

    private final AsciidoctorModelExtension parent
    private final AsciidoctorSourceSet sources
    private final NamedDomainObjectContainer<DefaultAsciidoctorOutputData> outputs

    @Inject
    AsciidoctorPublication(String name, AsciidoctorModelExtension parent, Project tempProjectReference) {
        this.name = name
        this.parent = parent
//        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.objectFactory = tempProjectReference.objects
        this.sources = objectFactory.newInstance(AsciidoctorSourceSet, name)
        this.outputs = objectFactory.domainObjectContainer(DefaultAsciidoctorOutputData) { String theName ->
            objectFactory.newInstance(DefaultAsciidoctorOutputData, theName)
        }
    }

    /**
     * Configures a source set.
     *
     * @param configurator Configuration DSL.
     */
    void sourceSet(Action<AsciidoctorSourceSet> configurator) {
        configurator.execute(sources)
    }

    /**
     * Configures a source set.
     *
     * @param configurator Configuration DSL.
     */
    void sourceSet(@DelegatesTo(AsciidoctorSourceSet) Closure<?> configurator) {
        ClosureUtils.configureItem(this.sources, configurator)
    }

    /**
     * Direct access to the source set.
     *
     * @return The source set.
     */
    AsciidoctorSourceSet getSourceSet() {
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

    private void registerOutput(String finalName, String toolchainName, String outputFormatterName) {
        final toolchain = parent.toolchains.getByName(toolchainName)
        final formatter = toolchain.registeredOutputFormatters.getByName(outputFormatterName)
        final newOutput = this.outputs.create(finalName).tap { DefaultAsciidoctorOutputData it ->
            configureFrom(owner.name, toolchain, formatter, sourceSet)
        }
        registerConversionTask(toolchain, newOutput)
    }

    private void registerConversionTask(
            AsciidoctorToolchain toolchain,
            AsciidoctorOutputData outputData
    ) {
        final taskFactory = objectFactory.newInstance(TaskFactory)
        final taskName = PublicationUtils.conversionTaskName(name, outputData.name)

        taskFactory.registerConversionTask(taskName) { AsciidoctorTaskMethods atm ->
            atm.outputData = outputData
            atm.launcher = toolchain.launcher
            atm.safeMode = toolchain.safeMode
            atm.sourceDir = sources.sourceDir
            atm.sourcePatterns = sources.sourcePatterns
            atm.baseDir = sources.baseDir.baseDirStrategy.flatMap { it.getBaseDir(sources.sourceDir) }
            atm.attributes = sources.attributes.attributeResolver
        }

        taskFactory.addPrerequisiteTasks(taskName, toolchain.toolchainPreparationTaskNames)
    }
}
