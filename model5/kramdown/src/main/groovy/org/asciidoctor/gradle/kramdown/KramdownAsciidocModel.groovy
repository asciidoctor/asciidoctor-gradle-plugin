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
package org.asciidoctor.gradle.kramdown

import groovy.transform.CompileStatic

/**
 * Utilities for working within a {@code kramdown-asciidoc}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class KramdownAsciidocModel {

    /**
     * Name of a declarable configuration for use with a specific @code kramdown-asciidoc} engine.
     *
     * @param engineName Name of engine.
     * @return Configuration name
     */
    static String nameForEngineConfiguration(String engineName) {
        "kramdownAsciidocEngine${engineName.capitalize()}"
    }

    /**
     * Name of a resolvable configuration for use with a specific @code kramdown-asciidoc} engine.
     *
     * @param engineName Name of engine.
     * @return Configuration name
     */
    static String nameForGemsConfiguration(String engineName) {
        "${nameForEngineConfiguration(engineName)}Gems"
    }

//    /**
//     * Registers an output formatter on all the @code kramdown-asciidoc} toolchains.
//     *
//     * @param toolchains Toolchain container
//     * @param formatterClass The formatter class
//     * @param factoryClass The factory for the formatter.
//     * @param objectFactory objectFactory
//     */
//    static <T extends AsciidoctorReducerOutputFormatter> void registerOutputFormatterFactory(
//        NamedDomainObjectContainer<KramdownToolchain> toolchains,
//        Class<T> formatterClass,
//        Class<? extends NamedDomainObjectFactory<T>> factoryClass,
//        ObjectFactory objectFactory
//    ) {
//        registerOutputFormatterFactory(toolchains, formatterClass) { AsciidoctorReducerToolchain tc ->
//            objectFactory.newInstance(factoryClass)
//        }
//    }

//    /**
//     * Registers an output formatter on all the @code kramdown-asciidoc} toolchains.
//     *
//     * @param toolchains Toolchain container
//     * @param formatterClass The formatter class
//     * @param factoryFunction A function that will create a factory given a specific toolchain instance.
//     */
//    static <T extends AsciidoctorReducerOutputFormatter> void registerOutputFormatterFactory(
//        NamedDomainObjectContainer<KramdownToolchain> toolchains,
//            Class<T> formatterClass,
//            Function<AsciidoctorReducerToolchain, NamedDomainObjectFactory<T>> factoryFunction
//    ) {
//        toolchains.configureEach { tc ->
//            tc.registeredOutputFormatters.registerFactory(formatterClass, factoryFunction.apply(tc))
//        }
//    }

//    /**
//     * Registers a named formatter on each of the @code kramdown-asciidoc}} toolchains
//     * @param toolchains Toolchains
//     * @param formatterClass Formatter class
//     * @param name Name of the formatter
//     */
//    static <T extends AsciidoctorReducerOutputFormatter> void registerOutputFormatterOnAllToolchains(
//        NamedDomainObjectContainer<KramdownToolchain> toolchains,
//            Class<T> formatterClass,
//            String name
//    ) {
//        registerOutputFormatterOnAllToolchains(toolchains, formatterClass, name) {
//        }
//    }

//    /**
//     * Registers a named formatter on each of the @code kramdown-asciidoc} toolchains.
//     * @param toolchains Toolchains.
//     * @param formatterClass Formatter class.
//     * @param name Name of the .
//     * @param configurator Configurator of the output formatter.
//     */
//    static <T extends AsciidoctorReducerOutputFormatter> void registerOutputFormatterOnAllToolchains(
//        NamedDomainObjectContainer<KramdownToolchain> toolchains,
//            Class<T> formatterClass,
//            String name,
//            Action<T> configurator
//    ) {
//        toolchains.configureEach { tc ->
//            final fc = tc.registeredOutputFormatters.register(name, formatterClass)
//            fc.configure(configurator)
//        }
//    }
}