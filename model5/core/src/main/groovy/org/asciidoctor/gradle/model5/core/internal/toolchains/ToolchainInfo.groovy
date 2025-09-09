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
package org.asciidoctor.gradle.model5.core.internal.toolchains

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension

/**
 * Builds a representation of toolchains.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class ToolchainInfo {
    final Map<String, SingleToolchain> toolchains

    static ToolchainInfo buildFrom(AsciidoctorModelExtension ame) {
        final allToolchainNames = ame.toolchains.names

        final allToolchains = allToolchainNames.collectEntries { tcName ->
            final tc = ame.toolchains.getByName(tcName)

            final allFormattersForTc = tc.registeredOutputFormatters.names.collectEntries { fName ->
                final formatter = tc.registeredOutputFormatters.getByName(fName)

                [fName, new Formatter().tap {
                    type = formatter.displayType ?: className(formatter)
                    backend = formatter.backend.get().backend
                }]
            } as Map<String, Formatter>

            final allExtensionsForTc = tc.asciidocExtensions.names.collectEntries { eName ->
                final ext = tc.asciidocExtensions.getByName(eName)
                [eName, new AsciidocExtension().tap {
                    type = ext.displayType ?: className(ext)
                }]
            } as Map<String,AsciidocExtension>

            [tcName, new SingleToolchain().tap {
                type = tc.displayType ?: className(tc)
                formatters = allFormattersForTc
                asciidocExtensions = allExtensionsForTc
            }]
        } as Map<String, SingleToolchain>

        new ToolchainInfo(allToolchains)
    }

    static class SingleToolchain {
        String type
        Map<String, Formatter> formatters
        Map<String, AsciidocExtension> asciidocExtensions
    }

    static class Formatter {
        String type
        String backend
    }

    static class AsciidocExtension {
        String type
    }

    private ToolchainInfo(Map<String, SingleToolchain> map) {
        final finalMap = new TreeMap<String, SingleToolchain>()
        finalMap.putAll(map)
        this.toolchains = finalMap.asImmutable()
    }

    static private String className(Object type) {
        type.class.canonicalName.replaceFirst(~/_Decorated$/, '')
    }
}
