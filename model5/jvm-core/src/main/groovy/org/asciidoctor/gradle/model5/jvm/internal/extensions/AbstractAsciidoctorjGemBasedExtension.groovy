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
package org.asciidoctor.gradle.model5.jvm.internal.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.internal.gems.GemUtils
import org.asciidoctor.gradle.model5.jvm.internal.utils.DependencyUpdater
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Base class for extension that uses a GEM.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
abstract class AbstractAsciidoctorjGemBasedExtension extends AbstractAsciidoctorjExtension {
    private final Property<String> gemVersion

    @SuppressWarnings('ParameterCount')
    protected AbstractAsciidoctorjGemBasedExtension(
        String name,
        String gemName,
        Provider<String> defaultVersion,
        String requires,
        List<String> excludeGems,
        AsciidoctorjToolchain tc,
        Project project
    ) {
        super(name, tc, project)
        this.gemVersion = objectFactory.property(String).convention(defaultVersion)
        addGem(gemName, excludeGems)
        packageRequires.add(requires)
    }

    /**
     * Override the default version.
     *
     * @param ver Anything that can be lazy-evaluated to a string.
     */
    void useVersion(Object ver) {
        ccso.stringTools().updateStringProperty(this.gemVersion, ver)
    }

    private void addGem(String gemName, List<String> excludes) {
        final updater = objectFactory.newInstance(DependencyUpdater)
        if (excludes.empty) {
            updater.add(
                GemUtils.nameForToolchainConfiguration(toolchain.name),
                "${GemUtils.GEM_GROUP}:${gemName}",
                this.gemVersion
            )
        } else {
            updater.add(
                GemUtils.nameForToolchainConfiguration(toolchain.name),
                "${GemUtils.GEM_GROUP}:${gemName}",
                this.gemVersion
            ) { ExternalModuleDependency emd ->
                excludes.each { mod ->
                    emd.exclude(group: GemUtils.GEM_GROUP, module: mod)
                }
            }
        }
    }
}
