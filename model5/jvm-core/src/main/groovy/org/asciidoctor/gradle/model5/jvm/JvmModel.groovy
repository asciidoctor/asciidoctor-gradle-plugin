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
package org.asciidoctor.gradle.model5.jvm

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.toolchains.AsciidoctorToolchain
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjHtml5
import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjOutputFormatter
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory

import java.util.function.Function

/**
 * Utilities for working with the new Asciidoctor model in an AsciidoctorJ context.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class JvmModel {
    /**
     * Name of a declarable configuration for use with a specific AsciidoctorJ engine.
     *
     * @param engineName Name of engine.
     * @return Configuration name
     */
    static String nameForEngineConfiguration(String engineName) {
        "asciidoctorjEngine${engineName.capitalize()}"
    }

    /**
     * Name of a resolvable configuration for use with a specific AsciidoctorJ engine.
     *
     * @param engineName Name of engine.
     * @return Configuration name
     */
    static String nameForEngineConfigurationResolvable(String engineName) {
        "${nameForEngineConfiguration(engineName)}RuntimeClasspath"
    }

    /**
     * Name of a declarable configuration for use with a specific AsciidoctorJ toolchain + output formatter.
     *
     * @param toolchainName Name of toolchain.
     * @param formatterName Name of output formatter.
     * @return Configuration name
     */
    static String nameForOutputFormatterConfiguration(String toolchainName, String formatterName) {
        "asciidoctorjOutputFormatter${toolchainName.capitalize()}${formatterName.capitalize()}"
    }

    /**
     * Name of a resolvable configuration for use with a specific AsciidoctorJ toolchain + output formatter.
     *
     * @param toolchainName Name of toolchain.
     * @param formatterName Name of output formatter.
     * @return Configuration name
     */
    static String nameForOutputFormatterConfigurationResolvable(String toolchainName, String formatterName) {
        "${nameForOutputFormatterConfiguration(toolchainName, formatterName)}RuntimeClasspath"
    }

    /**
     * Registers an output formatter on all the {@code asciidoctorj} toolchains.
     *
     * @param toolchains Toolchain container
     * @param formatterClass The formatter class
     * @param factoryClass The factory for the formatter.
     * @param objectFactory objectFactory
     */
    static <T extends AsciidoctorjOutputFormatter> void registerOutputFormatterFactory(
            ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
            Class<T> formatterClass,
            Class<? extends NamedDomainObjectFactory<T>> factoryClass,
            ObjectFactory objectFactory
    ) {
        registerOutputFormatterFactory(toolchains,formatterClass) { AsciidoctorjToolchain tc ->
            objectFactory.newInstance(factoryClass,tc)
        }
    }
    /**
     * Registers an output formatter on all the {@code asciidoctorj} toolchains.
     *
     * @param toolchains Toolchain container
     * @param formatterClass The formatter class
     * @param factoryFunction A function that will create a factory given a specific toolchain instance.
     */
    static <T extends AsciidoctorjOutputFormatter> void registerOutputFormatterFactory(
            ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
            Class<T> formatterClass,
            Function<AsciidoctorjToolchain,NamedDomainObjectFactory<T>> factoryFunction
    ) {
        toolchains.withType(AsciidoctorjToolchain).configureEach { tc ->
            tc.registeredOutputFormatters.registerFactory(formatterClass,factoryFunction.apply(tc))
        }
    }
}
