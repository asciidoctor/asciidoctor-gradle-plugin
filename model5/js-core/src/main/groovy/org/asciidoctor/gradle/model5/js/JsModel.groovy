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
package org.asciidoctor.gradle.model5.js

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.core.toolchains.AsciidoctorToolchain
import org.asciidoctor.gradle.model5.js.extensions.AsciidoctorjsExtension
import org.asciidoctor.gradle.model5.js.formatters.AsciidoctorjsOutputFormatter
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory

import java.util.function.Function

/**
 * Utilities for working with the new Asciidoctor model in an {@code Asciidoctor.js} context.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class JsModel {

    /**
     * The toolchain preparation task name
     * @param toolchainName Name of the toolchains
     * @return Task name
     */
    static String toolchainPrepareTaskName(String toolchainName) {
        "${PublicationUtils.TASK_PREFIX}Prepare${toolchainName.capitalize()}Toolchain"
    }

    /**
     * Registers an output formatter on all the {@code asciidoctorj} toolchains.
     *
     * @param toolchains Toolchain container
     * @param formatterClass The formatter class
     * @param engineTypes The {@code asciidoctor.js} engine implementation types, the formatter can be registered on.
     * @param factoryClass The factory for the formatter.
     * @param objectFactory objectFactory
     */
    static <T extends AsciidoctorjsOutputFormatter> void registerOutputFormatterFactory(
        ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
        Class<T> formatterClass,
        List<JsEngineType> engineTypes,
        Class<? extends NamedDomainObjectFactory<T>> factoryClass,
        ObjectFactory objectFactory
    ) {
        registerOutputFormatterFactory(toolchains, formatterClass, engineTypes) { AsciidoctorjsToolchain tc ->
            objectFactory.newInstance(factoryClass, tc)
        }
    }

    /**
     * Registers an output formatter on all the {@code asciidoctorj} toolchains.
     *
     * @param toolchains Toolchain container
     * @param formatterClass The formatter class
     * @param engineTypes The {@code asciidoctor.js} engine implementation types, the formatter can be registered on.
     * @param factoryFunction A function that will create a factory given a specific toolchain instance.
     */
    static <T extends AsciidoctorjsOutputFormatter> void registerOutputFormatterFactory(
        ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
        Class<T> formatterClass,
        List<JsEngineType> engineTypes,
        Function<AsciidoctorjsToolchain, NamedDomainObjectFactory<T>> factoryFunction
    ) {
        toolchains.withType(AsciidoctorjsToolchain).configureEach { tc ->
            if (tc.nativeImplementation && engineTypes.contains(JsEngineType.NATIVE) ||
                !tc.nativeImplementation && engineTypes.contains(JsEngineType.OPAL)
            ) {
                tc.registeredOutputFormatters.registerFactory(formatterClass, factoryFunction.apply(tc))
            }
        }
    }

    /**
     * Registers a named formatter on each of the {@code asciidoctorj} toolchains
     * @param toolchains Toolchains
     * @param formatterClass Formatter class
     * @param name Name of the formatter
     */
    static <T extends AsciidoctorjsOutputFormatter> void registerOutputFormatterOnAllToolchains(
        ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
        Class<T> formatterClass,
        String name
    ) {
        registerOutputFormatterOnAllToolchains(toolchains, formatterClass, name) {
        }
    }

    /**
     * Registers a named formatter on each of the {@code asciidoctorj} toolchains.
     * @param toolchains Toolchains.
     * @param formatterClass Formatter class.
     * @param name Name of the .
     * @param configurator Configurator of the output formatter.
     */
    static <T extends AsciidoctorjsOutputFormatter> void registerOutputFormatterOnAllToolchains(
        ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
        Class<T> formatterClass,
        String name,
        Action<T> configurator
    ) {
        toolchains.withType(AsciidoctorjsToolchain).configureEach { tc ->
            final fc = tc.registeredOutputFormatters.register(name, formatterClass)
            fc.configure(configurator)
        }
    }

    /**
     * Registers an extension on all the {@code asciidoctor.js} toolchains.
     *
     * @param toolchains Toolchain container
     * @param extensionClass The extension class
     * @param factoryClass The factory for the extension.
     * @param objectFactory objectFactory
     */
    static <T extends AsciidoctorjsExtension> void registerExtensionFactory(
        ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
        Class<T> extensionClass,
        Class<? extends NamedDomainObjectFactory<T>> factoryClass,
        ObjectFactory objectFactory
    ) {
        registerExtensionFactory(toolchains, extensionClass) { AsciidoctorjsToolchain tc ->
            objectFactory.newInstance(factoryClass, tc)
        }
    }

    /**
     * Registers an extension factory on the {@code asciidoctor.js} toolchains.
     *
     * @param toolchains Toolchain container
     * @param extensionClass The extension class
     * @param factoryFunction A function that will create a factory given a specific toolchain instance.
     */
    static <T extends AsciidoctorjsExtension> void registerExtensionFactory(
        ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
        Class<T> extensionClass,
        Function<AsciidoctorjsToolchain, NamedDomainObjectFactory<T>> factoryFunction
    ) {
        toolchains.withType(AsciidoctorjsToolchain).configureEach { tc ->
            tc.asciidocExtensions.registerFactory(extensionClass, factoryFunction.apply(tc))
        }
    }

    /**
     * Registers an extension on each of the {@code asciidoctorj} toolchains
     * @param toolchains Toolchains
     * @param extensionClass Extension class
     * @param name Name of the extension
     */
    static <T extends AsciidoctorjsExtension> void registerExtensionOnAllToolchains(
        ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
        Class<T> extensionClass,
        String name
    ) {
        registerExtensionOnAllToolchains(toolchains, extensionClass, name) {
        }
    }

    /**
     * Registers an extension on each of the {@code asciidoctor.js} toolchains.
     * @param toolchains Toolchains.
     * @param extensionClass Extension class.
     * @param name Name of the extension.
     * @param configurator Configurator of the extension.
     */
    static <T extends AsciidoctorjsExtension> void registerExtensionOnAllToolchains(
        ExtensiblePolymorphicDomainObjectContainer<AsciidoctorToolchain> toolchains,
        Class<T> extensionClass,
        String name,
        Action<T> configurator
    ) {
        toolchains.withType(AsciidoctorjsToolchain).configureEach { tc ->
            final fc = tc.asciidocExtensions.register(name, extensionClass)
            fc.configure(configurator)
        }
    }
}