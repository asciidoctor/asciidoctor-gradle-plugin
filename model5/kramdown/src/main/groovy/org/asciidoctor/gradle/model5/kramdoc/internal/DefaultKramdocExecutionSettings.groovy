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
package org.asciidoctor.gradle.model5.kramdoc.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.kramdoc.KramdocExecutionSettings
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import javax.inject.Inject

import static java.util.Collections.EMPTY_MAP

/**
 * Default mplementation of {@link KramdocExecutionSettings}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultKramdocExecutionSettings implements KramdocExecutionSettings {

    final Property<String> formatterName
    final Property<String> toolchainName
    final Provider<Map<String, String>> inputProperties

    @Inject
    DefaultKramdocExecutionSettings(ObjectFactory objectFactory) {
        formatterName = objectFactory.property(String)
        toolchainName = objectFactory.property(String)
        inputProperties = formatterName.map { EMPTY_MAP }
    }
}
